package ge.baqar.gogia.malazani.ui.search

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ge.baqar.gogia.malazani.databinding.FragmentSearchBinding
import ge.baqar.gogia.malazani.ui.MenuActivity
import ge.baqar.gogia.model.Ensemble
import ge.baqar.gogia.model.Song
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.koin.android.ext.android.inject
import reactivecircus.flowbinding.android.widget.textChanges
import timber.log.Timber
import kotlin.time.ExperimentalTime


@ExperimentalTime
@InternalCoroutinesApi
@FlowPreview
class SearchFragment : Fragment() {
    private val viewModel: SearchViewModel by inject()
    private var binding: FragmentSearchBinding? = null
    private val ime: InputMethodManager by lazy {
        context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        initializeIntents(binding?.searchTermInput?.textChanges()
            ?.debounce(500)
            ?.map { it.toString() }
            ?.map {
                if (it.length > 2)
                    DoSearch(it)
                else
                    ClearSearchResult
            }!!
        )

        binding?.searchInclude?.ensemblesSearchTab?.setOnClickListener {
            binding?.ensemblesSearchResultListView?.visibility = View.VISIBLE
            binding?.songsSearchResultListView?.visibility = View.GONE
        }
        binding?.searchInclude?.songsSearchTab?.setOnClickListener {
            binding?.ensemblesSearchResultListView?.visibility = View.GONE
            binding?.songsSearchResultListView?.visibility = View.VISIBLE
        }
        showKeyBoard()
        (activity as MenuActivity).destinationChanged = {
            if (it == javaClass.name) {
                showKeyBoard()
            } else {
                hideKeyBoard()
            }
        }
        return binding?.root!!
    }

    private fun hideKeyBoard() {
        ime.hideSoftInputFromWindow(view?.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun showKeyBoard() {
        binding?.searchTermInput?.post {
            binding?.searchTermInput?.requestFocus()
            ime.showSoftInput(binding?.searchTermInput, InputMethodManager.SHOW_IMPLICIT)
            binding?.searchTermInput?.requestFocus()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun initializeIntents(inputs: Flow<SearchAction>) {
        viewModel.intents(inputs)
            .onEach { output ->
                when (output) {
                    is SearchState -> render(output)
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun render(state: SearchState) {
        if (state.error != null) {
            val errorId = resources.getIdentifier(state.error, "string", context?.packageName)
            val error = getString(errorId)
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            binding?.searchProgressBar?.visibility = View.GONE
            Timber.i(error)
        }
        if (state.isInProgress) {
            binding?.searchProgressBar?.visibility = View.VISIBLE
            return
        }
        binding?.ensemblesSearchResultListView?.visibility = View.GONE
        binding?.songsSearchResultListView?.visibility = View.GONE

        state.result?.let {
            binding?.searchProgressBar?.visibility = View.GONE
            if (state.result.ensembles.isNotEmpty()) {
                binding?.ensemblesSearchResultListView?.adapter =
                    SearchedDataAdapter(state.result.ensembles) { _, ensemble ->
                        findNavController().navigate(
                            ge.baqar.gogia.malazani.R.id.navigation_artists_details,
                            Bundle().apply {
                                putParcelable("ensemble", ensemble)
                            })
                    }
                binding?.ensemblesSearchResultListView?.visibility = View.VISIBLE
            } else {
                binding?.searchInclude?.tabSeparator?.visibility = View.GONE
                binding?.searchInclude?.ensemblesSearchTab?.visibility = View.GONE
            }

            if (state.result.songs.isNotEmpty()) {
                binding?.songsSearchResultListView?.adapter =
                    SearchedDataAdapter(state.result.songs) { position, song ->
                        viewModel.ensembleById(song.ensembleId) { ensemble ->
                            ensemble?.let {
                                play(position, ensemble, state.result.songs)
                            }
                        }
                    }
                if (state.result.ensembles.isEmpty()) {
                    binding?.songsSearchResultListView?.visibility = View.VISIBLE
                } else {
                    binding?.songsSearchResultListView?.visibility = View.GONE
                }
            } else {
                binding?.searchInclude?.tabSeparator?.visibility = View.GONE
                binding?.searchInclude?.songsSearchTab?.visibility = View.GONE
            }
        }
    }

    private fun play(position: Int, ensemble: Ensemble, songs: MutableList<Song>) {
        (activity as MenuActivity).playMediaPlayback(position, songs, ensemble)
    }
}