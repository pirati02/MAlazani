package ge.baqar.gogia.malazani.ui.artist

import ge.baqar.gogia.malazani.arch.FailedResult
import ge.baqar.gogia.malazani.arch.ReactiveViewModel
import ge.baqar.gogia.malazani.arch.SucceedResult
import ge.baqar.gogia.malazani.http.repository.FolkApiRepository
import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.Song
import ge.baqar.gogia.malazani.poko.SongType
import ge.baqar.gogia.malazani.storage.db.FolkApiDao
import ge.baqar.gogia.storage.CharConverter
import ge.baqar.gogia.storage.usecase.FileSaveController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

class ArtistViewModel(
    private val alazaniRepository: FolkApiRepository,
    private val folkApiDao: FolkApiDao,
    private val saveController: FileSaveController
) : ReactiveViewModel<ArtistAction, ArtistResult, ArtistState>(ArtistState.DEFAULT) {

    fun songs(
        ensemble: Ensemble
    ) = update {
        emit {
            ArtistState.IS_LOADING
        }

        alazaniRepository.songs(ensemble.id).collect { result ->
            if (result is SucceedResult) {
                result.value.chants.forEach {
                    it.nameEng = CharConverter.toEng(it.name)
                }
                result.value.songs.forEach {
                    it.nameEng = CharConverter.toEng(it.name)
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
                        val fileSystemSong = saveController.getFile(ensemble.nameEng, it.name)
                        Song(
                            it.referenceId,
                            it.name,
                            it.nameEng,
                            it.filePath,
                            it.songType,
                            it.ensembleId,
                            ensemble.name,
                            false,
                            localPath = fileSystemSong?.uri
                        )
                    }
                    .toMutableList()

                val chants = cacheSongs
                    .filter { it.songType == SongType.Chant }
                    .map {
                        val fileSystemSong = saveController.getFile(ensemble.nameEng, it.name)
                        Song(
                            it.referenceId,
                            it.name,
                            it.nameEng,
                            it.filePath,
                            it.songType,
                            it.ensembleId,
                            ensemble.name,
                            false, 
                            localPath = fileSystemSong?.uri
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
}