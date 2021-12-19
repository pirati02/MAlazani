package ge.baqar.gogia.malazani.ui.artist

import android.annotation.SuppressLint
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
import ge.baqar.gogia.malazani.databinding.FragmentArtistBinding
import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.Song
import ge.baqar.gogia.malazani.poko.SongType
import ge.baqar.gogia.malazani.poko.events.CurrentPlayingSong
import ge.baqar.gogia.malazani.poko.events.GetCurrentSong
import ge.baqar.gogia.malazani.ui.MenuActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
@RequiresApi(Build.VERSION_CODES.O)
class ArtistFragment : Fragment() {

    private var _currentSong: Song? = null
    private var _ensemble: Ensemble? = null

    private val viewModel: ArtistViewModel by inject()
    private val albumDownloadManager: AlbumDownloadManager by inject()
    private var binding: FragmentArtistBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentArtistBinding.inflate(inflater, container, false)
        _ensemble = arguments?.getParcelable("ensemble")
        binding?.toolbarInclude?.tabTitleView?.text = _ensemble?.name

        val loadSongsAndChantsAction = flowOf(
            ArtistSongsRequested(_ensemble?.copy()!!),
        )
        initializeIntents(inputs(loadSongsAndChantsAction))

        initializeClickListeners()
        EventBus.getDefault().post(GetCurrentSong)
        return binding?.root!!
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun inputs(vararg actions: Flow<ArtistSongsRequested>): Flow<ArtistAction> = merge(
        *actions
    )

    private fun initializeClickListeners() {
        binding?.tabViewInclude?.artistSongsTab?.setOnClickListener {
            binding?.songsListView?.visibility = View.VISIBLE
            binding?.chantsListView?.visibility = View.GONE
        }

        binding?.tabViewInclude?.artistChantsTab?.setOnClickListener {
            binding?.chantsListView?.visibility = View.VISIBLE
            binding?.songsListView?.visibility = View.GONE
        }
        binding?.toolbarInclude?.tabBackImageView?.setOnClickListener {
            findNavController().navigateUp()
        }
        binding?.toolbarInclude?.enableOfflineMode?.setOnCheckedChangeListener { _, enabled ->
            if (enabled) {
                val songsDataSource = binding?.songsListView?.adapter as? SongsAdapter
                val chantsDataSource = binding?.chantsListView?.adapter as? SongsAdapter
                albumDownloadManager.setDownloadData(
                    _ensemble!!,
                    songsDataSource?.dataSource!!,
                    chantsDataSource?.dataSource!!
                )
                albumDownloadManager.download()
            } else {
                albumDownloadManager.clearDownloads()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun currentPlayingSong(event: CurrentPlayingSong?) {
        _currentSong = event?.song
        _currentSong?.let {
            if (_currentSong?.songType == SongType.Song) {
                (binding?.songsListView?.adapter as? SongsAdapter)?.apply {
                    applyNotPlayingState()
                    dataSource.firstOrNull { it.id == _currentSong?.id }?.isPlaying = true
                }
                (binding?.chantsListView?.adapter as? SongsAdapter)?.apply {
                    applyNotPlayingState()
                }
            } else {
                (binding?.chantsListView?.adapter as? SongsAdapter)?.apply {
                    applyNotPlayingState()
                    dataSource.firstOrNull { it.id == _currentSong?.id }?.isPlaying = true
                }
                (binding?.songsListView?.adapter as? SongsAdapter)?.apply {
                    applyNotPlayingState()
                }
            }
        }
    }

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

    @SuppressLint("NotifyDataSetChanged")
    private fun render(state: ArtistState) {
        if (state.isInProgress) {
            if (state is SongsState) {
                binding?.songsListView?.visibility = View.GONE
                binding?.songsProgressbar?.visibility = View.VISIBLE
            }
            if (state is ChantsState) {
                binding?.chantsListView?.visibility = View.GONE
                binding?.chantsProgressbar?.visibility = View.VISIBLE
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
                binding?.songsProgressbar?.visibility = View.GONE
                currentPlayingSong(CurrentPlayingSong(_currentSong))
                binding?.songsListView?.adapter = SongsAdapter(state.songs) { song, index ->
                    play(index, state.songs)
                    currentPlayingSong(CurrentPlayingSong(song))
                }
                binding?.songsListView?.visibility = View.VISIBLE
                binding?.chantsListView?.visibility = View.GONE
            } else {
                binding?.chantsListView?.visibility = View.VISIBLE
                binding?.songsListView?.visibility = View.GONE
                binding?.songsProgressbar?.visibility = View.GONE
                binding?.songsListView?.visibility = View.GONE
                binding?.tabViewInclude?.artistSongsTab?.visibility = View.GONE
                binding?.tabViewInclude?.tabSeparator?.visibility = View.GONE
            }
        }
        if (state is ChantsState) {
            if (state.chants.size > 0) {
                binding?.chantsProgressbar?.visibility = View.GONE
                currentPlayingSong(CurrentPlayingSong(_currentSong))
                binding?.chantsListView?.adapter = SongsAdapter(state.chants) { song, index ->
                    play(index, state.chants)
                    currentPlayingSong(CurrentPlayingSong(song))
                }
            } else {
                binding?.chantsProgressbar?.visibility = View.GONE
                binding?.chantsListView?.visibility = View.GONE
                binding?.tabViewInclude?.artistChantsTab?.visibility = View.GONE
                binding?.tabViewInclude?.tabSeparator?.visibility = View.GONE
            }
        }
    }

    private fun play(position: Int, songs: MutableList<Song>) {
        (activity as MenuActivity).playMediaPlayback(position, songs, _ensemble!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}