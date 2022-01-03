package ge.baqar.gogia.malazani.ui.songs

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
import ge.baqar.gogia.db.FolkAppPreferences
import ge.baqar.gogia.malazani.databinding.FragmentArtistBinding
import ge.baqar.gogia.malazani.ui.MenuActivity
import ge.baqar.gogia.model.Ensemble
import ge.baqar.gogia.model.Song
import ge.baqar.gogia.model.SongType
import ge.baqar.gogia.model.events.CurrentPlayingSong
import ge.baqar.gogia.model.events.GetCurrentSong
import ge.baqar.gogia.model.events.SongsMarkedAsFavourite
import ge.baqar.gogia.model.events.SongsUnmarkedAsFavourite
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@ExperimentalTime
@RequiresApi(Build.VERSION_CODES.O)
class SongsFragment : Fragment() {

    private var searchedItemId: String? = null
    private var _currentSong: Song? = null
    private var _ensemble: Ensemble? = null

    private val folkAppPreferences: FolkAppPreferences by inject()
    private val viewModel: SongsViewModel by inject()
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
        searchedItemId = arguments?.getString("searchedItemId")
        binding?.toolbarInclude?.tabTitleView?.text = _ensemble?.name

        val loadSongsAndChantsAction = flowOf(
            SongsRequested(_ensemble?.copy()!!),
        )
        initializeIntents(loadSongsAndChantsAction)

        initializeClickListeners()
        EventBus.getDefault().post(GetCurrentSong)
        return binding?.root!!
    }

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

        val offlineEnabled = folkAppPreferences.getOfflineEnabled(_ensemble?.id!!)
        binding?.toolbarInclude?.enableOfflineMode?.isChecked = offlineEnabled
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe
    fun songsMarkedAsFavourite(event: SongsMarkedAsFavourite) {
        binding?.songsListView?.post {
            (binding?.songsListView?.adapter as? SongsAdapter)?.apply {
                dataSource
                    .filter { event.songs.map { it.id }.contains(it.id) }
                    .forEach {
                        it.isFav = true
                    }
                notifyDataSetChanged()
            }
            (binding?.chantsListView?.adapter as? SongsAdapter)?.apply {
                dataSource
                    .filter { event.songs.map { it.id }.contains(it.id) }
                    .forEach {
                        it.isFav = true
                    }
                notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe
    fun songsMarkedAsFavourite(event: SongsUnmarkedAsFavourite) {
        binding?.songsListView?.post {
            (binding?.songsListView?.adapter as? SongsAdapter)?.apply {
                dataSource
                    .filter { event.songs.map { it.id }.contains(it.id) }
                    .forEach {
                        it.isFav = false
                    }
                notifyDataSetChanged()
            }
            (binding?.chantsListView?.adapter as? SongsAdapter)?.apply {
                dataSource
                    .filter { event.songs.map { it.id }.contains(it.id) }
                    .forEach {
                        it.isFav = false
                    }
                notifyDataSetChanged()
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
    fun initializeIntents(inputs: Flow<SongsAction>) {
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
            binding?.songsListView?.visibility = View.GONE
            binding?.chantsListView?.visibility = View.GONE
            binding?.progressbar?.visibility = View.VISIBLE
            return
        }
        if (state.error != null) {
            val errorId = resources.getIdentifier(state.error, "string", context?.packageName)
            val error = getString(errorId)
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            Timber.i(error)
            return
        }

        binding?.progressbar?.visibility = View.GONE
        if (state.songs.size > 0) {
            currentPlayingSong(CurrentPlayingSong(_currentSong))
            binding?.songsListView?.adapter = SongsAdapter(state.songs) { song, index ->
                play(index, state.songs)
                currentPlayingSong(CurrentPlayingSong(song))
            }

            binding?.songsListView?.visibility = View.VISIBLE
            binding?.chantsListView?.visibility = View.GONE

            binding?.tabViewInclude?.artistSongsTab?.visibility = View.VISIBLE
            binding?.tabViewInclude?.tabSeparator?.visibility = View.VISIBLE
        } else {
            binding?.chantsListView?.visibility = View.VISIBLE
            binding?.songsListView?.visibility = View.GONE
            binding?.songsListView?.visibility = View.GONE
            binding?.tabViewInclude?.artistSongsTab?.visibility = View.GONE
            binding?.tabViewInclude?.tabSeparator?.visibility = View.GONE
        }
        if (state.chants.size > 0) {
            currentPlayingSong(CurrentPlayingSong(_currentSong))
            binding?.chantsListView?.adapter = SongsAdapter(state.chants) { song, index ->
                play(index, state.chants)
                currentPlayingSong(CurrentPlayingSong(song))
            }
            binding?.tabViewInclude?.artistChantsTab?.visibility = View.VISIBLE
            binding?.tabViewInclude?.tabSeparator?.visibility = View.VISIBLE
        } else {
            binding?.chantsListView?.visibility = View.GONE
            binding?.tabViewInclude?.artistChantsTab?.visibility = View.GONE
            binding?.tabViewInclude?.tabSeparator?.visibility = View.GONE
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