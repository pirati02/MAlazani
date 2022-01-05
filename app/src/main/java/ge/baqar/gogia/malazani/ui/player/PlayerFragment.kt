package ge.baqar.gogia.malazani.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import ge.baqar.gogia.malazani.databinding.FragmentPlayerBinding
import ge.baqar.gogia.malazani.ui.menuActivity
import ge.baqar.gogia.model.Ensemble
import ge.baqar.gogia.model.Song
import kotlinx.coroutines.InternalCoroutinesApi
import kotlin.time.ExperimentalTime

@InternalCoroutinesApi
@ExperimentalTime
class PlayerFragment : Fragment() {
    private lateinit var binding: FragmentPlayerBinding
    private val ensemble: Ensemble by lazy {
        menuActivity().mediaPlayerController?.ensemble!!
    }
    private val playList: MutableList<Song> by lazy {
        menuActivity().mediaPlayerController?.playList!!
    }
    private val currentSong: Song by lazy {
        menuActivity().mediaPlayerController?.getCurrentSong()!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater
                .from(context)
                .inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        binding.playingTrackTitle.text = currentSong.name
        binding.playerViewCloseBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }
}