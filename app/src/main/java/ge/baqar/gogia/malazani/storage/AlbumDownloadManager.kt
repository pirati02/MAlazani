package ge.baqar.gogia.malazani.storage

import ge.baqar.gogia.malazani.arch.SucceedResult
import ge.baqar.gogia.malazani.http.repository.FolkApiRepository
import ge.baqar.gogia.malazani.poko.DownloadableSong
import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.db.DbEnsemble
import ge.baqar.gogia.malazani.poko.db.DbSong
import ge.baqar.gogia.malazani.storage.db.FolkApiDao
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
    private val alazaniRepository: FolkApiRepository,
    private val saveController: FileSaveController
) : CoroutineScope {

    override val coroutineContext = Dispatchers.IO + SupervisorJob()

    private val songs: MutableList<DbSong> = mutableListOf()
    private lateinit var _ensemble: Ensemble
    private var isDownloading = false
    private var canceled = false

    fun setDownloadData(ensemble: Ensemble, downloadSongs: MutableList<DownloadableSong>) {
        _ensemble = ensemble
        songs.clear()
        songs.addAll(downloadSongs.map {
            DbSong(
                UUID.randomUUID().toString(),
                it.id,
                it.name,
                it.nameEng,
                it.link,
                it.ensembleId,
                it.songType,
                ""
            )
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
                    _ensemble.artistType
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

                val result = alazaniRepository.downloadSong(song.path!!)
                if (result is SucceedResult<InputStream>) {
                    saveController.saveDocumentFile(
                        FileStreamContent(
                            data = result.value,
                            fileNameWithoutSuffix = song.name,
                            suffix = "mp3",
                            mimeType = "audio/mp3",
                            subfolderName = _ensemble.name
                        )
                    )
                    folkApiDao.saveSong(song)
                }
            }

            isDownloading = false
            completion()
        }
    }

    fun cancel() {
        canceled = true
    }

    fun clearDownloads(ensembleId: String) {
        launch {
            folkApiDao.removeEnsemble(ensembleId)
            folkApiDao.removeSongsByEnsembleId(ensembleId)
        }
    }
}