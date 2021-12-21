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
import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.ui.MenuActivity
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.time.ExperimentalTime

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
        when (intent?.action) {
            DOWNLOAD_SONGS -> {
                val ensemble = intent.getParcelableExtra<Ensemble>("ensemble")
                showNotification(ensemble!!)
                val songs = intent.getParcelableArrayListExtra<DownloadableSong>("songs")
                DownloadServiceManager.isRunning = true
                downloadSongs(ensemble, songs!!)
            }
            STOP_DOWNLOADING -> {
                val ensemble = intent.getParcelableExtra<Ensemble>("ensemble")
                DownloadServiceManager.isRunning = false
                stopServiceInternally(ensemble)
            }
        }
        return START_NOT_STICKY
    }

    private fun stopServiceInternally(ensemble: Ensemble?) {
        ensemble?.let {
            cancelDownloads(ensemble)
        }
        stopForeground(true)
    }

    private fun cancelDownloads(ensemble: Ensemble) {
        val albumDownloadManager = albumDownloadProvider.tryGet(ensemble.id)
        albumDownloadManager.cancel()
        albumDownloadProvider.dispose(albumDownloadManager)
    }

    private fun downloadSongs(ensemble: Ensemble?, songs: ArrayList<DownloadableSong>) {
        val albumDownloadManager = albumDownloadProvider.tryGet(ensemble?.id!!)
        albumDownloadManager.setDownloadData(
            ensemble,
            songs
        )
        albumDownloadManager.download {
            stopServiceInternally(ensemble)
            DownloadServiceManager.isRunning = false
        }
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    private fun showNotification(ensemble: Ensemble) {
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

//        val contentIntent = PendingIntent.getActivity(
//            this, 0,
//            Intent(this, MenuActivity::class.java).apply {
//                action = STOP_DOWNLOADING
//            },
//            Intent.FILL_IN_ACTION
//        )
//
//        val cancelAction = NotificationCompat.Action(
//            R.drawable.ic_round_cancel_24,
//            getString(R.string.cancel),
//            contentIntent
//        )

        val notification: NotificationCompat.Builder = notificationBuilder
            .setSmallIcon(R.drawable.ic_baseline_download_for_offline_24)
            .setAutoCancel(true)
            .setContentText(songsDownloadTitle)
            .setStyle(NotificationCompat.BigTextStyle())
//            .addAction(cancelAction)

        val id = Random().nextInt(2000)
        startForeground(id, notification.build())
    }


    companion object {
        val DOWNLOAD_SONGS = "DOWNLOAD_SONGS"
        val STOP_DOWNLOADING = "STOP_DOWNLOADING"
    }
}