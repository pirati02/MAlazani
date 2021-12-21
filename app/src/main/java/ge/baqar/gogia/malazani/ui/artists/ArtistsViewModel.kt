package ge.baqar.gogia.malazani.ui.artists

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import ge.baqar.gogia.malazani.arch.FailedResult
import ge.baqar.gogia.malazani.arch.ReactiveViewModel
import ge.baqar.gogia.malazani.arch.SucceedResult
import ge.baqar.gogia.malazani.http.repository.AlazaniRepository
import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.storage.FolkApiDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ArtistsViewModel(
    private val alazaniRepository: AlazaniRepository,
    private val folkApiDao: FolkApiDao
) : ReactiveViewModel<ArtistsAction, ArtistsResult, ArtistsState>(ArtistsState.DEFAULT) {

    var dataSetOffline = false
    @RequiresApi(Build.VERSION_CODES.M)
    fun ensembles() = update {
        emit {
            state.copy(isInProgress = true)
        }
        alazaniRepository.ensembles().collect { result ->
            if (result is SucceedResult) {
                emit {
                    result.value.sortBy { it.name }
                    state.copy(isInProgress = false, artists = result.value, error = null)
                }
                return@collect
            }
            if (result is FailedResult) {
                val enembles = folkApiDao.ensembles().map {
                    Ensemble(
                        it.id,
                        it.name,
                        it.artistType,
                    )
                }.toMutableList()
                enembles.sortBy { it.name }

                dataSetOffline = true
                emit {
                    state.copy(isInProgress = false, artists = enembles)
                }
                emit { state.copy(isInProgress = false, error = result.value) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun oldRecordings() = update {
        emit {
            state.copy(isInProgress = true)
        }
        alazaniRepository.oldRecordings().collect { result ->
            if (result is SucceedResult) {
                emit {
                    state.copy(isInProgress = false, artists = result.value)
                }
            }
            if (result is FailedResult) {
                emit { state.copy(isInProgress = false, error = result.value) }
            }
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
            is EnsemblesRequested -> {
                ensembles()
            }
            is OldRecordingsRequested -> {
                oldRecordings()
            }
            else -> update {

            }
        }
    }
}