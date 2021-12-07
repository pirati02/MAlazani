package ge.baqar.gogia.malazani.media

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.android.inject

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
public class MediaPlaybackService : Service(), MediaPlayer.OnPreparedListener {
    inner class LocalBinder : Binder() {
        val service: MediaPlaybackService
            get() = this@MediaPlaybackService
    }

    val mediaPlayerController: MediaPlayerController by inject()

    override fun onBind(p0: Intent?): IBinder? {
        return LocalBinder()
    }

    override fun onPrepared(p0: MediaPlayer?) {

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            PLAY_MEDIA -> {
                val position = intent.getIntExtra("position", 0)
                mediaPlayerController.play(position)
            }
        }
        return result
    }

    companion object {
        const val PLAY_MEDIA = "PLAY_MEDIA"
    }
}