package ge.baqar.gogia.malazani.media

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.ui.MenuActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.android.inject


@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@FlowPreview
class MediaPlaybackService : Service(), MediaPlayer.OnPreparedListener {
    inner class LocalBinder : Binder() {
        val service: MediaPlaybackService
            get() = this@MediaPlaybackService
    }

    private var notificationManager: NotificationManager? = null
    private val notificationId: Int = 1024
    val mediaPlayerController: MediaPlayerController by inject()

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(p0: Intent?): IBinder {
        return LocalBinder()
    }

    override fun onPrepared(p0: MediaPlayer?) {

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            PLAY_MEDIA -> {
                val position = intent.getIntExtra("position", 0)
                mediaPlayerController.play(position)
                showNotification()
            }
            RESUME_MEDIA -> {
                mediaPlayerController.resume()
                showNotification(true)
            }
            PAUSE_MEDIA -> {
                mediaPlayerController.pause()
                showNotification(true)
            }
            STOP_MEDIA -> {
                mediaPlayerController.stop()
                stopForeground(true)
            }
            PREV_MEDIA -> {
                mediaPlayerController.previous()
                showNotification()
            }
            NEXT_MEDIA -> {
                mediaPlayerController.next()
                showNotification()
            }
        }
        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("RemoteViewLayout", "UnspecifiedImmutableFlag")
    private fun showNotification(update: Boolean = false) {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MenuActivity::class.java),
            0
        )
        val currentSong = mediaPlayerController.getCurrentSong()

        val notificationLayout = RemoteViews(packageName, R.layout.view_notification_small)
        val notificationLayoutExpanded = RemoteViews(packageName, R.layout.view_notification_large)

        notificationLayout.setTextViewText(R.id.notification_title, currentSong.title)
        notificationLayoutExpanded.setTextViewText(R.id.notification_title, currentSong.title)

        notificationLayout.setOnClickPendingIntent(R.id.notification_view_small, contentIntent)
        notificationLayoutExpanded.setOnClickPendingIntent(
            R.id.notification_view_small,
            contentIntent
        )

        initRemoteViewClicks(notificationLayoutExpanded)

        val channelId = "HEADS_UP_NOTIFICATIONS"
        val channel = NotificationChannel(
            channelId,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.enableVibration(false)

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayoutExpanded)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setColor(getColor(R.color.colorAccentLighter))
            .setColorized(true)
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
            .setVibrate(longArrayOf(-1))

        if (update) {
            notificationManager?.notify(notificationId, notification.build())
        } else {
            startForeground(notificationId, notification.build())
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun initRemoteViewClicks(notificationLayoutExpanded: RemoteViews) {
        notificationLayoutExpanded.setOnClickPendingIntent(
            R.id.playStopButton, PendingIntent.getService(
                this, 0,
                Intent(this, MediaPlaybackService::class.java).apply {
                    action = STOP_MEDIA
                },
                0
            )
        )
        val isPlaying = mediaPlayerController.isPlaying()
        if (isPlaying) {
            notificationLayoutExpanded.setImageViewResource(
                R.id.playPauseButton,
                R.drawable.ic_baseline_pause_circle_outline_24
            )
        } else {
            notificationLayoutExpanded.setImageViewResource(
                R.id.playPauseButton,
                R.drawable.ic_baseline_play_circle_outline_24
            )
        }
        notificationLayoutExpanded.setOnClickPendingIntent(
            R.id.playPauseButton, PendingIntent.getService(
                this, 0,
                Intent(this, MediaPlaybackService::class.java).apply {
                    action = if (isPlaying) PAUSE_MEDIA else RESUME_MEDIA
                },
                0
            )
        )
        notificationLayoutExpanded.setOnClickPendingIntent(
            R.id.playPrevButton, PendingIntent.getService(
                this, 0,
                Intent(this, MediaPlaybackService::class.java).apply {
                    action = PREV_MEDIA
                },
                0
            )
        )
        notificationLayoutExpanded.setOnClickPendingIntent(
            R.id.playNextButton, PendingIntent.getService(
                this, 0,
                Intent(this, MediaPlaybackService::class.java).apply {
                    action = NEXT_MEDIA
                },
                0
            )
        )
    }

    companion object {
        const val PLAY_MEDIA = "PLAY_MEDIA"
        const val RESUME_MEDIA = "RESUME_MEDIA"
        const val PAUSE_MEDIA = "PAUSE_MEDIA"
        const val STOP_MEDIA = "STOP_MEDIA"
        const val PREV_MEDIA = "PREV_MEDIA"
        const val NEXT_MEDIA = "NEXT_MEDIA"
    }
}