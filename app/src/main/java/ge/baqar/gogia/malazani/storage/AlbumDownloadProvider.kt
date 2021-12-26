package ge.baqar.gogia.malazani.storage

import ge.baqar.gogia.malazani.http.repository.FolkApiRepository
import ge.baqar.gogia.malazani.storage.db.FolkApiDao
import ge.baqar.gogia.storage.usecase.FileSaveController

class AlbumDownloadProvider(
    private val folkApiDao: FolkApiDao,
    private val alazaniRepository: FolkApiRepository,
    private val saveController: FileSaveController
) {
    private val _queue = hashMapOf<String, AlbumDownloadManager>()

    fun tryGet(ensembleId: String): AlbumDownloadManager {
        if (_queue.containsKey(ensembleId))
            return _queue[ensembleId]!!
        val albumDownloadManager = AlbumDownloadManager(folkApiDao, alazaniRepository, saveController)
        _queue[ensembleId] = albumDownloadManager
        return albumDownloadManager
    }

    fun dispose(albumDownloadProvider: AlbumDownloadManager) {
        if (_queue.containsValue(albumDownloadProvider)) {
            val key = _queue.entries.firstOrNull { it.value == albumDownloadProvider }?.key
            key?.let {
             _queue.remove(key)
            }
        }
    }
}