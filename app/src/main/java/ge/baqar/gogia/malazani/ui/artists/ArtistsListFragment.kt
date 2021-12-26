package ge.baqar.gogia.malazani.ui.artists

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ge.baqar.gogia.malazani.databinding.FragmentArtistsBinding
import ge.baqar.gogia.malazani.job.SyncFilesAndDatabaseJob
import ge.baqar.gogia.malazani.poko.events.OpenArtistFragment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import timber.log.Timber


class ArtistsListFragment : Fragment() {

    private val viewModel: ArtistsViewModel by inject()
    private var binding: FragmentArtistsBinding? = null
    private var _view: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_view == null) {
            binding = FragmentArtistsBinding.inflate(inflater, container, false)
            if (binding?.artistsListView?.adapter == null) {
                val action = if (arguments?.get("artistType")?.toString()?.equals("1") == true) {
                    EnsemblesRequested()
                } else {
                    OldRecordingsRequested()
                }
                val loadFlow = flowOf(action)
                initializeIntents(loadFlow)
            }
            binding?.include?.searchImageView?.setOnClickListener {

            }
            _view = binding?.root
            return _view!!
        }
        return _view!!
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun openArtistFragment(event: OpenArtistFragment) {
        findNavController().navigate(
            ge.baqar.gogia.malazani.R.id.navigation_artists_details,
            Bundle().apply {
                putParcelable("ensemble", event.ensemble)
            })
    }

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
            val errorId = resources.getIdentifier(state.error, "string", context?.packageName)
            val error = getString(errorId)
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            Timber.i(error)
        }
        if (state.isInProgress) {
            binding?.artistsProgressbar?.visibility = View.VISIBLE
            return
        }
        binding?.artistsProgressbar?.visibility = View.GONE

        SyncFilesAndDatabaseJob.triggerNow(requireContext())
        if (state.artists.count() > 0) {
            binding?.artistsListView?.adapter = ArtistsAdapter(state.artists) {
                openArtistFragment(OpenArtistFragment(it))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}