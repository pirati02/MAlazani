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
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ge.baqar.gogia.malazani.databinding.FragmentArtistBinding
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
class ArtistFragment : Fragment() {

    @ExperimentalCoroutinesApi
    private val viewModel: ArtistViewModel by inject()
    private var _binding: FragmentArtistBinding? = null
    private var mediaPlayerController: MediaPlayerController? = null
    private val downloadManager: DownloadManager by lazy {
        activity?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    }

    private val binding get() = _binding!!

    @SuppressLint("UseRequireInsteadOfGet")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistBinding.inflate(inflater, container, false)
        val title = arguments?.get("title")?.toString()
        binding.tabTitleView.text = title
        val loadSongsAndChantsAction = flowOf(
            ArtistSongsRequested(arguments?.get("link").toString()),
            ArtistChantsRequested().apply {
                link = arguments?.get("link").toString()
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
        binding.tabBackView.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.downloadAlbumbtn.setOnClickListener {
            (activity as MenuActivity).let {
                it.askForPermission {
                    downloadAlbum(title)
                }
            }
        }
        (activity as MenuActivity).let {
            mediaPlayerController =
                MediaPlayerController(
                    it.audioPlayer,
                    it.binding,
                    viewModel,
                    mutableListOf()
                )
        }
        return binding.root
    }

    private fun downloadAlbum(title: String?) {
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
            request.setTitle("იწერება $title ${it.title}")
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "${title}-${it.title}.mp3"
            )
            downloadManager.enqueue(request)
        }
    }

    @FlowPreview
    @RequiresApi(Build.VERSION_CODES.M)
    fun initializeIntents(inputs: Flow<ArtistAction>) {
        viewModel.intents(inputs)
            .onEach { output ->
                when (output) {
                    is ArtistState -> render(output)
                }
            }
            .launchIn(lifecycleScope)
    }

    @RequiresApi(Build.VERSION_CODES.M)
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
            Timber.i(state.error)
            return
        }
        if (state is SongsState) {
            binding.songsProgressbar.visibility = View.GONE
            binding.songsListView.adapter = SongsAdapter(state.songs) { item, index ->
                mediaPlayerController?.dataSource = state.songs
                play(index)

            }
            binding.songsListView.visibility = View.VISIBLE
            binding.chantsListView.visibility = View.GONE

            if (state.songs.size == 0) {
                binding.chantsListView.visibility = View.VISIBLE
            }
        }
        if (state is ChantsState) {
            binding.chantsProgressbar.visibility = View.GONE
            binding.chantsListView.adapter = SongsAdapter(state.chants) { item, index ->
                mediaPlayerController?.dataSource = state.chants
                play(index)
            }
        }
    }

    fun play(position: Int) {
            mediaPlayerController?.play(position)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}