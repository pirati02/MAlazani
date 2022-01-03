package ge.baqar.gogia.malazani.media

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.viewModelScope
import com.androidisland.ezpermission.EzPermission
import ge.baqar.gogia.db.FolkAppPreferences
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.databinding.ActivityMenuBinding
import ge.baqar.gogia.malazani.media.MediaPlaybackService.Companion.NEXT_MEDIA
import ge.baqar.gogia.malazani.media.MediaPlaybackService.Companion.PAUSE_OR_MEDIA
import ge.baqar.gogia.malazani.media.MediaPlaybackService.Companion.PLAY_MEDIA
import ge.baqar.gogia.malazani.media.MediaPlaybackService.Companion.PREV_MEDIA
import ge.baqar.gogia.malazani.media.MediaPlaybackService.Companion.STOP_MEDIA
import ge.baqar.gogia.malazani.media.player.AudioPlayer
import ge.baqar.gogia.malazani.storage.DownloadService
import ge.baqar.gogia.malazani.ui.songs.SongsViewModel
import ge.baqar.gogia.model.AutoPlayState
import ge.baqar.gogia.model.DownloadableSong
import ge.baqar.gogia.model.Ensemble
import ge.baqar.gogia.model.Song
import ge.baqar.gogia.model.events.ArtistChanged
import ge.baqar.gogia.model.events.OpenArtistFragment
import ge.baqar.gogia.model.events.SongsMarkedAsFavourite
import ge.baqar.gogia.model.events.SongsUnmarkedAsFavourite
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import kotlin.time.ExperimentalTime

