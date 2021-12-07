package ge.baqar.gogia.malazani.media.player

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.CountDownTimer
import androidx.annotation.RequiresApi


class AudioPlayerImpl : AudioPlayer {
    private var updateCallback: ((Long?, String?) -> Unit)? = null
    private var mediaPlayer: MediaPlayer? = null
    private var timer: CountDownTimer? = null
    private var isPlaying = false
    private var mediaPlayerIsPlayingCallback: ((Boolean) -> Unit)? = null

    init {
        mediaPlayer = MediaPlayer()
    }

    override suspend fun listenPlayer(callback: (Boolean) -> Unit) {
        mediaPlayerIsPlayingCallback = callback
    }

    override suspend fun isPlaying(): Boolean {
        return isPlaying
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun play(audioData: String, callback: () -> Unit) {
        if (isPlaying) release()
        if (mediaPlayer == null) mediaPlayer = MediaPlayer()

        isPlaying = true
        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer?.setDataSource(audioData)
        mediaPlayer?.prepareAsync()
        mediaPlayer?.setOnPreparedListener {
            mediaPlayer?.start()
            startTimer()
            callback.invoke()
            mediaPlayerIsPlayingCallback?.invoke(isPlaying)
        }
    }

    override suspend fun pause() {
        isPlaying = false
        mediaPlayer?.pause()
        mediaPlayerIsPlayingCallback?.invoke(isPlaying)
    }

    override suspend fun resume() {
        isPlaying = true
        mediaPlayer?.start()
        mediaPlayerIsPlayingCallback?.invoke(isPlaying)
    }

    override fun updateTimeHandler(callback: (Long?, String?) -> Unit) {
        if (mediaPlayer == null) return
        updateCallback = callback
    }

    fun startTimer() {
        timer = object : CountDownTimer(getDuration(), 1000.toLong()) {
            override fun onTick(p0: Long) {
                val currentDuration = mediaPlayer?.currentPosition?.toLong()
                val durationString = getTimeString(currentDuration!!)
                updateCallback?.invoke(currentDuration, durationString)
            }

            override fun onFinish() {
            }
        }
        timer?.start()
    }

    override fun completed(callback: () -> Unit) {
        mediaPlayer?.setOnCompletionListener {
            callback.invoke()
            timer?.cancel()
        }
    }

    override fun getDurationString(): String {
        val totalDuration = mediaPlayer?.duration?.toLong()
        return getTimeString(totalDuration!!)
    }

    override fun getDuration(): Long {
        val totalDuration = mediaPlayer?.duration?.toLong()
        return totalDuration!!
    }

    override fun playOn(progress: Int?) {
        mediaPlayer?.seekTo(progress!!)
    }

    private fun getTimeString(millis: Long): String {
        val buf = StringBuffer()
        val minutes = (millis % (1000 * 60 * 60) / (1000 * 60)).toInt()
        val seconds = (millis % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        buf.append(String.format("%02d", minutes))
            .append(":")
            .append(String.format("%02d", seconds))
        return buf.toString()
    }

    override suspend fun release() {
        isPlaying = false
        mediaPlayer?.release()
        timer?.cancel()
        timer = null
        mediaPlayer = null
        mediaPlayerIsPlayingCallback?.invoke(isPlaying)
    }
}