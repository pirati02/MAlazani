package ge.baqar.gogia.malazani.ui.artist

import ArtistAction
import ArtistChantsRequested
import ArtistSongsRequested
import android.os.Build
import androidx.annotation.RequiresApi
import ge.baqar.gogia.malazani.arch.FailedResult
import ge.baqar.gogia.malazani.arch.ReactiveViewModel
import ge.baqar.gogia.malazani.arch.SucceedResult
import ge.baqar.gogia.malazani.http.repository.AlazaniRepository
import ge.baqar.gogia.malazani.poko.AlazaniArtistListItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import org.jsoup.Jsoup

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class ArtistViewModel(
    private val alazaniRepository: AlazaniRepository?
) : ReactiveViewModel<ArtistAction, ArtistResult, ArtistState>(ArtistState.DEFAULT) {

    constructor() : this(null) {

    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun loadArtistDetails(link: String) = update {
        emit {
            ArtistState.IS_LOADING
        }

        alazaniRepository?.artists(formatUrl(link))?.collect { result ->
            if (result is SucceedResult) {

                val parsed = Jsoup.parse(result.value)
                val elements =
                    parsed.getElementsByAttributeValue("width", 500.toString()).toList()

                val songs = elements
                    .asSequence()
                    .filter { it.childNodes().count() == 1 }
                    .map {
                        val element = it.getElementsByTag("a")
                        element
                    }
                    .filter {
                        it.text().isNotEmpty()
                    }
                    .map { element ->
                        val firstChild = element.first()
                        AlazaniArtistListItem(firstChild.text(), firstChild.attr("href"))
                    }
                    .toMutableList()

                emit {
                    SongsState.dataLoaded(songs)
                }
            }
            if (result is FailedResult) {
                emit {
                    ChantsState.error(result.value.message)
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun loadArtistChants(
        link: String
    ) = update {
        emit {
            ArtistState.IS_LOADING
        }

        alazaniRepository?.artists(formatUrl(link))?.collect { result ->
            if (result is SucceedResult) {

                val parsed = Jsoup.parse(result.value)
                val elements =
                    parsed.getElementsByAttributeValue("width", 500.toString()).toList()

                val chants = elements
                    .asSequence()
                    .filter { it.childNodes().count() == 1 }
                    .map {
                        val element = it.getElementsByTag("a")
                        element
                    }
                    .filter {
                        it.text().isNotEmpty()
                    }
                    .map { element ->
                        val firstChild = element.first()
                        AlazaniArtistListItem(firstChild.text(), firstChild.attr("href"))
                    }.toMutableList()
                emit {
                    ChantsState.dataLoaded(chants)
                }
            }
            if (result is FailedResult) {
                emit {
                    ChantsState.error(result.value.message)
                }
            }
        }
    }

    fun formatUrl(url: String): String {
        var correctUrl = url
        if (!correctUrl.startsWith("http")) {
            correctUrl = "http://alazani.ge/${url}"
        }
        return correctUrl
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun ArtistAction.process(): Flow<() -> ArtistResult> {
        return when (this) {
            is ArtistSongsRequested -> {
                loadArtistDetails(link)
            }
            is ArtistChantsRequested -> {
                loadArtistChants(link!!)
            }
            else -> update {

            }
        }
    }
}