package ge.baqar.gogia.malazani.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.databinding.ActivityMenuBinding
import ge.baqar.gogia.malazani.media.MediaPlaybackService
import ge.baqar.gogia.malazani.media.MediaPlaybackServiceManager
import ge.baqar.gogia.malazani.media.MediaPlayerController
import ge.baqar.gogia.malazani.media.RequestMediaControllerInstance
import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.events.ServiceCreatedEvent
import ge.baqar.gogia.malazani.poko.Song
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@InternalCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
@RequiresApi(Build.VERSION_CODES.O)
class MenuActivity : AppCompatActivity() {

    private var tempEnsemble: Ensemble? = null
    private var tempDataSource: MutableList<Song>? = null
    private var tempPosition: Int? = null

    private var _playbackRequest: Boolean = false
    private var _playMediaPlaybackAction: ((MutableList<Song>, Int, Ensemble) -> Unit)? =
        { songs, position, ensemble ->
            mediaPlayerController?.playList = songs
            mediaPlayerController?.playListEnsemble = ensemble
            mediaPlayerController?.setInitialPosition(position)
            val intent = Intent(this, MediaPlaybackService::class.java).apply {
                action = MediaPlaybackService.PLAY_MEDIA
            }
            startForegroundService(intent)
        }
    private lateinit var _binding: ActivityMenuBinding
    private lateinit var navController: NavController
    var mediaPlayerController: MediaPlayerController? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        supportActionBar?.hide()
        navController = findNavController(R.id.nav_host_fragment_activity_menu)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_ensembles,
                R.id.navigation_oldRecordings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        _binding.navView.setupWithNavController(navController)

        if (MediaPlaybackServiceManager.isRunning)
            doBindService()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!MediaPlaybackServiceManager.isRunning)
            doUnbindService()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMessageEvent(event: MediaPlayerController) {
        mediaPlayerController = event

        if (mediaPlayerController?.binding == null)
            mediaPlayerController?.binding = _binding

        if (_playbackRequest) {
            mediaPlayerController?.binding = _binding
            _playMediaPlaybackAction?.invoke(
                tempDataSource!!,
                tempPosition!!,
                tempEnsemble!!
            )
            tempDataSource = null
            tempPosition = null
            _playbackRequest = false
            return
        }
        if (mediaPlayerController?.isPlaying() == true) {
            mediaPlayerController?.binding = _binding
            mediaPlayerController?.updatePlayer()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun serviceCreated(event: ServiceCreatedEvent) {
        EventBus.getDefault().post(RequestMediaControllerInstance())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun playMediaPlayback(position: Int, songs: MutableList<Song>, ensemble: Ensemble) {
        if (mediaPlayerController != null) {
            _playMediaPlaybackAction?.invoke(songs, position, ensemble)
        } else {
            tempDataSource = songs
            tempPosition = position
            tempEnsemble = ensemble
            _playbackRequest = true
            doBindService()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun doBindService() {
        val intent = Intent(this, MediaPlaybackService::class.java)
        startForegroundService(intent)

        if (mediaPlayerController == null)
            EventBus.getDefault().postSticky(RequestMediaControllerInstance())
    }

    private fun doUnbindService() {
        val intent = Intent(this, MediaPlaybackService::class.java)
        stopService(intent)
    }
}