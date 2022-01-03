package ge.baqar.gogia.malazani.ui.favourites

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import ge.baqar.gogia.malazani.databinding.FragmentFavouritesBinding
import ge.baqar.gogia.malazani.ui.MenuActivity
import ge.baqar.gogia.model.ArtistType
import ge.baqar.gogia.model.Ensemble
import ge.baqar.gogia.model.Song
import ge.baqar.gogia.model.events.SongsMarkedAsFavourite
import ge.baqar.gogia.model.events.SongsUnmarkedAsFavourite
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import kotlin.time.ExperimentalTime

@ExperimentalTime
@InternalCoroutinesApi
class FavouritesFragment : Fragment() {
    private val viewModel: FavouritesViewModel by inject()
    private var binding: FragmentFavouritesBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        initializeIntents(flowOf(FavouritesList()))
        return binding?.root!!
    }

    private fun initializeIntents(inputs: Flow<FavouriteAction>) {
        viewModel.intents(inputs)
            .onEach { output ->
                when (output) {
                    is FavouriteState -> render(output)
                }
            }
            .launchIn(lifecycleScope)
    }


    @SuppressLint("NotifyDataSetChanged")
    @Subscribe
    fun songsMarkedAsFavourite(event: SongsMarkedAsFavourite) {
        initializeIntents(flowOf(FavouritesList()))
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe
    fun songsMarkedAsFavourite(event: SongsUnmarkedAsFavourite) {
        initializeIntents(flowOf(FavouritesList()))
    }

    private fun render(state: FavouriteState) {
        if (state.isInProgress) {
            binding?.favsProgressBar?.visibility = View.VISIBLE
            return
        }
        if (state.favSongs.isNotEmpty()) {
            binding?.noRecordsView?.visibility = View.GONE
            binding?.favsProgressBar?.visibility = View.GONE
            binding?.favSongsListView?.adapter = FavouritesAdapter(state.favSongs) {
                play(
                    Ensemble(it.ensembleId, it.ensembleName, "", ArtistType.ENSEMBLE, true),
                    it
                )
            }
            binding?.favSongsListView?.visibility = View.VISIBLE
        } else {
            binding?.noRecordsView?.visibility = View.VISIBLE
        }
    }

    private fun play(ensemble: Ensemble, song: Song) {
        (activity as MenuActivity).playMediaPlayback(0, mutableListOf(song), ensemble)
    }
}