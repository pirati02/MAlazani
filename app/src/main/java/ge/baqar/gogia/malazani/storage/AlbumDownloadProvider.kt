package ge.baqar.gogia.malazani.storage

import ge.baqar.gogia.malazani.http.repository.AlazaniRepository

class AlbumDownloadProvider(
    private val folkApiDao: FolkApiDao,
    private val alazaniRepository: AlazaniRepository
) {
    private val _queue = hashMapOf<String, AlbumDownloadManager>()

    fun tryGet(ensembleId: String): AlbumDownloadManager {
        if (_queue.containsKey(ensembleId))
            return _queue[ensembleId]!!
        val albumDownloadManager = AlbumDownloadManager(folkApiDao, alazaniRepository)
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