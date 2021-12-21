package ge.baqar.gogia.malazani.storage

import android.os.Parcelable
import ge.baqar.gogia.malazani.arch.SucceedResult
import ge.baqar.gogia.malazani.http.repository.AlazaniRepository
import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.SongType
import ge.baqar.gogia.malazani.poko.database.DbEnsemble
import ge.baqar.gogia.malazani.poko.database.DbSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.util.*

class AlbumDownloadManager internal constructor(
    private val folkApiDao: FolkApiDao,
    private val alazaniRepository: AlazaniRepository
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
                it.link,
                it.ensembleId,
                it.songType,
                null
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
                if (result is SucceedResult) {
                    song.data = result.value
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

    fun clearDownloads(ensembleId: String) {
        launch {
            folkApiDao.removeEnsemble(ensembleId)
            folkApiDao.removeSongsByEnsembleId(ensembleId)
        }
    }
}

@Parcelize
data class DownloadableSong(
    val id: String,
    val name: String,
    val link: String,
    val songType: SongType,
    val ensembleId: String
) : Parcelable