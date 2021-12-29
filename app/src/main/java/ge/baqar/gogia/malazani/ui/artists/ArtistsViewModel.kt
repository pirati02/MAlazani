package ge.baqar.gogia.malazani.ui.artists

import android.os.Build
import androidx.annotation.RequiresApi
import ge.baqar.gogia.db.db.FolkApiDao
import ge.baqar.gogia.http.repository.FolkApiRepository
import ge.baqar.gogia.malazani.arch.ReactiveViewModel
import ge.baqar.gogia.model.Ensemble
import ge.baqar.gogia.model.FailedResult
import ge.baqar.gogia.model.ReactiveResult
import ge.baqar.gogia.model.SucceedResult
import ge.baqar.gogia.storage.CharConverter
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

@InternalCoroutinesApi
class ArtistsViewModel(
    private val alazaniRepository: FolkApiRepository,
    private val folkApiDao: FolkApiDao
) : ReactiveViewModel<ArtistsAction, ArtistsResult, ArtistsState>(ArtistsState.DEFAULT) {

    @RequiresApi(Build.VERSION_CODES.M)
    fun ensembles() = update {
        emit {
            state.copy(isInProgress = true)
        }
        alazaniRepository.ensembles().collect(object: FlowCollector<ReactiveResult<String, MutableList<Ensemble>>>{
            override suspend fun emit(value: ReactiveResult<String, MutableList<Ensemble>>) {
                if (value is SucceedResult) {
                    emit {
                        value.value.sortBy { it.name }
                        value.value.forEach {
                            it.nameEng = CharConverter.toEng(it.name)
                        }
                        state.copy(isInProgress = false, artists = value.value)
                    }
                }
                if (value is FailedResult) {
                    val cachedEnsembles = folkApiDao.ensembles()
                    if (cachedEnsembles.isNotEmpty()) {
                        val mapped = cachedEnsembles.map {
                            Ensemble(
                                it.referenceId,
                                it.name,
                                it.nameEng,
                                it.artistType
                            )
                        }.toMutableList()
                        emit {
                            state.copy(isInProgress = false, artists = mapped)
                        }
                    }
                    emit { state.copy(isInProgress = false, error = value.value) }
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun oldRecordings() = update {
        emit {
            state.copy(isInProgress = true)
        }
        alazaniRepository.oldRecordings().collect(object: FlowCollector<ReactiveResult<String, MutableList<Ensemble>>>{
            override suspend fun emit(result: ReactiveResult<String, MutableList<Ensemble>>) {
                if (result is SucceedResult) {
                    emit {
                        state.copy(isInProgress = false, artists = result.value)
                    }
                }
                if (result is FailedResult) {
                    emit { state.copy(isInProgress = false, error = result.value) }
                }
            }

        })
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