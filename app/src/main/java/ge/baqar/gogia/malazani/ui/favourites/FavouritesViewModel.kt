package ge.baqar.gogia.malazani.ui.favourites

import ge.baqar.gogia.db.db.FolkApiDao
import ge.baqar.gogia.malazani.arch.ReactiveViewModel
import ge.baqar.gogia.model.Song
import ge.baqar.gogia.storage.usecase.FileSaveController
import ge.baqar.gogia.utils.FileExtensions
import kotlinx.coroutines.flow.Flow

class FavouritesViewModel(
    private val folkApiDao: FolkApiDao,
    private val saveController: FileSaveController,
    private val fileExtensions: FileExtensions
) :
    ReactiveViewModel<FavouriteAction, FavouriteResultState, FavouriteState>(FavouriteState.DEFAULT) {
    override fun FavouriteAction.process(): Flow<() -> FavouriteResultState> {
        return when (this) {
            is FavouritesList -> update {

                val favSongs = folkApiDao.songs().groupBy {
                    it.ensembleId
                }.flatMap {
                    val ensemble = folkApiDao.ensembleById(it.key)
                    it.value.map { song ->
                        val fileSystemSong =
                            saveController.getFile(ensemble?.nameEng!!, song.nameEng)
                        Song(
                            song.referenceId,
                            song.name,
                            song.nameEng,
                            song.filePath,
                            song.songType,
                            song.ensembleId,
                            ensemble.name,
                            false,
                            data = fileExtensions.read(fileSystemSong?.data),
                            isFav = true
                        )
                    }
                }.toMutableList()
                emit {
                    state.copy(isInProgress = false, error = null, favSongs = favSongs)
                }
            }
            else -> update {

            }
        }
    }

}