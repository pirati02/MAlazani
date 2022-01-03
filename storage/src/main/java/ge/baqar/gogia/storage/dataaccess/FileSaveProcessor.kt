package ge.baqar.gogia.storage.dataaccess

import android.net.Uri
import ge.baqar.gogia.storage.domain.FileBytesContent
import ge.baqar.gogia.storage.domain.FileResult
import ge.baqar.gogia.storage.domain.FileSaveResult
import ge.baqar.gogia.storage.domain.FileStreamContent
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal interface FileSaveProcessor {

    suspend fun save(file: FileStreamContent): FileSaveResult {
        return suspendCoroutine { continuation ->
            val result = runCatching {
                saveFile(file)
            }
            continuation.resume(result.toFileSaveResult())
        }
    }

    suspend fun save(file: FileBytesContent): FileSaveResult {
        return suspendCoroutine { continuation ->
            val result = runCatching {
                saveFile(file)
            }
            continuation.resume(result.toFileSaveResult())
        }
    }

    fun saveFile(content: FileStreamContent): Uri
    fun saveFile(content: FileBytesContent): Uri

    fun delete(dirName: String, fileName: String)
    suspend fun exists(dirName: String, fileName: String): Boolean
    suspend fun getFile(dirName: String, fileName: String): FileResult?
}