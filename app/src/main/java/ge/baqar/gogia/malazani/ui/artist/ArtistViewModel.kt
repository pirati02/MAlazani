package ge.baqar.gogia.malazani.ui.artist

import android.os.Build
import androidx.annotation.RequiresApi
import ge.baqar.gogia.malazani.arch.FailedResult
import ge.baqar.gogia.malazani.arch.ReactiveViewModel
import ge.baqar.gogia.malazani.arch.SucceedResult
import ge.baqar.gogia.malazani.http.repository.AlazaniRepository
import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.Song
import ge.baqar.gogia.malazani.poko.SongType
import ge.baqar.gogia.malazani.storage.FolkApiDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

class ArtistViewModel(
    private val alazaniRepository: AlazaniRepository,
    private val folkApiDao: FolkApiDao
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
                val dbSongs = folkApiDao.songsByEnsembleId(ensemble.id)
                emit {
                    result.value.songs.forEach {
                        it.availableOffline =
                            dbSongs.firstOrNull { inner -> it.id == inner.referenceId } != null
                    }
                    SongsState.dataLoaded(result.value.songs)
                }

                emit {
                    result.value.chants.forEach {
                        it.availableOffline =
                            dbSongs.firstOrNull { inner -> it.id == inner.referenceId } != null
                    }
                    ChantsState.dataLoaded(result.value.chants)
                }
            }
            if (result is FailedResult) {
                val dbSongs = folkApiDao.songsByEnsembleId(ensemble.id)
                emit {
                    val songs = dbSongs
                        .filter { it.songType == SongType.Song }
                        .map {
                            Song(
                                it.referenceId,
                                it.name!!,
                                it.path!!,
                                it.data,
                                it.songType,
                                it.ensembleId!!,
                                availableOffline = true
                            )
                        }.toMutableList()
                    SongsState.dataLoaded(songs)
                }

                emit {
                    val chants = dbSongs
                        .filter { it.songType == SongType.Chant }
                        .map {
                            Song(
                                it.referenceId,
                                it.name!!,
                                it.path!!,
                                it.data,
                                it.songType,
                                it.ensembleId!!,
                                availableOffline = true
                            )
                        }.toMutableList()
                    ChantsState.dataLoaded(chants)
                }

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