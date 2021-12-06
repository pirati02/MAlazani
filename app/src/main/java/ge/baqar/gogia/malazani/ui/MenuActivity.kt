package ge.baqar.gogia.malazani.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.databinding.ActivityMenuBinding
import ge.baqar.gogia.malazani.utility.player.AudioPlayer
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MenuActivity : AppCompatActivity() {

    lateinit var binding: ActivityMenuBinding
    val audioPlayer: AudioPlayer by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_menu)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_ensembles,
                R.id.navigation_states,
                R.id.navigation_oldRecordings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        binding.playPauseButton.setOnClickListener {
            lifecycleScope.launch {
                if (audioPlayer.isPlaying()) {
                    audioPlayer.pause()
                } else {
                    audioPlayer.resume()
                }
            }
        }
    }
}