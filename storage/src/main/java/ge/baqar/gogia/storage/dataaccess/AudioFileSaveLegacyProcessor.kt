package ge.baqar.gogia.storage.dataaccess

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import ge.baqar.gogia.storage.domain.FileBytesContent
import ge.baqar.gogia.storage.domain.FileResult
import ge.baqar.gogia.storage.domain.FileStreamContent
import ge.baqar.gogia.storage.domain.SaveContent
import java.io.File


internal class AudioFileSaveLegacyProcessor(
    private val context: Context,
    private val fileProviderName: String?
) : FileSaveProcessor {

    override fun saveFile(content: FileStreamContent): Uri {
        return content.data.saveToFile(content.fileNameWithSuffix, File(getDirectory(content)))
            .getUriWithFileProviderIfPresent(fileProviderName, context).also {
                it.startMediaScan(context)
            }
    }

    override fun saveFile(content: FileBytesContent): Uri {
        return content.data.saveToFile(content.fileNameWithSuffix, File(getDirectory(content)))
            .getUriWithFileProviderIfPresent(fileProviderName, context).also {
                it.startMediaScan(context)
            }
    }

    override fun delete(dirName: String, fileName: String) {
        val fileOrDirectory = File(dirName, fileName)
        if (fileOrDirectory.isDirectory)
            for (child in fileOrDirectory.listFiles()) {
                deleteDir(child)
            }

        fileOrDirectory.delete()
    }

    private fun deleteDir(dir: File) {
        if (dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                deleteDir(File(dir, children[i]))
            }
        }
        dir.delete()
    }

    override suspend fun exists(dirName: String, fileName: String): Boolean {
        return exists(fileName, File(getDirectory(dirName)))
    }

    override suspend fun getFile(dirName: String, fileName: String): FileResult {
        return getFile(fileName, File(getDirectory(dirName)))
    }

    private fun getDirectory(content: SaveContent): String {
        return "${getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString()}/${
            content.subfolderName?.let { "/$it" }.orEmpty()
        }"
    }

    private fun getDirectory(path: String): String {
        return "${getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString()}/${path}"
    }
}
