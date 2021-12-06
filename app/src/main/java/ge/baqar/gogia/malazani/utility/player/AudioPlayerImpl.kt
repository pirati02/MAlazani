package ge.baqar.gogia.malazani.utility.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.FileOutputStream


class AudioPlayerImpl(private var context: Context) : AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var _callback: ((Boolean) -> Unit)? = null


    init {
        mediaPlayer = MediaPlayer()
    }

    override suspend fun listenPlayer(callback: (Boolean) -> Unit) {
        _callback = callback
    }

    override suspend fun isPlaying(): Boolean {
        return isPlaying
    }

    override suspend fun play(audioData: String) {
        if (isPlaying) release()
        if (mediaPlayer == null) mediaPlayer = MediaPlayer()

        isPlaying = true
        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer?.setDataSource(audioData)
        mediaPlayer?.prepare()
        mediaPlayer?.start()
        _callback?.invoke(isPlaying)
    }

    override suspend fun pause() {
        isPlaying = false
        mediaPlayer?.pause()
        _callback?.invoke(isPlaying)
    }

    override suspend fun resume() {
        isPlaying = true
        mediaPlayer?.start()
        _callback?.invoke(isPlaying)
    }

    override suspend fun release() {
        isPlaying = false
        mediaPlayer?.release()
        mediaPlayer = null
        _callback?.invoke(isPlaying)
    }
}