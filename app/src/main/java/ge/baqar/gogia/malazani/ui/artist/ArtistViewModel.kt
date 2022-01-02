package ge.baqar.gogia.malazani.ui.artist

import androidx.lifecycle.viewModelScope
import ge.baqar.gogia.db.db.FolkApiDao
import ge.baqar.gogia.http.repository.FolkApiRepository
import ge.baqar.gogia.malazani.arch.ReactiveViewModel
import ge.baqar.gogia.model.*
import ge.baqar.gogia.storage.CharConverter
import ge.baqar.gogia.storage.usecase.FileSaveController
import ge.baqar.gogia.utils.FileExtensions
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

@InternalCoroutinesApi
class ArtistViewModel(
    private val alazaniRepository: FolkApiRepository,
    private val folkApiDao: FolkApiDao,
    private val saveController: FileSaveController,
    private val fileExtensions: FileExtensions
) : ReactiveViewModel<ArtistAction, ArtistResult, ArtistState>(ArtistState.DEFAULT) {

    fun songs(
        ensemble: Ensemble
    ) = update {
        emit {
            ArtistState.IS_LOADING
        }

        alazaniRepository.songs(ensemble.id)
            .collect(object : FlowCollector<ReactiveResult<String, SongsResponse>> {
                override suspend fun emit(result: ReactiveResult<String, SongsResponse>) {
                    if (result is SucceedResult) {
                        val songs = folkApiDao.songsByEnsembleId(ensemble.id)

                        result.value.chants.forEach { song ->
                            song.nameEng = CharConverter.toEng(song.name)
                            song.isFav =
                                songs.firstOrNull { it.referenceId == song.id } != null
                        }
                        result.value.songs.forEach { song ->
                            song.nameEng = CharConverter.toEng(song.name)
                            song.isFav =
                                songs.firstOrNull { it.referenceId == song.id } != null
                        }
                        emit {
                            state.copy(
                                isInProgress = false,
                                songs = result.value.songs,
                                chants = result.value.chants
                            )
                        }
                    }
                    if (result is FailedResult) {
                        val cacheSongs = folkApiDao.songsByEnsembleId(ensemble.id)
                        val songs = cacheSongs
                            .filter { it.songType == SongType.Song }
                            .map {
                                val fileSystemSong =
                                    saveController.getFile(ensemble.nameEng, it.nameEng)
                                Song(
                                    it.referenceId,
                                    it.name,
                                    it.nameEng,
                                    it.filePath,
                                    it.songType,
                                    it.ensembleId,
                                    ensemble.name,
                                    false,
                                    data = fileExtensions.read(fileSystemSong?.data),
                                    isFav = true
                                )
                            }
                            .toMutableList()

                        val chants = cacheSongs
                            .filter { it.songType == SongType.Chant }
                            .map {
                                val fileSystemSong =
                                    saveController.getFile(ensemble.nameEng, it.nameEng)
                                Song(
                                    it.referenceId,
                                    it.name,
                                    it.nameEng,
                                    it.filePath,
                                    it.songType,
                                    it.ensembleId,
                                    ensemble.name,
                                    false,
                                    data = fileExtensions.read(fileSystemSong?.data),
                                    isFav = true
                                )
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
                            state.copy(isInProgress = false, error = result.value)
                        }
                    }
                }

            })
    }

    override fun ArtistAction.process(): Flow<() -> ArtistResult> {
        return when (this) {
            is ArtistSongsRequested -> {
                songs(ensemble)
            }
            else -> update {

            }
        }
    }

    suspend fun isSongFav(songId: String): Boolean {
        return  folkApiDao.song(songId) != null
    }
}