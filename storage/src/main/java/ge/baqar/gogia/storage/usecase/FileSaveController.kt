package ge.baqar.gogia.storage.usecase

import android.content.Context
import ge.baqar.gogia.storage.dataaccess.CheckWritePermissionProcessor
import ge.baqar.gogia.storage.dataaccess.FileSaveProcessor
import ge.baqar.gogia.storage.domain.FileBytesContent
import ge.baqar.gogia.storage.domain.FileResult
import ge.baqar.gogia.storage.domain.FileSaveResult
import ge.baqar.gogia.storage.domain.FileStreamContent

class FileSaveController internal constructor(
    private val processors: ProcessorProvider,
    private val checkPermissionProcessor: CheckWritePermissionProcessor,
) {
    companion object {
        fun getInstance(context: Context, fileProviderName: String? = null): FileSaveController {
            return FileSaveController(
                ProcessorProvider(context, fileProviderName),
                CheckWritePermissionProcessor(context)
            )
        }
    }

    suspend fun saveDocumentFile(content: FileStreamContent): FileSaveResult {
        return saveFileStreamIfPermissionGranted(content, processors.audioManager)
    }

    suspend fun saveDocumentFile(content: FileBytesContent): FileSaveResult {
        return saveFileBytesIfPermissionGranted(content, processors.audioManager)
    }

    private suspend fun saveFileStreamIfPermissionGranted(
        content: FileStreamContent,
        processor: FileSaveProcessor
    ): FileSaveResult {
        return saveIfPermissionGranted {
            processor.save(content)
        }
    }

    private suspend fun saveFileBytesIfPermissionGranted(
        content: FileBytesContent,
        processor: FileSaveProcessor
    ): FileSaveResult {
        return saveIfPermissionGranted {
            processor.save(content)
        }
    }

    private suspend fun saveIfPermissionGranted(
        saveAction: suspend () -> FileSaveResult
    ): FileSaveResult {
        return if (checkPermissionProcessor.hasWritePermission()) {
            saveAction()
        } else FileSaveResult.MissingWritePermission
    }

    suspend fun getFile(dirName: String, fileName: String): FileResult? {
        return if (checkPermissionProcessor.hasWritePermission()) {
            processors.audioManager.getFile(dirName, fileName)
        } else null
    }

    suspend fun exists(dirName: String, fileName: String): Boolean {
        return if (checkPermissionProcessor.hasWritePermission()) {
            processors.audioManager.exists(dirName, fileName)
        } else false
    }

    fun delete(ensembleName: String, fileName: String) {
        processors.audioManager.delete(ensembleName, fileName)
    }
}