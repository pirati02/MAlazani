package ge.baqar.gogia.storage.dataaccess

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import ge.baqar.gogia.storage.domain.FileResult
import ge.baqar.gogia.storage.domain.FileSaveResult
import ge.baqar.gogia.storage.domain.UnknownSaveError
import ge.baqar.gogia.storage.utils.endsWithMp3
import java.io.*

fun ContentResolver.saveFile(
    folder: Uri,
    contentDetails: ContentValues,
    stream: InputStream
): Uri {
    return saveFile(folder, contentDetails) {
        it.write(stream.buffered().readBytes())
    }
}

fun ContentResolver.saveFile(
    folder: Uri,
    contentDetails: ContentValues,
    bytes: ByteArray
): Uri {
    return saveFile(folder, contentDetails) {
        it.write(bytes)
    }
}

fun ContentResolver.saveFile(
    folder: Uri,
    contentDetails: ContentValues,
    writer: (OutputStream) -> Unit
): Uri {
    return insert(folder, contentDetails)?.also { contentUri ->
        openFileDescriptor(contentUri, "w")
            .use { parcelFileDescriptor ->
                writer(ParcelFileDescriptor.AutoCloseOutputStream(parcelFileDescriptor))
            }
    } ?: throw UnknownSaveError
}

fun InputStream.saveToFile(
    fileName: String,
    attachmentPath: File
): File {
    use {
        return saveToFile(fileName, attachmentPath) { outputStream ->
            val readBytes = buffered().readBytes()
            outputStream.write(readBytes)
            outputStream.flush()
        }
    }
}

fun ByteArray.saveToFile(
    fileName: String,
    attachmentPath: File
): File {
    return saveToFile(fileName, attachmentPath) { outputStream ->
        outputStream.write(this)
        outputStream.flush()
    }
}

fun getFile(
    fileName: String,
    attachmentPath: File
): FileResult {
    val uniqueFileName = FileNameLegacyResolver.getUniqueFileName(attachmentPath, fileName)
    val savedFile = File(attachmentPath, uniqueFileName)
    val filePath = if (savedFile.absolutePath.endsWithMp3()) savedFile.absolutePath
                    else "${savedFile.absolutePath}.mp3"
    val ur = File(filePath).toUri()
    return FileResult(ur, savedFile.name)
}

fun exists(
    fileName: String,
    attachmentPath: File
): Boolean {
    val uniqueFileName = FileNameLegacyResolver.getUniqueFileName(attachmentPath, fileName)
    val savedFile = File(attachmentPath, uniqueFileName)
    return savedFile.exists()
}

fun saveToFile(
    fileName: String,
    attachmentPath: File,
    writer: (OutputStream) -> Unit
): File {
    val uniqueFileName = FileNameLegacyResolver.getUniqueFileName(attachmentPath, fileName)
    val savedFile = File(attachmentPath, uniqueFileName)
    try {
        savedFile.parentFile?.mkdirs()

        FileOutputStream(savedFile).use { outputStream ->
            writer(outputStream)
        }
    } catch (e: IOException) {
        savedFile.delete()
        e.printStackTrace()
        throw e
    }
    return savedFile
}

fun Result<Uri>.toFileSaveResult(): FileSaveResult {
    return when {
        isSuccess -> FileSaveResult.SaveSuccess(this.getOrThrow())
        else -> FileSaveResult.SaveError(exceptionOrNull())
    }
}

fun Uri.startMediaScan(context: Context) {
    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    mediaScanIntent.data = this
    context.sendBroadcast(mediaScanIntent)
}

fun File.getUriWithFileProviderIfPresent(fileProviderName: String?, context: Context): Uri {
    return fileProviderName?.let {
        FileProvider.getUriForFile(context, fileProviderName, this)
    } ?: toUri()
}