@ExperimentalTime
@InternalCoroutinesApi
class MediaPlayerController(
    private val viewModel: SongsViewModel,
    private val folkAppPreferences: FolkAppPreferences,
    private val audioPlayer: AudioPlayer,
    private val context: Context
) {
    var binding: ActivityMenuBinding? = null
    var playListEnsemble: Ensemble? = null
    var playList: MutableList<Song>? = null

    private var position = 0
    private var autoPlayState = AutoPlayState.OFF
    private var playerControlsAreVisible = true

    fun play() {
        playList?.let {
            val song = playList!![this.position]
            viewListeners()
            listenAudioPlayerChanges(song)
            binding?.included?.mediaPlayerView?.let {
                it.visibility = View.VISIBLE
            }
            checkAutoPlayEnabled()
            binding?.included?.playingTrackTitle?.text = song.name
            binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            if (song.isFav) {
                binding?.included?.favBtn?.setImageResource(R.drawable.ic_baseline_favorite_24)
            } else {
                binding?.included?.favBtn?.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            }
            updateFavouriteMarkFor(song)
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
            audioPlayer.play(song.path, song.data) { onPrepareListener() }
        }
        audioPlayer.completed {
            binding?.included?.playingTrackTime?.text = null
            binding?.included?.playerProgressBar?.progress = 0
            binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
            binding?.included?.playingTrackDurationTime?.text = null

            when (autoPlayState) {
                AutoPlayState.OFF -> {
                    stop()
                    return@completed
                }
                AutoPlayState.REPEAT_ONE -> {
                    val repeatedSong = playList!![position]
                    viewModel.viewModelScope.launch {
                        audioPlayer.play(
                            repeatedSong.path,
                            repeatedSong.data
                        ) { onPrepareListener() }
                        EventBus.getDefault().post(ArtistChanged(PLAY_MEDIA))
                    }
                    binding?.included?.playingTrackTitle?.text = repeatedSong.name
                    binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                }
                AutoPlayState.REPEAT_ALBUM -> {
                    if ((position + 1) < playList?.size!!) {
                        ++position
                        val nextSong = playList!![position]
                        viewModel.viewModelScope.launch {
                            audioPlayer.play(nextSong.path, nextSong.data) { onPrepareListener() }
                            EventBus.getDefault().post(ArtistChanged(NEXT_MEDIA))
                        }
                        binding?.included?.playingTrackTitle?.text = nextSong.name
                        binding?.included?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                    } else {
                        stop()
                    }
                }
            }
        }
        audioPlayer.updateTimeHandler { progress, time ->
            binding?.included?.playingTrackTime?.text = time
            binding?.included?.playerProgressBar?.progress = progress?.toInt()!!
        }
    }

    private fun viewListeners() {
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
        binding?.included?.favBtn?.setOnClickListener {
            val currentSong = getCurrentSong()!!

            EzPermission.with(context)
                .permissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .request { granted, _, _ ->
                    if (granted.isNotEmpty()) {
                        viewModel.viewModelScope.launch {
                            var isFav = viewModel.isSongFav(currentSong.id)
                            val downloadableSongs = DownloadableSong(
                                currentSong.id,
                                currentSong.name,
                                currentSong.nameEng,
                                currentSong.path,
                                currentSong.songType,
                                currentSong.ensembleId
                            )
                            if (!isFav) {
                                val intent = Intent(context, DownloadService::class.java).apply {
                                    action = DownloadService.DOWNLOAD_SONGS
                                    putExtra("ensemble", playListEnsemble)
                                    putParcelableArrayListExtra(
                                        "songs",
                                        arrayListOf(downloadableSongs)
                                    )
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    context.startForegroundService(intent)
                                } else {
                                    context.startService(intent)
                                }
                            } else {
                                val intent = Intent(context, DownloadService::class.java).apply {
                                    action = DownloadService.STOP_DOWNLOADING
                                    putExtra("ensemble", playListEnsemble)
                                    putParcelableArrayListExtra(
                                        "songs",
                                        arrayListOf(downloadableSongs)
                                    )
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    context.startForegroundService(intent)
                                } else {
                                    context.startService(intent)
                                }

                                updateFavouriteMarkFor(currentSong.also {
                                    isFav = false
                                })
                            }
                        }
                    }
                }
        }
    }

    fun songsMarkedAsFavourite(event: SongsMarkedAsFavourite) {
        val ids = event.songs
        playList?.filter { event.songs.map { it.id }.contains(it.id) }?.forEach {
            it.isFav = ids.map { it.id }.contains(it.id)
        }
        updateFavouriteMarkFor(getCurrentSong())
    }

    fun songsUnMarkedAsFavourite(event: SongsUnmarkedAsFavourite) {
        val ids = event.songs
        playList?.filter { event.songs.map { it.id }.contains(it.id) }?.forEach {
            it.isFav = !ids.map { it.id }.contains(it.id)
        }
        updateFavouriteMarkFor(getCurrentSong())
    }

    fun pause() {
        EventBus.getDefault().post(ArtistChanged(PAUSE_OR_MEDIA))
        audioPlayer.pause()
    }

    fun stop() {
        EventBus.getDefault().post(ArtistChanged(STOP_MEDIA))
        audioPlayer.release()
        binding?.included?.playingTrackTime?.text = null
        binding?.included?.playerProgressBar?.progress = 0
        binding?.included?.playingTrackDurationTime?.text = null
        binding?.included?.mediaPlayerView?.let {
            it.visibility = View.GONE
        }
    }

    fun resume() {
        EventBus.getDefault().post(ArtistChanged(PAUSE_OR_MEDIA))
        audioPlayer.resume()
    }

    fun next() {
        if ((position + 1) < playList?.size!!) {
            ++position
            val song = playList!![position]
            updateUI(song)
            viewModel.viewModelScope.launch {
                audioPlayer.play(song.path, song.data) { onPrepareListener() }
                EventBus.getDefault().post(ArtistChanged(NEXT_MEDIA))
            }
            binding?.included?.playingTrackTitle?.text = song.name
            updateFavouriteMarkFor(song)
        }
    }


    fun previous() {
        if (position > 0) {
            --position
            val song = playList!![position]
            updateUI(song)
            viewModel.viewModelScope.launch {
                audioPlayer.play(song.path, song.data) { onPrepareListener() }
                EventBus.getDefault().post(ArtistChanged(PREV_MEDIA))
            }
            binding?.included?.playingTrackTitle?.text = song.name
            updateFavouriteMarkFor(song)
        }
    }

    private fun updateFavouriteMarkFor(song: Song?) {
       song?.let{
           if (song.isFav) {
               binding?.included?.favBtn?.setImageResource(R.drawable.ic_baseline_favorite_24)
           } else {
               binding?.included?.favBtn?.setImageResource(R.drawable.ic_baseline_favorite_border_24)
           }
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
        viewListeners()
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