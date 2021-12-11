package ge.baqar.gogia.malazani.ui.artist

import ArtistAction
import ArtistChantsRequested
import ArtistSongsRequested
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ge.baqar.gogia.malazani.databinding.FragmentArtistBinding
import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.Song
import ge.baqar.gogia.malazani.ui.MenuActivity
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
@RequiresApi(Build.VERSION_CODES.O)
class ArtistFragment : Fragment() {

    private var _ensemble: Ensemble? = null

    @ExperimentalCoroutinesApi
    private val viewModel: ArtistViewModel by inject()
    private var _binding: FragmentArtistBinding? = null
    private val downloadManager: DownloadManager by lazy {
        activity?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    }

    private val binding get() = _binding!!

    @SuppressLint("UseRequireInsteadOfGet")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistBinding.inflate(inflater, container, false)
        _ensemble = arguments?.getParcelable("ensemble")
        binding.tabViewInclude.tabTitleView.text = _ensemble?.title
        val loadSongsAndChantsAction = flowOf(
            ArtistSongsRequested(_ensemble?.link!!),
            ArtistChantsRequested().apply {
                link = _ensemble?.link!!
            })
        initializeIntents(loadSongsAndChantsAction)

        binding.artistSongsTab.setOnClickListener {
            binding.songsListView.visibility = View.VISIBLE
            binding.chantsListView.visibility = View.GONE
        }

        binding.artistChantsTab.setOnClickListener {
            binding.chantsListView.visibility = View.VISIBLE
            binding.songsListView.visibility = View.GONE
        }
        binding.tabViewInclude.tabBackImageView.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.downloadAlbumbtn.setOnClickListener {
            downloadAlbum()
        }
        return binding.root
    }

    private fun downloadAlbum() {
        val songsDataSource = binding.songsListView.adapter as? SongsAdapter
        val chantsDataSource = binding.chantsListView.adapter as? SongsAdapter
        val album = songsDataSource?.dataSource
        album?.addAll(chantsDataSource?.dataSource!!)
        album?.map {
            val downloadUri: Uri =
                Uri.parse(viewModel.formatUrl(it.link))
            val request = DownloadManager.Request(downloadUri)

            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setAllowedOverRoaming(false)
            request.setTitle("იწერება ${_ensemble?.link} ${it.title}")
            request.setDestinationInExternalPublicDir(
                "${Environment.DIRECTORY_DOWNLOADS}/${_ensemble?.title}/",
                "${_ensemble?.link}-${it.title}.mp3"
            )
            downloadManager.enqueue(request)
        }
    }

    @FlowPreview
    @RequiresApi(Build.VERSION_CODES.O)
    fun initializeIntents(inputs: Flow<ArtistAction>) {
        viewModel.intents(inputs)
            .onEach { output ->
                when (output) {
                    is ArtistState -> render(output)
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun render(state: ArtistState) {
        if (state.isInProgress) {
            if (state is SongsState) {
                binding.songsListView.visibility = View.GONE
                binding.songsProgressbar.visibility = View.VISIBLE
            }
            if (state is ChantsState) {
                binding.chantsListView.visibility = View.GONE
                binding.chantsProgressbar.visibility = View.VISIBLE
            }
            return
        }
        if (state.error != null) {
            val errorId = resources.getIdentifier(state.error, "string", context?.packageName)
            val error = getString(errorId)
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            Timber.i(error)
            return
        }
        if (state is SongsState) {
            if (state.songs.size > 0) {
                binding.songsProgressbar.visibility = View.GONE
                binding.songsListView.adapter = SongsAdapter(state.songs) { _, index ->
                    play(index, state.songs)

                }
                binding.songsListView.visibility = View.VISIBLE
                binding.chantsListView.visibility = View.GONE
            } else {
                binding.chantsListView.visibility = View.VISIBLE
                binding.songsListView.visibility = View.GONE
                binding.songsProgressbar.visibility = View.GONE
                binding.songsListView.visibility = View.GONE
                binding.artistSongsTab.visibility = View.GONE
                binding.tabSeparator.visibility = View.GONE
            }
        }
        if (state is ChantsState) {
            if (state.chants.size > 0) {
                binding.chantsProgressbar.visibility = View.GONE
                binding.chantsListView.adapter = SongsAdapter(state.chants) { _, index ->
                    play(index, state.chants)
                }
            } else {
                binding.chantsProgressbar.visibility = View.GONE
                binding.chantsListView.visibility = View.GONE
                binding.artistChantsTab.visibility = View.GONE
                binding.tabSeparator.visibility = View.GONE
            }
        }
    }

    private fun play(position: Int, songs: MutableList<Song>) {
        (activity as MenuActivity).playMediaPlayback(position, songs, _ensemble!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}