package ge.baqar.gogia.malazani.ui.artist

import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.databinding.ActivityMenuBinding
import ge.baqar.gogia.malazani.poko.AlazaniArtistListItem
import ge.baqar.gogia.malazani.utility.player.AudioPlayer
import kotlinx.coroutines.*

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class MediaPlayerController(
    private val audioPlayer: AudioPlayer,
    private val binding: ActivityMenuBinding,
    private val viewModel: ArtistViewModel,
    var dataSource: MutableList<AlazaniArtistListItem>
) {
    private var _position = 0
    private var autoPlayEnabled = true

    fun play(position: Int) {
        _position = position
        val artist = dataSource[position]
        binding.playingTrackTitle.text = artist.title
        binding.mediaPlayerView.let {
            it.visibility = View.VISIBLE
        }
        initializeViewClickListeners()
        binding.playPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        viewModel.viewModelScope.launch {
            audioPlayer.play(viewModel.formatUrl(artist.link))
            withContext(Dispatchers.Main) {
                audioPlayer.listenPlayer {
                    if (it) {
                        binding.playPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                    } else {
                        binding.playPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
                    }
                }
            }
        }
        audioPlayer.updateTimeHandler { progress, time ->
            binding.playingTrackTime.post {
                binding.playingTrackTime.text = time
                binding.playerProgressBar.progress = progress?.toInt()!!
            }
        }
        val duration = audioPlayer.getDurationString()
        binding.playingTrackDurationTime.text = duration
        binding.playerProgressBar.max = audioPlayer.getDuration().toInt()

        audioPlayer.completed {
            if (!autoPlayEnabled) return@completed

            if ((position + 1) < dataSource.size) {
                ++_position
                val nextItem = dataSource[position]
                viewModel.viewModelScope.launch {
                    audioPlayer.play(viewModel.formatUrl(nextItem.link))
                }

                binding.playingTrackTitle.text = nextItem.title
                binding.playPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            }
        }
    }

    private fun initializeViewClickListeners() {
        binding.playerProgressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                audioPlayer.playOn(p0?.progress)
            }
        })
        binding.playNextButton.setOnClickListener {
            if ((_position + 1) < dataSource.size) {
                ++_position
                val nextItem = dataSource[_position]
                viewModel.viewModelScope.launch {
                    audioPlayer.play(viewModel.formatUrl(nextItem.link))
                }

                binding.playingTrackTitle.text = nextItem.title
            }
        }
        binding.playPrevButton.setOnClickListener {
            if (_position > 0) {
                --_position
                val prevItem = dataSource[_position]
                viewModel.viewModelScope.launch {
                    audioPlayer.play(viewModel.formatUrl(prevItem.link))
                }

                binding.playingTrackTitle.text = prevItem.title
            }
        }
        binding.playStopButton.setOnClickListener {
            viewModel.viewModelScope.launch {
                audioPlayer.release()
                binding.playPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
            }
        }
        binding.playerAutoPlayButton.setOnClickListener {
            autoPlayEnabled = !autoPlayEnabled
            if (autoPlayEnabled) {
                Toast.makeText(it.context, R.string.auto_play_on, Toast.LENGTH_SHORT).show()
                binding.playerAutoPlayButton.setImageResource(R.drawable.ic_baseline_repeat_24_white)
            } else {
                Toast.makeText(it.context, R.string.auto_play_off, Toast.LENGTH_SHORT).show()
                binding.playerAutoPlayButton.setImageResource(R.drawable.ic_baseline_repeat_24)
            }
        }
    }
}