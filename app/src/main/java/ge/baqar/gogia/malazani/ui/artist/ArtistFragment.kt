package ge.baqar.gogia.malazani.ui.artist

import ArtistAction
import ArtistChantsRequested
import ArtistSongsRequested
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.databinding.FragmentArtistBinding
import ge.baqar.gogia.malazani.poko.AlazaniArtistListItem
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

    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistBinding.inflate(inflater, container, false)
        binding.tabTitleView.text = arguments?.get("title")?.toString()
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
        return binding.root
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
            binding.songsListView.adapter = SongsAdapter(state.songs) {
                play(it)
            }
            binding.songsListView.visibility = View.VISIBLE
            binding.chantsListView.visibility = View.GONE

            if (state.songs.size == 0) {
                binding.chantsListView.visibility = View.VISIBLE
            }
        }
        if (state is ChantsState) {
            binding.chantsProgressbar.visibility = View.GONE
            binding.chantsListView.adapter = SongsAdapter(state.chants) {
                play(it)
            }
        }
    }

    fun play(artist: AlazaniArtistListItem) {
        (activity as MenuActivity).let {
            val mediaPlayerView = it.findViewById<LinearLayoutCompat>(R.id.mediaPlayerView)
            val playBtn = it.findViewById<AppCompatImageView>(R.id.playPauseButton)
            mediaPlayerView?.let {
                it.visibility = View.VISIBLE
            }
            it.findViewById<AppCompatTextView>(R.id.playingTrackTitle).text = artist.title
            it.audioPlayer.play(viewModel.formatUrl(artist.link))
            it.audioPlayer.listenPlayer {
                if (it) {
                    playBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                } else {
                    playBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}