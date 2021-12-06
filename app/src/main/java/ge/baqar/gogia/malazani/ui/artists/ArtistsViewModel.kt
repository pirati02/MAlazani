package ge.baqar.gogia.malazani.ui.artists

import ArtistsAction
import ArtistsLoaded
import ArtistsRequested
import android.os.Build
import androidx.annotation.RequiresApi
import ge.baqar.gogia.malazani.arch.ReactiveViewModel
import ge.baqar.gogia.malazani.arch.SucceedResult
import ge.baqar.gogia.malazani.http.repository.AlazaniRepositoryImpl
import ge.baqar.gogia.malazani.poko.AlazaniArtistListItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import org.jsoup.Jsoup

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class ArtistsViewModel(
    private val alazaniRepository: AlazaniRepositoryImpl?
) : ReactiveViewModel<ArtistsAction, ArtistsResult, ArtistsState>(ArtistsState.DEFAULT) {

    constructor() : this(null) {

    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun loadArtists(link: String) = update {
        emit {
            state.copy(isInProgress = true)
        }
        alazaniRepository?.artists(link)?.collect { result ->
            if (result is SucceedResult) {
                emit {
                    val parsed = Jsoup.parse(result.value)
                    val elements =
                        parsed.getElementsByAttributeValue("width", 600.toString()).toList()
                    val mapped = elements
                        .filter { it.childNodes().count() == 1 }
                        .map {
                            val el = it.getElementsByTag("a")
                            AlazaniArtistListItem(el.text(), el.attr("href"))
                        }.toMutableList()

                    state.copy(isInProgress = false, artists = mapped)
                }
            }
        }
        emit {
            state.copy(isInProgress = false,
                artists = state.artists.apply {})
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun ArtistsAction.process(): Flow<() -> ArtistsResult> {
        return when (this) {
            is ArtistsLoaded -> update {
                emit {
                    state.copy(
                        isInProgress = false,
                        artists = state.artists,
                        error = null
                    )
                }
            }
            is ArtistsRequested -> {
                loadArtists(link)
            }
            else -> update {

            }
        }
    }
}