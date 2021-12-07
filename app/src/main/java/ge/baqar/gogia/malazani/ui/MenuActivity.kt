package ge.baqar.gogia.malazani.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
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
import ge.baqar.gogia.malazani.utility.permission.OnDenyPermissions
import ge.baqar.gogia.malazani.utility.permission.OnGrantPermissions
import ge.baqar.gogia.malazani.utility.permission.OnPermissionsFailure
import ge.baqar.gogia.malazani.utility.permission.RuntimePermissioner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
class MenuActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityMenuBinding
    private var mIsBound: Boolean = false
    private var permissioner: RuntimePermissioner? = null

    private val REQUEST_CODE = 1994
    private var permissionCallback: (() -> Unit)? = null
    private var mediaController: MediaPlayerController? = null

    @FlowPreview
    @ExperimentalCoroutinesApi
    private val mConnection: ServiceConnection = object : ServiceConnection {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val boundedService =
                (service as MediaPlaybackService.LocalBinder).service
            mediaController = boundedService.mediaPlayerController
            if (mediaController?.binding == null)
                mediaController?.binding = _binding
        }

        override fun onServiceDisconnected(className: ComponentName) {

        }
    }

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
            ?.requestCode(REQUEST_CODE)
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
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
    }


    fun setDataSource(songs: MutableList<AlazaniArtistListItem>) {
        mediaController?.dataSource = songs
    }

    fun playMediaPlayback(position: Int) {
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
        val intent = Intent(this, MediaPlaybackService::class.java)
        startForegroundService(intent)
        bindService(
            intent,
            mConnection,
            Context.BIND_AUTO_CREATE
        )
        mIsBound = true
    }

    private fun doUnbindService() {
        if (mIsBound) {
            val intent = Intent(this, MediaPlaybackService::class.java)
//            stopService(intent)
            unbindService(mConnection)
            mIsBound = false
        }
    }
}