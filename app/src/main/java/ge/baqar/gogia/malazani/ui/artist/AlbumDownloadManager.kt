package ge.baqar.gogia.malazani.ui.artist

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import ge.baqar.gogia.malazani.arch.SucceedResult
import ge.baqar.gogia.malazani.http.repository.AlazaniRepository
import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.Song
import ge.baqar.gogia.malazani.poko.StorageOption
import ge.baqar.gogia.malazani.poko.database.DbEnsemble
import ge.baqar.gogia.malazani.poko.database.DbSong
import ge.baqar.gogia.malazani.storage.FolkApiDao
import ge.baqar.gogia.malazani.storage.prefs.FolkAppPreferences
import ge.baqar.gogia.malazani.utility.tickerFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class AlbumDownloadManager(
    private val context: Context,
    private val folkAppPreferences: FolkAppPreferences,
    private val folkApiDao: FolkApiDao,
    private val alazaniRepository: AlazaniRepository
) : CoroutineScope {

    override val coroutineContext = Dispatchers.IO + SupervisorJob()

    private lateinit var _songs: MutableList<Song>
    private lateinit var _ensemble: Ensemble
    private val downloadManager: DownloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }
    private val downloadRequestIds = mutableListOf<Long>()
    private var isDownloading = false

    fun setDownloadData(ensemble: Ensemble, songs: MutableList<Song>, chants: MutableList<Song>) {
        _ensemble = ensemble
        songs.addAll(chants)
        _songs = songs
    }

    @ExperimentalTime
    fun download() {
        val storageOption = folkAppPreferences.getStorageOption()
        if (storageOption == StorageOption.ApplicationCache) {
            downloadAlbumLocalCache()
            return
        }
        downloadAlbumExternal()
    }

    private fun downloadAlbumLocalCache() {
        launch(Dispatchers.IO) {
            val convertedSongs = _songs
                .map { DbSong(it.id, it.name, it.path, it.ensembleId, it.songType, null) }
                .toMutableList()
            isDownloading = true
            convertedSongs.forEachIndexed { _, dbSong ->
                alazaniRepository.downloadSong(dbSong.path!!).collect {
                    if (it is SucceedResult) {
                        dbSong.data = it.value
                    }
                }
            }

            val convertedEnsemble =
                DbEnsemble(_ensemble.id, _ensemble.name, _ensemble.artistType.toString())
            folkApiDao.saveEnsemble(convertedEnsemble)
            folkApiDao.saveSongs(convertedSongs)
            isDownloading = false
        }
    }

    private fun cleanLocalCache() {
        launch { folkApiDao.removeSongs(_songs) }
    }

    @ExperimentalTime
    private fun downloadAlbumExternal() {
        isDownloading = true
        _songs.forEach {
            val downloadUri: Uri =
                Uri.parse(it.path)
            val request = DownloadManager.Request(downloadUri)

            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setAllowedOverRoaming(false)
            request.setTitle("იწერება ${_ensemble.name} ${it.name}")
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "${_ensemble.name}-${it.name}.mp3"
            )
            downloadRequestIds.add(downloadManager.enqueue(request))
        }
        launch {
            tickerFlow(Duration.seconds(1000L), Duration.seconds(2000L)).collect {
                checkExternalDownloadStatus()
            }
        }
    }

    @SuppressLint("Range")
    private fun checkExternalDownloadStatus() {
        if (downloadRequestIds.size > 0) {
            downloadRequestIds.forEach {
                val cursor: Cursor? =
                    downloadManager.query(DownloadManager.Query().setFilterById(it))

                if (cursor != null && cursor.moveToNext()) {
                    val status: Int =
                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    cursor.close()
                    if (status == DownloadManager.STATUS_FAILED) {
                        isDownloading = false
                    } else if (status == DownloadManager.STATUS_PENDING || status == DownloadManager.STATUS_PAUSED) {
                        // do something pending or paused
                    } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        isDownloading = false
                    } else if (status == DownloadManager.STATUS_RUNNING) {
                        // do something when running
                    }
                }
            }
        }
    }


    fun clearDownloads() {
        val storageOption = folkAppPreferences.getStorageOption()
        if (storageOption == StorageOption.ApplicationCache) {
            cleanLocalCache()
            return
        }
        downloadManager.remove(*downloadRequestIds.toLongArray())
    }
}