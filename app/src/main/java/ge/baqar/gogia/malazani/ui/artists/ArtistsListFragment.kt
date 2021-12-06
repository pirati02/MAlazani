package ge.baqar.gogia.malazani.ui.artists

import ArtistsAction
import ArtistsRequested
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.databinding.FragmentArtistsBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import timber.log.Timber

@InternalCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
class ArtistsListFragment : Fragment() {

    @ExperimentalCoroutinesApi
    private val viewModel: ArtistsViewModel by inject()
    private var _binding: FragmentArtistsBinding? = null

    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistsBinding.inflate(inflater, container, false)
        val li = arguments?.get("link").toString().toInt()
        val url = getString(li)
        val loadFlow = flowOf(ArtistsRequested(url!!))
        initializeIntents(loadFlow)
        return binding.root
    }


    @FlowPreview
    @RequiresApi(Build.VERSION_CODES.M)
    fun initializeIntents(inputs: Flow<ArtistsAction>) {
        viewModel.intents(inputs)
            .onEach { output ->
                when (output) {
                    is ArtistsState -> render(output)
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun render(state: ArtistsState) {
        if (state.error != null) {
            Timber.i(state.error)
        }

        if (state.Artists.count() > 0) {
            binding.artistsListView.adapter = ArtistsAdapter(state.Artists) {
                findNavController().navigate(R.id.navigation_artists_details, Bundle().apply {
                    putString("link", it.link)
                    putString("title", it.title)
                })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}