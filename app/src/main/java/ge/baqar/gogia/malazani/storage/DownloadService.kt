package ge.baqar.gogia.malazani.storage

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.ui.MenuActivity
import ge.baqar.gogia.model.DownloadableSong
import ge.baqar.gogia.model.Ensemble
import ge.baqar.gogia.model.events.SongsMarkedAsFavourite
import ge.baqar.gogia.model.events.SongsUnmarkedAsFavourite
import kotlinx.coroutines.InternalCoroutinesApi
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject
import kotlin.time.ExperimentalTime

@InternalCoroutinesApi
@ExperimentalTime
class DownloadService : Service() {
    private var notificationManager: NotificationManager? = null
    private val albumDownloadProvider: AlbumDownloadProvider by inject()

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ensemble = intent?.getParcelableExtra<Ensemble>("ensemble")
        when (intent?.action) {
            DOWNLOAD_SONGS -> {
                val songs = intent.getParcelableArrayListExtra<DownloadableSong>("songs")
                DownloadServiceManager.isRunning = true
                val id = downloadSongs(ensemble!!, songs!!)

                showNotification(ensemble, id, songs)
            }
            STOP_DOWNLOADING -> {
                val songs = intent.getParcelableArrayListExtra<DownloadableSong>("songs")
                DownloadServiceManager.isRunning = false
                cancelDownloads(ensemble!!, songs!!)
                stopForeground(true)
            }
        }
        return START_NOT_STICKY
    }

    private fun cancelDownloads(ensemble: Ensemble, songs: ArrayList<DownloadableSong>) {
        val albumDownloadManager = albumDownloadProvider.tryGet(ensemble.id)
        albumDownloadManager.clearDownloads(ensemble.id, songs, ensemble.nameEng)
        albumDownloadManager.cancel()
        albumDownloadProvider.dispose(albumDownloadManager)
        EventBus.getDefault().post(SongsUnmarkedAsFavourite(songs.toMutableList()))
    }

    private fun downloadSongs(ensemble: Ensemble, songs: ArrayList<DownloadableSong>): Int {
        val albumDownloadManager = albumDownloadProvider.tryGet(ensemble.id)
        albumDownloadManager.setDownloadData(
            ensemble,
            songs
        )
        albumDownloadManager.download {
            DownloadServiceManager.isRunning = false
            stopForeground(true)
            EventBus.getDefault().post(SongsMarkedAsFavourite(songs.toMutableList()))
        }
        return albumDownloadManager.downloadId
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun showNotification(ensemble: Ensemble, id: Int, songs: ArrayList<DownloadableSong>) {
        val notificationBuilder: NotificationCompat.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "DOWNLOADING_NOTIFICATION_CHANNEL for ${ensemble.name}"
                val channel = NotificationChannel(
                    channelId,
                    "download songs for ${ensemble.name}",
                    NotificationManager.IMPORTANCE_LOW
                )
                notificationManager?.createNotificationChannel(channel)
                NotificationCompat.Builder(this, channelId)
            } else {
                NotificationCompat.Builder(this)
            }

        val downloadSongs = songs.joinToString(", \n") { it.name }
        val songsDownloadTitle = "${getString(R.string.downloading)} \n $downloadSongs"
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            Intent.FILL_IN_ACTION
        }
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MenuActivity::class.java).apply {
                action = STOP_DOWNLOADING
                putExtra("ensemble", ensemble)
                putExtra("songs", downloadSongs)
            },
            flag
        )

        val cancelAction = NotificationCompat.Action(
            R.drawable.ic_round_cancel_24,
            getString(R.string.cancel),
            contentIntent
        )

        val notification: NotificationCompat.Builder = notificationBuilder
            .setSmallIcon(R.drawable.ic_baseline_download_for_offline_24)
            .setAutoCancel(true)
            .setContentText(songsDownloadTitle)
            .setStyle(NotificationCompat.BigTextStyle())
            .addAction(cancelAction)

        startForeground(id, notification.build())
    }

    companion object {
        const val DOWNLOAD_SONGS = "DOWNLOAD_SONGS"
        const val STOP_DOWNLOADING = "STOP_DOWNLOADING"
    }
}