package ge.baqar.gogia.malazani.media

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import androidx.appcompat.app.AlertDialog
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
import ge.baqar.gogia.malazani.ui.MenuActivity
import ge.baqar.gogia.malazani.ui.songs.SongsViewModel
import ge.baqar.gogia.malazani.utility.asDownloadable
import ge.baqar.gogia.model.AutoPlayState
import ge.baqar.gogia.model.Ensemble
import ge.baqar.gogia.model.Song
import ge.baqar.gogia.model.events.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import kotlin.time.ExperimentalTime


@ExperimentalTime
@InternalCoroutinesApi
class MediaPlayerController(
    private val viewModel: SongsViewModel,
    private val folkAppPreferences: FolkAppPreferences,
    private val audioPlayer: AudioPlayer,
    private val context: Context,
    private val activity: MenuActivity
) {
    var binding: ActivityMenuBinding? = null
    var ensemble: Ensemble? = null
    var playList: MutableList<Song>? = null

    private var position = 0
    private var autoPlayState = AutoPlayState.OFF
    private var timerSet = false

    private fun checkAutoPlayEnabled() {
        autoPlayState = folkAppPreferences.getAutoPlay()
        binding?.mediaPlayerView?.setAutoPlayState(autoPlayState)
    }

    private fun onPrepareListener() {
        val durationString = audioPlayer.getDurationString()
        val duration = audioPlayer.getDuration().toInt()
        binding?.mediaPlayerView?.setAutoPlayState(autoPlayState)
        binding?.mediaPlayerView?.setDuration(durationString, duration)
    }

    private fun listenAudioPlayerChanges(song: Song) {
        audioPlayer.listenPlayer {
            binding?.mediaPlayerView?.isPlaying(it)
        }
        audioPlayer.completed {
            binding?.mediaPlayerView?.setDuration(null, 0)
            binding?.mediaPlayerView?.setCurrentDuration(null)
            binding?.mediaPlayerView?.isPlaying(false)

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
                    binding?.mediaPlayerView?.setTrackTitle(repeatedSong.name, ensemble?.name)
                    binding?.mediaPlayerView?.isPlaying(true)
                }
                AutoPlayState.REPEAT_ALBUM -> {
                    next()
                }
            }
        }
        audioPlayer.updateTimeHandler { progress, time ->
            binding?.mediaPlayerView?.setProgress(time, progress.toInt())
        }
    }

    private fun viewListeners() {
        binding?.mediaPlayerView?.setSeekListener = { progress ->
            viewModel.viewModelScope.launch {
                audioPlayer.playOn(progress)
            }
        }

        binding?.mediaPlayerView?.onNext = {
            next()
        }
        binding?.mediaPlayerView?.onPrev = {
            previous()
        }
        binding?.mediaPlayerView?.onPlayPause = {
            if (audioPlayer.isPlaying()) {
                pause()
            } else {
                resume()
            }
        }
        binding?.mediaPlayerView?.onStop = {
            stop()
        }
        binding?.mediaPlayerView?.onAutoPlayChanged = {
            when (autoPlayState) {
                AutoPlayState.OFF -> {
                    autoPlayState = AutoPlayState.REPEAT_ALBUM
                }
                AutoPlayState.REPEAT_ALBUM -> {
                    autoPlayState = AutoPlayState.REPEAT_ONE
                }
                AutoPlayState.REPEAT_ONE -> {
                    autoPlayState = AutoPlayState.OFF
                }
            }
            folkAppPreferences.updateAutoPlay(autoPlayState)
            checkAutoPlayEnabled()
        }

        binding?.mediaPlayerView?.setOnCloseListener = {
            folkAppPreferences.setPlayerState(binding?.mediaPlayerView?.minimized!!)
        }
        binding?.mediaPlayerView?.openPlayListListener = {
            EventBus.getDefault().post(OpenArtistFragment(ensemble!!, getCurrentSong()))
            binding?.mediaPlayerView?.minimize()
        }

        timerSet = folkAppPreferences.getTimerSet()
        binding?.mediaPlayerView?.setTimer(timerSet)
        binding?.mediaPlayerView?.onTimerSetRequested = {
            timerSet = !timerSet
            folkAppPreferences.setTimerSet(timerSet)
            binding?.mediaPlayerView?.setTimer(timerSet)

            val array = arrayOf(context.resources.getString(R.string.unset), "5", "10", "30", "60")
            var selectedPosition = 0
            val dialog = AlertDialog.Builder(activity)
                .setTitle(R.string.app_name_georgian)
                .setSingleChoiceItems(
                    array, 0
                ) { _, position -> selectedPosition = position }
                .setPositiveButton(
                    context.resources.getString(R.string.set)
                ) { _, _ ->
                    val item = array[selectedPosition]
                    if (item == context.resources.getString(R.string.unset)) {
                        EventBus.getDefault().post(UnSetTimerEvent)
                    } else {
                        val time = item.toLong()
                        EventBus.getDefault().post(SetTimerEvent(time))
                    }
                }
                .create()
            dialog.show()
        }
        binding?.mediaPlayerView?.setFabButtonClickListener = {
            val currentSong = getCurrentSong()!!

            EzPermission.with(context)
                .permissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .request { granted, _, _ ->
                    if (granted.isNotEmpty()) {
                        viewModel.viewModelScope.launch {
                            var isFav = viewModel.isSongFav(currentSong.id)
                            val downloadableSongs = currentSong.asDownloadable()
                            if (!isFav) {
                                val intent = Intent(context, DownloadService::class.java).apply {
                                    action = DownloadService.DOWNLOAD_SONGS
                                    putExtra("ensemble", ensemble)
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
                                    putExtra("ensemble", ensemble)
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

    fun play() {
        playList?.let {
            val song = playList!![this.position]
            listenAudioPlayerChanges(song)
            viewListeners()
            viewModel.viewModelScope.launch {
                audioPlayer.play(song.path, song.data) { onPrepareListener() }
            }
            binding?.mediaPlayerView?.show()
            checkAutoPlayEnabled()
            binding?.mediaPlayerView?.setTrackTitle(song.name, ensemble?.name)
            updateFavouriteMarkFor(song)
        }
    }

    fun pause() {
        EventBus.getDefault().post(ArtistChanged(PAUSE_OR_MEDIA))
        audioPlayer.pause()
    }

    fun stop() {
        pause()
        EventBus.getDefault().post(ArtistChanged(STOP_MEDIA))
        binding?.mediaPlayerView?.setDuration(null, 0)
        binding?.mediaPlayerView?.setCurrentDuration(null)
        binding?.mediaPlayerView?.minimize()
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
            binding?.mediaPlayerView?.setTrackTitle(song.name, ensemble?.name)
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
            binding?.mediaPlayerView?.setTrackTitle(song.name, ensemble?.name)
            updateFavouriteMarkFor(song)
        }
    }

    private fun updateFavouriteMarkFor(song: Song?) {
        song?.let {
            binding?.mediaPlayerView?.setIsFav(song.isFav)
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

    private fun updateUI(song: Song) {
        viewListeners()
        binding?.mediaPlayerView?.let {
            it.visibility = View.VISIBLE
        }
        checkAutoPlayEnabled()
        binding?.mediaPlayerView?.setTrackTitle(song.name, ensemble?.name)
        binding?.mediaPlayerView?.isPlaying(true)
        onPrepareListener()
    }

    fun setInitialPosition(position: Int) {
        this.position = position
    }

    fun setCurrentSong(song: Song) {
        playList = mutableListOf(song)
        position = 0
        play()
        pause()
    }
}