package ge.baqar.gogia.malazani.ui

import android.Manifest
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
import ge.baqar.gogia.malazani.media.MediaPlayerController
import ge.baqar.gogia.malazani.poko.AlazaniArtistListItem
import ge.baqar.gogia.malazani.poko.RequestMediaControllerInstance
import ge.baqar.gogia.malazani.poko.ServiceCreatedEvent
import ge.baqar.gogia.malazani.utility.permission.OnDenyPermissions
import ge.baqar.gogia.malazani.utility.permission.OnGrantPermissions
import ge.baqar.gogia.malazani.utility.permission.OnPermissionsFailure
import ge.baqar.gogia.malazani.utility.permission.RuntimePermissioner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@InternalCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
class MenuActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityMenuBinding
    private var mIsBound: Boolean = false
    private var permissioner: RuntimePermissioner? = null

    private val requestCode = 1994
    private var permissionCallback: (() -> Unit)? = null
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

        permissioner = RuntimePermissioner.builder()
            ?.requestCode(requestCode)
            ?.permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ?.callBack(object : OnGrantPermissions {
                override fun get(grantedPermissions: List<String>) {
                    permissionCallback?.invoke()
                }

            }, object : OnDenyPermissions {
                override fun get(deniedPermissions: List<String>) {

                }
            }, object : OnPermissionsFailure {
                override fun fail(e: Exception) {

                }
            })

        doBindService()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MediaPlayerController) {
        mediaController = event

        if (mediaController?.binding == null)
            mediaController?.binding = _binding

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

    fun playMediaPlayback(position: Int, songs: MutableList<AlazaniArtistListItem>) {
        mediaController?.dataSource = songs
        val intent = Intent(this, MediaPlaybackService::class.java).apply {
            action = MediaPlaybackService.PLAY_MEDIA
            putExtra("position", position)
        }
        startService(intent)
    }

    fun askForPermission(callback: () -> Unit) {
        permissionCallback = callback
        permissioner?.request(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissioner?.onPermissionsResult(requestCode, permissions, grantResults)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun doBindService() {
        if (!mIsBound) {
            val intent = Intent(this, MediaPlaybackService::class.java)
            startForegroundService(intent)
            mIsBound = true
        }
    }

    private fun doUnbindService() {
        if (mIsBound) {
            val intent = Intent(this, MediaPlaybackService::class.java)
            stopService(intent)
            mIsBound = false
        }
    }
}