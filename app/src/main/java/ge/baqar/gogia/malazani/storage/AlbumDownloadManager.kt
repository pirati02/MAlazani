package ge.baqar.gogia.malazani.storage

import ge.baqar.gogia.db.db.FolkApiDao
import ge.baqar.gogia.db.model.DbEnsemble
import ge.baqar.gogia.db.model.DbSong
import ge.baqar.gogia.http.repository.FolkApiRepository
import ge.baqar.gogia.malazani.utility.toDb
import ge.baqar.gogia.model.DownloadableSong
import ge.baqar.gogia.model.Ensemble
import ge.baqar.gogia.model.SucceedResult
import ge.baqar.gogia.storage.domain.FileStreamContent
import ge.baqar.gogia.storage.usecase.FileSaveController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.*

class AlbumDownloadManager internal constructor(
    private val folkApiDao: FolkApiDao,
    private val folkApiRepository: FolkApiRepository,
    private val saveController: FileSaveController
) : CoroutineScope {
    override val coroutineContext = Dispatchers.IO + SupervisorJob()

    private val songs: MutableList<DbSong> = mutableListOf()
    private lateinit var _ensemble: Ensemble
    private var isDownloading = false
    private var canceled = false
    var downloadId = Random().nextInt(3000)

    fun setDownloadData(ensemble: Ensemble, downloadSongs: MutableList<DownloadableSong>) {
        _ensemble = ensemble
        songs.clear()
        songs.addAll(downloadSongs.map {
            it.toDb()
        })
    }

    fun download(completion: () -> Unit) {
        if (!::_ensemble.isInitialized)
            return

        launch {
            isDownloading = true
            var existingEnsemble = folkApiDao.ensembleById(_ensemble.id)
            if (existingEnsemble == null) {
                existingEnsemble = DbEnsemble(
                    UUID.randomUUID().toString(),
                    _ensemble.id,
                    _ensemble.name,
                    _ensemble.nameEng,
                    _ensemble.artistType,
                    false
                )
                folkApiDao.saveEnsemble(existingEnsemble)
            }

            val dbSongs = folkApiDao.songsByEnsembleId(_ensemble.id)
            val filtered =
                songs.filter { outer ->
                    dbSongs.firstOrNull { inner -> inner.referenceId == outer.referenceId } == null
                }

            if (filtered.isEmpty()) {
                completion()
                return@launch
            }

            for (song in filtered) {
                if (canceled) return@launch

                val exists = saveController.exists(_ensemble.nameEng, song.nameEng)
                if (!exists) {
                    val result = folkApiRepository.downloadSong(song.path!!)
                    if (result is SucceedResult<InputStream>) {
                        saveController.saveDocumentFile(
                            FileStreamContent(
                                data = result.value,
                                fileNameWithoutSuffix = song.nameEng,
                                suffix = "mp3",
                                mimeType = "audio/mp3",
                                subfolderName = _ensemble.nameEng
                            )
                        )
                    }
                }
                folkApiDao.saveSong(song)
            }

            isDownloading = false
            completion()
        }
    }

    fun cancel() {
        canceled = true
    }

    fun clearDownloads(ensembleId: String, songIds: MutableList<DownloadableSong>, ensembleName: String) {
        launch {
            folkApiDao.removeSongsByIds(songIds.map { it.id })

            val songs = folkApiDao.songsByEnsembleId(ensembleId)
            if (songs.isEmpty())
                folkApiDao.removeEnsemble(ensembleId)

            songIds.forEach {
                saveController.delete(ensembleName, it.nameEng)
            }
        }
    }
}