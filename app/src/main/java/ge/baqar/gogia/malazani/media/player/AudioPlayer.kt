package ge.baqar.gogia.malazani.media.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.CountDownTimer
import android.os.PowerManager
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class AudioPlayer(private val context: Context) {
    private var updateCallback: ((Long?, String?) -> Unit)? = null
    private var mediaPlayer: MediaPlayer? = null
    private var timer: CountDownTimer? = null
    private var mediaPlayerIsPlayingCallback: ((Boolean) -> Unit)? = null

    fun listenPlayer(callback: (Boolean) -> Unit) {
        mediaPlayerIsPlayingCallback = callback
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun play(link: String?, audioData: ByteArray?, callback: () -> Unit) {
        if (link == null && audioData == null) return
        reset()
        if (mediaPlayer == null) mediaPlayer = MediaPlayer()

        mediaPlayer?.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        if (audioData != null) {
            val tempMp3: File = File.createTempFile("temp_song", "mp3", context.cacheDir)
            tempMp3.deleteOnExit()
            val fos = FileOutputStream(tempMp3)
            fos.write(audioData)
            fos.close()
            val fis = FileInputStream(tempMp3)
            mediaPlayer?.setDataSource(fis.fd)
        } else {
            mediaPlayer?.setDataSource(link)
        }
        mediaPlayer?.prepareAsync()
        mediaPlayer?.setOnPreparedListener {
            mediaPlayer?.start()
            startTimer()
            callback.invoke()
            mediaPlayerIsPlayingCallback?.invoke(isPlaying())
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        mediaPlayerIsPlayingCallback?.invoke(isPlaying())
    }

    fun resume() {
        mediaPlayer?.start()
        mediaPlayerIsPlayingCallback?.invoke(isPlaying())
    }

    fun updateTimeHandler(callback: (Long?, String?) -> Unit) {
        if (mediaPlayer == null) return
        updateCallback = callback
    }

    private fun startTimer() {
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

    fun completed(callback: () -> Unit) {
        mediaPlayer?.setOnCompletionListener {
            callback.invoke()
            timer?.cancel()
        }
    }

    fun getDurationString(): String {
        val totalDuration = mediaPlayer?.duration?.toLong()
        return getTimeString(totalDuration!!)
    }

    fun getDuration(): Long {
        val totalDuration = mediaPlayer?.duration?.toLong()
        return totalDuration!!
    }

    fun playOn(progress: Int?) {
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

    fun reset() {
        mediaPlayer?.reset()
        timer?.cancel()
        mediaPlayerIsPlayingCallback?.invoke(isPlaying())
    }


    fun release() {
        mediaPlayer?.release()
        timer?.cancel()
        timer = null
        mediaPlayer = null
        mediaPlayerIsPlayingCallback?.invoke(isPlaying())
    }
}