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
import ge.baqar.gogia.malazani.job.SyncFilesAndDatabaseJob
import ge.baqar.gogia.malazani.media.MediaPlaybackService
import ge.baqar.gogia.malazani.media.MediaPlaybackServiceManager
import ge.baqar.gogia.malazani.media.MediaPlayerController
import ge.baqar.gogia.model.Ensemble
import ge.baqar.gogia.model.Song
import ge.baqar.gogia.model.events.RequestMediaControllerInstance
import ge.baqar.gogia.model.events.ServiceCreatedEvent
import kotlinx.coroutines.InternalCoroutinesApi
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.time.ExperimentalTime


@InternalCoroutinesApi
@ExperimentalTime
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
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
                R.id.navigation_oldRecordings,
                R.id.navigation_search,
                R.id.navigation_favs
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
        SyncFilesAndDatabaseJob.triggerNow(this)
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
        mediaPlayerController?.binding = _binding

        if (_playbackRequest) {
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
            mediaPlayerController?.updatePlayer()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun serviceCreated(event: ServiceCreatedEvent) {
        EventBus.getDefault().post(RequestMediaControllerInstance())
    }

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

    private fun doBindService() {
        val intent = Intent(this, MediaPlaybackService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent);
        }

        if (mediaPlayerController == null)
            EventBus.getDefault().postSticky(RequestMediaControllerInstance())
    }

    private fun doUnbindService() {
        val intent = Intent(this, MediaPlaybackService::class.java)
        stopService(intent)
    }
}