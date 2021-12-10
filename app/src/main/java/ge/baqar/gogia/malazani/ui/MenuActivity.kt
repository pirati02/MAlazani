package ge.baqar.gogia.malazani.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.databinding.ActivityMenuBinding
import ge.baqar.gogia.malazani.media.MediaPlaybackService
import ge.baqar.gogia.malazani.media.MediaPlaybackServiceManager
import ge.baqar.gogia.malazani.media.MediaPlayerController
import ge.baqar.gogia.malazani.poko.AlazaniArtistListItem
import ge.baqar.gogia.malazani.poko.RequestMediaControllerInstance
import ge.baqar.gogia.malazani.poko.ServiceCreatedEvent
import ge.baqar.gogia.malazani.utility.ServiceUtils
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

    private var tempDataSource: MutableList<AlazaniArtistListItem>? = null
    private var tempPosition: Int? = null
    private var _playbackRequest: Boolean = false
    private var _playMediaPlaybackAction: ((MutableList<AlazaniArtistListItem>, Int) -> Unit)? =
        { songs, position ->
            mediaController?.dataSource = songs
            val intent = Intent(this, MediaPlaybackService::class.java).apply {
                action = MediaPlaybackService.PLAY_MEDIA
                putExtra("position", position)
            }
            startForegroundService(intent)
        }
    private lateinit var _binding: ActivityMenuBinding
    private var mediaController: MediaPlayerController? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        supportActionBar?.hide()
        val navController = findNavController(R.id.nav_host_fragment_activity_menu)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_ensembles,
//                R.id.navigation_states,
                R.id.navigation_oldRecordings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        _binding.navView.setupWithNavController(navController)

        if (MediaPlaybackServiceManager.isRunning)
            doBindService()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MediaPlayerController) {
        mediaController = event

        if (mediaController?.binding == null)
            mediaController?.binding = _binding

        if (_playbackRequest) {
            mediaController?.binding = _binding
            _playMediaPlaybackAction?.invoke(
                tempDataSource!!,
                tempPosition!!
            )
            tempDataSource = null
            tempPosition = null
            _playbackRequest = false
        }
        if (mediaController?.isPlaying() == true) {
            mediaController?.binding = _binding
            mediaController?.updatePlayer()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun serviceCreated(event: ServiceCreatedEvent) {
        EventBus.getDefault().post(RequestMediaControllerInstance())
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun playMediaPlayback(position: Int, songs: MutableList<AlazaniArtistListItem>) {
        if (mediaController != null) {
            _playMediaPlaybackAction?.invoke(songs, position)
        } else {
            tempDataSource = songs
            tempPosition = position
            _playbackRequest = true
            doBindService()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun doBindService() {
        val intent = Intent(this, MediaPlaybackService::class.java)
        startForegroundService(intent)
    }

    private fun doUnbindService() {
        val intent = Intent(this, MediaPlaybackService::class.java)
        stopService(intent)
    }
}