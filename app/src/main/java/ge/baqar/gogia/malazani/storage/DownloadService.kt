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
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.android.inject
import java.util.*
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
                showNotification(ensemble, id)
            }
            STOP_DOWNLOADING -> {
                DownloadServiceManager.isRunning = false
                cancelDownloads(ensemble!!)
                stopForeground(true)
            }
        }
        return START_NOT_STICKY
    }

    private fun cancelDownloads(ensemble: Ensemble) {
        val albumDownloadManager = albumDownloadProvider.tryGet(ensemble.id)
        albumDownloadManager.clearDownloads(ensemble.id)
        albumDownloadManager.cancel()
        albumDownloadProvider.dispose(albumDownloadManager)
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
        }
        return albumDownloadManager.downloadId
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    private fun showNotification(ensemble: Ensemble, id: Int) {
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
        val songsDownloadTitle =
            String.format(getString(R.string.downloading_songs), ensemble.name)

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MenuActivity::class.java).apply {
                action = STOP_DOWNLOADING
                putExtra("ensemble", ensemble)
            },
            Intent.FILL_IN_ACTION
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
        val DOWNLOAD_SONGS = "DOWNLOAD_SONGS"
        val STOP_DOWNLOADING = "STOP_DOWNLOADING"
    }
}