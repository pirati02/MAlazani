package ge.baqar.gogia.malazani.ui.songs

import ge.baqar.gogia.db.db.FolkApiDao
import ge.baqar.gogia.http.repository.FolkApiRepository
import ge.baqar.gogia.malazani.arch.ReactiveViewModel
import ge.baqar.gogia.malazani.utility.toModel
import ge.baqar.gogia.model.*
import ge.baqar.gogia.storage.CharConverter
import ge.baqar.gogia.storage.usecase.FileSaveController
import ge.baqar.gogia.utils.FileExtensions
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

@InternalCoroutinesApi
class SongsViewModel(
    private val folkApiRepository: FolkApiRepository,
    private val folkApiDao: FolkApiDao,
    private val saveController: FileSaveController,
    private val fileExtensions: FileExtensions
) : ReactiveViewModel<SongsAction, SongsResult, ArtistState>(ArtistState.DEFAULT) {

    fun songs(
        ensemble: Ensemble
    ) = update {
        emit {
            ArtistState.IS_LOADING
        }

        folkApiRepository.songs(ensemble.id)
            .collect(object : FlowCollector<ReactiveResult<String, SongsResponse>> {
                override suspend fun emit(value: ReactiveResult<String, SongsResponse>) {
                    if (value is SucceedResult) {
                        val songs = folkApiDao.songsByEnsembleId(ensemble.id)

                        value.value.chants.forEach { song ->
                            song.nameEng = CharConverter.toEng(song.name)
                            song.isFav =
                                songs.firstOrNull { it.referenceId == song.id } != null
                        }
                        value.value.songs.forEach { song ->
                            song.nameEng = CharConverter.toEng(song.name)
                            song.isFav =
                                songs.firstOrNull { it.referenceId == song.id } != null
                        }
                        emit {
                            state.copy(
                                isInProgress = false,
                                songs = value.value.songs,
                                chants = value.value.chants
                            )
                        }
                    }
                    if (value is FailedResult) {
                        val cacheSongs = folkApiDao.songsByEnsembleId(ensemble.id)
                        val songs = cacheSongs
                            .filter { it.songType == SongType.Song }
                            .map {
                                val fileSystemSong =
                                    saveController.getFile(ensemble.nameEng, it.nameEng)
                                it.toModel(ensemble.name, fileExtensions.read(fileSystemSong?.data))
                            }
                            .toMutableList()

                        val chants = cacheSongs
                            .filter { it.songType == SongType.Chant }
                            .map {
                                val fileSystemSong =
                                    saveController.getFile(ensemble.nameEng, it.nameEng)
                                it.toModel(ensemble.name, fileExtensions.read(fileSystemSong?.data))
                            }
                            .toMutableList()

                        emit {
                            state.copy(
                                isInProgress = false,
                                songs = songs,
                                chants = chants
                            )
                        }
                        emit {
                            state.copy(isInProgress = false, error = value.value)
                        }
                    }
                }

            })
    }

    override fun SongsAction.process(): Flow<() -> SongsResult> {
        return when (this) {
            is SongsRequested -> {
                songs(ensemble)
            }
            else -> update {

            }
        }
    }

    suspend fun isSongFav(songId: String): Boolean {
        return folkApiDao.song(songId) != null
    }
}