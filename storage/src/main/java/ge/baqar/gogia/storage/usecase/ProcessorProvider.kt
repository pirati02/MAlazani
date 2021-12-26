package ge.baqar.gogia.storage.usecase

import android.content.Context
import android.os.Build
import ge.baqar.gogia.storage.dataaccess.AudioFileSaveLegacyProcessor
import ge.baqar.gogia.storage.dataaccess.AudioFileSaveProcessor
import ge.baqar.gogia.storage.dataaccess.FileSaveProcessor

internal data class ProcessorProvider(
    private val context: Context,
    private val fileProviderName: String?) {

    val audioManager: FileSaveProcessor =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AudioFileSaveProcessor(context.contentResolver, context)
        } else {
            AudioFileSaveLegacyProcessor(context, fileProviderName)
        }
}