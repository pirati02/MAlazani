package ge.baqar.gogia.malazani.media

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.databinding.ActivityMenuBinding
import ge.baqar.gogia.malazani.media.MediaPlaybackService.Companion.NEXT_MEDIA
import ge.baqar.gogia.malazani.media.MediaPlaybackService.Companion.PAUSE_OR_MEDIA
import ge.baqar.gogia.malazani.media.MediaPlaybackService.Companion.PREV_MEDIA
import ge.baqar.gogia.malazani.media.MediaPlaybackService.Companion.STOP_MEDIA
import ge.baqar.gogia.malazani.media.player.AudioPlayer
import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.Song
import ge.baqar.gogia.malazani.poko.events.ArtistChanged
import ge.baqar.gogia.malazani.poko.events.OpenArtistFragment
import ge.baqar.gogia.malazani.ui.artist.ArtistViewModel
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@RequiresApi(Build.VERSION_CODES.O)
class MediaPlayerController(
    private val viewModel: ArtistViewModel,
    private val audioPlayer: AudioPlayer,
    context: Context
) {
    var binding: ActivityMenuBinding? = null
    var playListEnsemble: Ensemble? = null
    var playList: MutableList<Song>? = null
    private val preferences: SharedPreferences =
        context.getSharedPreferences(context.packageName, MODE_PRIVATE)
    var position = 0
        private set

    private val autoPlayEnabledKey = "autoPlayEnabled"
    private var autoPlayEnabled = true
    private var playerControlsAreVisible = true

    @SuppressLint("LongLogTag")
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
        autoPlayEnabled = preferences.getBoolean(autoPlayEnabledKey, false)
        if (autoPlayEnabled) {
            binding?.included?.playerAutoPlayButton?.setImageResource(R.drawable.ic_baseline_repeat_24_white)
        } else {
            binding?.included?.playerAutoPlayButton?.setImageResource(R.drawable.ic_baseline_repeat_24)
        }
    }

    private fun onPrepareListener() {
        val duration = audioPlayer.getDurationString()
        binding?.included?.playingTrackDurationTime?.text = duration
        binding?.included?.playerProgressBar?.max = audioPlayer.getDuration().toInt()
    }

    private fun listenAudioPlayerChanges(artist: Song) {
        audioPlayer.listenPlayer {
            if (it) {
                binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            } else {
                binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
            }
        }
        viewModel.viewModelScope.launch {
            audioPlayer.play(artist.path) { onPrepareListener() }
        }
        audioPlayer.completed {
            binding?.included?.playingTrackTime?.text = null
            binding?.included?.playerProgressBar?.progress = 0
            binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
            binding?.included?.playingTrackDurationTime?.text = null

            if (!autoPlayEnabled){
                binding?.included?.mediaPlayerView?.let {
                    it.visibility = View.GONE
                }
                return@completed
            }

            if ((position + 1) < playList?.size!!) {
                ++position
                val nextItem = playList!![position]
                viewModel.viewModelScope.launch {
                    audioPlayer.play(nextItem.path) { onPrepareListener() }
                    EventBus.getDefault().post(ArtistChanged(NEXT_MEDIA))
                }
                binding?.included?.playingTrackTitle?.text = nextItem.name
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
            stop()
        }
        binding?.included?.playerAutoPlayButton?.setOnClickListener {
            autoPlayEnabled = !autoPlayEnabled

            preferences.edit()
                .putBoolean(autoPlayEnabledKey, autoPlayEnabled)
                .apply()

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
        EventBus.getDefault().post(ArtistChanged(STOP_MEDIA))
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
            val nextItem = playList!![position]
            updateUI(nextItem)
            viewModel.viewModelScope.launch {
                audioPlayer.play(nextItem.path) { onPrepareListener() }
                EventBus.getDefault().post(ArtistChanged(NEXT_MEDIA))
            }
            binding?.included?.playingTrackTitle?.text = nextItem.name
        }
    }


    fun previous() {
        if (position > 0) {
            --position
            val prevItem = playList!![position]
            updateUI(prevItem)
            viewModel.viewModelScope.launch {
                audioPlayer.play(prevItem.path) { onPrepareListener() }
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
        checkAutoPlayEnabled ()
        binding?.included?.playingTrackTitle?.text = artist.name
        binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        onPrepareListener()
    }

    fun setInitialPosition(position: Int) {
        this.position = position
    }
}