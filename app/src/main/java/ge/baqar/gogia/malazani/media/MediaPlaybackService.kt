package ge.baqar.gogia.malazani.media

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.ui.MenuActivity
import ge.baqar.gogia.model.events.*
import kotlinx.coroutines.InternalCoroutinesApi
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import kotlin.time.ExperimentalTime


@ExperimentalTime
@InternalCoroutinesApi
class MediaPlaybackService : Service(), MediaPlayer.OnPreparedListener {

    private var notificationManager: NotificationManager? = null
    private val notificationId: Int = 1024
    private val mediaPlayerController: MediaPlayerController by inject()

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        EventBus.getDefault().post(ServiceCreatedEvent())
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onPrepared(p0: MediaPlayer?) {

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleAction(intent?.action)
        return START_STICKY
    }

    @Subscribe
    fun songChanged(event: ArtistChanged) {
        handleAction(event.action, false)
    }

    private fun handleAction(action: String?, useMediaController: Boolean = true) {
        MediaPlaybackServiceManager.isRunning = true
        when (action) {
            PLAY_MEDIA -> {
                mediaPlayerController.play()
                showNotification(true)
            }
            PAUSE_OR_MEDIA -> {
                if (useMediaController) {
                    if (mediaPlayerController.isPlaying()) {
                        mediaPlayerController.pause()
                        MediaPlaybackServiceManager.isRunning = false
                    } else
                        mediaPlayerController.resume()
                }

                if (!mediaPlayerController.isPlaying())
                    MediaPlaybackServiceManager.isRunning = false
                showNotification()
            }
            STOP_MEDIA -> {
                MediaPlaybackServiceManager.isRunning = false
                if (useMediaController)
                    mediaPlayerController.stop()
                stopForeground(true)
            }
            PREV_MEDIA -> {
                if (useMediaController)
                    mediaPlayerController.previous()
                showNotification(true)
                EventBus.getDefault().post(CurrentPlayingSong(mediaPlayerController.getCurrentSong()))
            }
            NEXT_MEDIA -> {
                if (useMediaController)
                    mediaPlayerController.next()
                showNotification(true)
                EventBus.getDefault().post(CurrentPlayingSong(mediaPlayerController.getCurrentSong()))
            }
            null -> {
                if (mediaPlayerController.isPlaying()) {
                    showNotification()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun songsMarkedAsFavourite(event: SongsMarkedAsFavourite) {
        mediaPlayerController.songsMarkedAsFavourite(event)
    }

    @Subscribe
    fun songsUnMarkedAsFavourite(event: SongsUnmarkedAsFavourite) {
        mediaPlayerController.songsUnMarkedAsFavourite(event)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun getCurrentSong(event: GetCurrentSong){
        EventBus.getDefault().post(CurrentPlayingSong(mediaPlayerController.getCurrentSong()))
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    fun onMessageEvent(event: RequestMediaControllerInstance?) {
        EventBus.getDefault().postSticky(mediaPlayerController)
    }

    @SuppressLint("RemoteViewLayout", "UnspecifiedImmutableFlag")
    private fun showNotification(showResumeIcon: Boolean = false) {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MenuActivity::class.java),
            0
        )
        val currentSong = mediaPlayerController.getCurrentSong()
        currentSong?.let {
            val notificationLayout = RemoteViews(packageName, R.layout.view_notification_small)
            val notificationLayoutExpanded =
                RemoteViews(packageName, R.layout.view_notification_large)

            notificationLayout.setTextViewText(R.id.notification_title, currentSong.name)
            notificationLayoutExpanded.setTextViewText(
                R.id.notification_title,
                currentSong.name
            )

            notificationLayout.setOnClickPendingIntent(
                R.id.notification_view_small,
                contentIntent
            )
            notificationLayoutExpanded.setOnClickPendingIntent(
                R.id.notification_view_small,
                contentIntent
            )

            initRemoteViewClicks(notificationLayoutExpanded, contentIntent, showResumeIcon)

            val notificationBuilder: NotificationCompat.Builder
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "HEADS_UP_NOTIFICATIONS"
                val channel = NotificationChannel(
                    channelId,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW
                )
                channel.enableVibration(false)
                notificationBuilder = NotificationCompat.Builder(this, channelId)
                notificationManager?.createNotificationChannel(channel)
            } else {
                notificationBuilder = NotificationCompat.Builder(this)
            }
            val notification: NotificationCompat.Builder = notificationBuilder
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setColorized(true)
                .setColor(ContextCompat.getColor(this, R.color.colorAccentLighter))

            startForeground(notificationId, notification.build())
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun initRemoteViewClicks(
        notificationLayoutExpanded: RemoteViews,
        contentIntent: PendingIntent,
        showResumeIcon: Boolean = false
    ) {

        notificationLayoutExpanded.setOnClickPendingIntent(
            R.id.notification_view_large,
            contentIntent
        )
        notificationLayoutExpanded.setOnClickPendingIntent(
            R.id.playStopButton, PendingIntent.getService(
                this, 0,
                Intent(this, MediaPlaybackService::class.java).apply {
                    action = STOP_MEDIA
                },
                0
            )
        )

        if (mediaPlayerController.isPlaying() || showResumeIcon) {
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
            R.id.playPauseButton,
            PendingIntent.getService(
                this, 0,
                Intent(this, MediaPlaybackService::class.java).apply {
                    action = PAUSE_OR_MEDIA
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
        const val PAUSE_OR_MEDIA = "PAUSE_OR_MEDIA"
        const val STOP_MEDIA = "STOP_MEDIA"
        const val PREV_MEDIA = "PREV_MEDIA"
        const val NEXT_MEDIA = "NEXT_MEDIA"
    }
}