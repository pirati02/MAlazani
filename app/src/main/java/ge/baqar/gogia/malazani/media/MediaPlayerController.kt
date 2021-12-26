package ge.baqar.gogia.malazani.media

import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.viewModelScope
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.databinding.ActivityMenuBinding
import ge.baqar.gogia.malazani.media.MediaPlaybackService.Companion.NEXT_MEDIA
import ge.baqar.gogia.malazani.media.MediaPlaybackService.Companion.PAUSE_OR_MEDIA
import ge.baqar.gogia.malazani.media.MediaPlaybackService.Companion.PLAY_MEDIA
import ge.baqar.gogia.malazani.media.MediaPlaybackService.Companion.PREV_MEDIA
import ge.baqar.gogia.malazani.media.player.AudioPlayer
import ge.baqar.gogia.malazani.poko.AutoPlayState
import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.Song
import ge.baqar.gogia.malazani.poko.events.ArtistChanged
import ge.baqar.gogia.malazani.poko.events.OpenArtistFragment
import ge.baqar.gogia.malazani.storage.FolkAppPreferences
import ge.baqar.gogia.malazani.ui.artist.ArtistViewModel
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus

class MediaPlayerController(
    private val viewModel: ArtistViewModel,
    private val folkAppPreferences: FolkAppPreferences,
    private val audioPlayer: AudioPlayer
) {
    var binding: ActivityMenuBinding? = null
    var playListEnsemble: Ensemble? = null
    var playList: MutableList<Song>? = null

    private var position = 0
    private var autoPlayState = AutoPlayState.OFF
    private var playerControlsAreVisible = true

    fun play() {
        playList?.let {
            val artist = playList!![this.position]

            initializeViewClickListeners()
            listenAudioPlayerChanges(artist)
            binding?.included?.mediaPlayerView?.let {
                it.visibility = View.VISIBLE
            }
            checkAutoPlayEnabled()
            binding?.included?.playingTrackTitle?.text = artist.name
            binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        }
    }

    private fun checkAutoPlayEnabled() {
        autoPlayState = folkAppPreferences.getAutoPlay()
        binding?.included?.playerAutoPlayButton?.post {
            when (autoPlayState) {
                AutoPlayState.OFF -> {
                    binding?.included?.playerAutoPlayButton?.setImageResource(R.drawable.ic_baseline_repeat_24_off)
                }
                AutoPlayState.REPEAT_ONE -> {
                    binding?.included?.playerAutoPlayButton?.setImageResource(R.drawable.ic_baseline_repeat_one_24)
                }
                AutoPlayState.REPEAT_ALBUM -> {
                    binding?.included?.playerAutoPlayButton?.setImageResource(R.drawable.ic_baseline_repeat_24_on)
                }
            }
        }
    }

    private fun onPrepareListener() {
        val duration = audioPlayer.getDurationString()
        binding?.included?.playingTrackDurationTime?.text = duration
        binding?.included?.playerProgressBar?.max = audioPlayer.getDuration().toInt()
    }

    private fun listenAudioPlayerChanges(song: Song) {
        audioPlayer.listenPlayer {
            if (it) {
                binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            } else {
                binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
            }
        }
        viewModel.viewModelScope.launch {
            audioPlayer.play(song.path, song.localPath) { onPrepareListener() }
        }
        audioPlayer.completed {
            binding?.included?.playingTrackTime?.text = null
            binding?.included?.playerProgressBar?.progress = 0
            binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
            binding?.included?.playingTrackDurationTime?.text = null

            when (autoPlayState) {
                AutoPlayState.OFF -> {
                    return@completed
                }
                AutoPlayState.REPEAT_ONE -> {
                    val song = playList!![position]
                    viewModel.viewModelScope.launch {
                        audioPlayer.play(song.path, song.localPath) { onPrepareListener() }
                        EventBus.getDefault().post(ArtistChanged(PLAY_MEDIA))
                    }
                    binding?.included?.playingTrackTitle?.text = song.name
                    binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                }
                AutoPlayState.REPEAT_ALBUM -> {
                    if ((position + 1) < playList?.size!!) {
                        ++position
                        val song = playList!![position]
                        viewModel.viewModelScope.launch {
                            audioPlayer.play(song.path, song.localPath) { onPrepareListener() }
                            EventBus.getDefault().post(ArtistChanged(NEXT_MEDIA))
                        }
                        binding?.included?.playingTrackTitle?.text = song.name
                        binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                    }
                }
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
            stop()
        }
        binding?.included?.playerAutoPlayButton?.setOnClickListener {
            when (autoPlayState) {
                AutoPlayState.OFF -> {
                    autoPlayState = AutoPlayState.REPEAT_ONE
                }
                AutoPlayState.REPEAT_ONE -> {
                    autoPlayState = AutoPlayState.REPEAT_ALBUM
                }
                AutoPlayState.REPEAT_ALBUM -> {
                    autoPlayState = AutoPlayState.OFF
                }
            }
            folkAppPreferences.updateAutoPlay(autoPlayState)
            checkAutoPlayEnabled()
        }
        playerControlsAreVisible = folkAppPreferences.getPlayerState()
        if (playerControlsAreVisible) {
            binding?.included?.playerControlsView?.visibility = View.VISIBLE
            binding?.included?.playerViewCloseBtn?.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
        } else {
            binding?.included?.playerControlsView?.visibility = View.GONE
            binding?.included?.playerViewCloseBtn?.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
        }
        binding?.included?.playerViewCloseBtn?.setOnClickListener {
            playerControlsAreVisible = !playerControlsAreVisible
            folkAppPreferences.setPlayerState(playerControlsAreVisible)
            if (playerControlsAreVisible) {
                binding?.included?.playerControlsView?.visibility = View.VISIBLE
                binding?.included?.playerViewCloseBtn?.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            } else {
                binding?.included?.playerControlsView?.visibility = View.GONE
                binding?.included?.playerViewCloseBtn?.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            }
        }
        binding?.included?.playerPlaylistButton?.setOnClickListener {
            EventBus.getDefault().post(OpenArtistFragment(playListEnsemble!!))
        }
    }

    fun pause() {
        audioPlayer.pause()
        EventBus.getDefault().post(ArtistChanged(PAUSE_OR_MEDIA))
    }

    fun stop() {
        audioPlayer.release()
        binding?.included?.playingTrackTime?.text = null
        binding?.included?.playerProgressBar?.progress = 0
        binding?.included?.playingTrackDurationTime?.text = null
        binding?.included?.mediaPlayerView?.let {
            it.visibility = View.GONE
        }
    }

    fun resume() {
        audioPlayer.resume()
        EventBus.getDefault().post(ArtistChanged(PAUSE_OR_MEDIA))
    }

    fun next() {
        if ((position + 1) < playList?.size!!) {
            ++position
            val song = playList!![position]
            updateUI(song)
            viewModel.viewModelScope.launch {
                audioPlayer.play(song.path, song.localPath) { onPrepareListener() }
                EventBus.getDefault().post(ArtistChanged(NEXT_MEDIA))
            }
            binding?.included?.playingTrackTitle?.text = song.name
        }
    }


    fun previous() {
        if (position > 0) {
            --position
            val prevItem = playList!![position]
            updateUI(prevItem)
            viewModel.viewModelScope.launch {
                audioPlayer.play(prevItem.path, prevItem.localPath) { onPrepareListener() }
                EventBus.getDefault().post(ArtistChanged(PREV_MEDIA))
            }
            binding?.included?.playingTrackTitle?.text = prevItem.name
        }
    }

    fun getCurrentSong(): Song? {
        return if (playList == null) null else playList!![position]
    }

    fun isPlaying(): Boolean {
        return audioPlayer.isPlaying()
    }

    fun updatePlayer() {
        playList?.let {
            val song = playList!![position]
            updateUI(song)
        }
    }

    private fun updateUI(artist: Song) {
        initializeViewClickListeners()
        binding?.included?.mediaPlayerView?.let {
            it.visibility = View.VISIBLE
        }
        checkAutoPlayEnabled()
        binding?.included?.playingTrackTitle?.text = artist.name
        binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        onPrepareListener()
    }

    fun setInitialPosition(position: Int) {
        this.position = position
    }
}