package ge.baqar.gogia.malazani.media

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.databinding.ActivityMenuBinding
import ge.baqar.gogia.malazani.media.player.AudioPlayer
import ge.baqar.gogia.malazani.poko.AlazaniArtistListItem
import ge.baqar.gogia.malazani.ui.artist.ArtistViewModel
import kotlinx.coroutines.*

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@RequiresApi(Build.VERSION_CODES.O)
class MediaPlayerController(
    private val viewModel: ArtistViewModel,
    private val audioPlayer: AudioPlayer
) {

    var binding: ActivityMenuBinding? = null
    var dataSource: MutableList<AlazaniArtistListItem>? = null
    var position = 0
        get() {
            return field
        }
        private set(value) {
            field = value
        }

    private var autoPlayEnabled = true
    private var playerControlsAreVisible = true

    @SuppressLint("LongLogTag")
    fun play(position: Int) {
        dataSource?.let {
            this.position = position
            val artist = dataSource!![this.position]

            initializeViewClickListeners()
            listenAudioPlayerChanges(artist)
            binding?.included?.mediaPlayerView?.let {
                it.visibility = View.VISIBLE
            }
            binding?.included?.playingTrackTitle?.text = artist.title
            binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        }
    }

    private fun onPrepareListener() {
        val duration = audioPlayer.getDurationString()
        binding?.included?.playingTrackDurationTime?.text = duration
        binding?.included?.playerProgressBar?.max = audioPlayer.getDuration().toInt()
    }

    private fun listenAudioPlayerChanges(artist: AlazaniArtistListItem) {
        audioPlayer.listenPlayer {
            if (it) {
                binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            } else {
                binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
            }
        }
        viewModel.viewModelScope.launch {
            audioPlayer.play(viewModel.formatUrl(artist.link)) { onPrepareListener() }
        }
        audioPlayer.completed {
            if (!autoPlayEnabled) return@completed

            if ((position + 1) < dataSource?.size!!) {
                ++position
                val nextItem = dataSource!![position]
                viewModel.viewModelScope.launch {
                    audioPlayer.play(viewModel.formatUrl(nextItem.link)) { onPrepareListener() }
                }
                binding?.included?.playingTrackTitle?.text = nextItem.title
                binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            }
        }
        audioPlayer.updateTimeHandler { progress, time ->
            binding?.included?.playingTrackTime?.text = time
            binding?.included?.playerProgressBar?.progress = progress?.toInt()!!
        }
    }

    private fun initializeViewClickListeners() {
        binding?.included?.playerProgressBar?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                viewModel.viewModelScope.launch {
                    audioPlayer.playOn(p0?.progress)
                }
            }
        })

        binding?.included?.playStopButton?.setOnClickListener {
            audioPlayer.release()
            binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
        }
        binding?.included?.playNextButton?.setOnClickListener {
            next()
        }
        binding?.included?.playPrevButton?.setOnClickListener {
            previous()
        }
        binding?.included?.playPauseButton?.setOnClickListener {
            if (audioPlayer.isPlaying()) {
                pause()
            } else {
                resume()
            }
        }
        binding?.included?.playStopButton?.setOnClickListener {
            audioPlayer.release()
            binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
        }
        binding?.included?.playerAutoPlayButton?.setOnClickListener {
            autoPlayEnabled = !autoPlayEnabled
            if (autoPlayEnabled) {
                Toast.makeText(it.context, R.string.auto_play_on, Toast.LENGTH_SHORT).show()
                binding?.included?.playerAutoPlayButton?.setImageResource(R.drawable.ic_baseline_repeat_24_white)
            } else {
                Toast.makeText(it.context, R.string.auto_play_off, Toast.LENGTH_SHORT).show()
                binding?.included?.playerAutoPlayButton?.setImageResource(R.drawable.ic_baseline_repeat_24)
            }
        }
        binding?.included?.playerViewCloseBtn?.setOnClickListener {
            playerControlsAreVisible = !playerControlsAreVisible
            if (playerControlsAreVisible) {
                binding?.included?.playerControlsView?.visibility = View.VISIBLE
                binding?.included?.playerViewCloseBtn?.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            } else {
                binding?.included?.playerControlsView?.visibility = View.GONE
                binding?.included?.playerViewCloseBtn?.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            }
        }
    }

    fun pause() {
        audioPlayer.pause()
    }

    fun stop() {
        audioPlayer.release()
    }

    fun resume() {
        audioPlayer.resume()
    }

    fun next() {
        if ((position + 1) < dataSource?.size!!) {
            ++position
            val nextItem = dataSource!![position]
            viewModel.viewModelScope.launch {
                audioPlayer.play(viewModel.formatUrl(nextItem.link)) { onPrepareListener() }
            }
            binding?.included?.playingTrackTitle?.text = nextItem.title
        }
    }


    fun previous() {
        if (position > 0) {
            --position
            val prevItem = dataSource!![position]
            viewModel.viewModelScope.launch {
                audioPlayer.play(viewModel.formatUrl(prevItem.link)) { onPrepareListener() }
            }
            binding?.included?.playingTrackTitle?.text = prevItem.title
        }
    }

    fun getCurrentSong(): AlazaniArtistListItem? {
        return if (dataSource == null) null else dataSource!![position]
    }

    fun isPlaying(): Boolean {
        return audioPlayer.isPlaying()
    }

    fun updatePlayer() {
        dataSource?.let {
            val artist = dataSource!![position]
            initializeViewClickListeners()
            binding?.included?.mediaPlayerView?.let {
                it.visibility = View.VISIBLE
            }
            binding?.included?.playingTrackTitle?.text = artist.title
            binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            onPrepareListener()
        }
    }
}