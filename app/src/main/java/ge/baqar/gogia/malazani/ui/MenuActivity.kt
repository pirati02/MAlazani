package ge.baqar.gogia.malazani.ui

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.databinding.ActivityMenuBinding
import ge.baqar.gogia.malazani.utility.player.AudioPlayer
import org.koin.android.ext.android.inject
import org.koin.java.KoinJavaComponent.inject

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    val audioPlayer: AudioPlayer by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_menu)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
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
            if (audioPlayer.isPlaying()) {
                audioPlayer.pause()
            } else {
                audioPlayer.resume()
            }
        }
    }
}