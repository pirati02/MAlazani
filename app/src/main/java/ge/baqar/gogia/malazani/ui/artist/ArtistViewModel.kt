package ge.baqar.gogia.malazani.ui.artist

import android.os.Build
import androidx.annotation.RequiresApi
import ge.baqar.gogia.malazani.arch.FailedResult
import ge.baqar.gogia.malazani.arch.ReactiveViewModel
import ge.baqar.gogia.malazani.arch.SucceedResult
import ge.baqar.gogia.malazani.http.repository.AlazaniRepository
import ge.baqar.gogia.malazani.poko.Ensemble
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

class ArtistViewModel(
    private val alazaniRepository: AlazaniRepository
) : ReactiveViewModel<ArtistAction, ArtistResult, ArtistState>(ArtistState.DEFAULT) {

    @RequiresApi(Build.VERSION_CODES.M)
    fun songs(
        ensemble: Ensemble
    ) = update {
        emit {
            ArtistState.IS_LOADING
        }

        alazaniRepository.songs(ensemble.id).collect { result ->
            if (result is SucceedResult) {
                emit {
                    SongsState.dataLoaded(result.value.songs)
                }

                emit {
                    ChantsState.dataLoaded(result.value.chants)
                }
            }
            if (result is FailedResult) {
                emit {
                    ChantsState.error(result.value)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun ArtistAction.process(): Flow<() -> ArtistResult> {
        return when (this) {
            is ArtistSongsRequested -> {
                songs(ensemble)
            }
            else -> update {

            }
        }
    }
}