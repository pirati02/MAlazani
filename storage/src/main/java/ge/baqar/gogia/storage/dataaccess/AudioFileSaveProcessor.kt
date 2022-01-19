package ge.baqar.gogia.storage.dataaccess

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import ge.baqar.gogia.storage.domain.FileBytesContent
import ge.baqar.gogia.storage.domain.FileResult
import ge.baqar.gogia.storage.domain.FileStreamContent
import ge.baqar.gogia.storage.domain.SaveContent
import ge.baqar.gogia.storage.utils.endsWithMp3
import java.io.File


@RequiresApi(Build.VERSION_CODES.Q)
internal class AudioFileSaveProcessor(
    private val contentResolver: ContentResolver,
    private val context: Context
) : FileSaveProcessor {

    override fun saveFile(content: FileStreamContent): Uri {
        val downloadsFolder = getAudioFolderUri()

        with(content) {
            val contentDetails = getContentValues()

            return contentResolver.saveFile(downloadsFolder, contentDetails, data).also {
                it.startMediaScan(context)
            }
        }
    }

    private fun SaveContent.getContentValues(): ContentValues {
        val contentDetails = ContentValues().apply {
            put(MediaStore.Audio.AudioColumns.DISPLAY_NAME, fileNameWithSuffix)
            put(MediaStore.Audio.AudioColumns.ARTIST, subfolderName)
            mimeType?.let { put(MediaStore.Audio.Media.MIME_TYPE, it) }
            subfolderName?.let {
                put(
                    MediaStore.Audio.AudioColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_MUSIC}/$it"
                )
            }
        }
        return contentDetails
    }

    override fun saveFile(content: FileBytesContent): Uri {
        val downloadsFolder = getAudioFolderUri()

        with(content) {
            val contentDetails = getContentValues()

            return contentResolver.saveFile(downloadsFolder, contentDetails, data).also {
                it.startMediaScan(context)
            }
        }
    }

    override fun delete(dirName: String, fileName: String) {
        val musicUri = getAudioFolderUri()
        val file = formatFile(fileName)
        val selection = "${MediaStore.Audio.AudioColumns.DISPLAY_NAME} LIKE ?"
        contentResolver.delete(musicUri, selection, arrayOf(file))
    }

    private fun formatFile(fileName: String): String {
        return if (fileName.endsWithMp3()) fileName
        else "${fileName}.mp3"
    }

    override suspend fun exists(dirName: String, fileName: String): Boolean {
        return getFile(dirName, fileName) != null
    }

    @SuppressLint("Recycle")
    override suspend fun getFile(dirName: String, fileName: String): FileResult? {
        val selection = "${MediaStore.Audio.AudioColumns.DISPLAY_NAME} like ?"
        val selectionArgs = arrayOf(
            formatFile(fileName)
        )

        val musicUri = getAudioFolderUri()
        val musicCursor = contentResolver.query(musicUri, null, selection, selectionArgs, null)

        if (musicCursor != null && musicCursor.moveToFirst()) {
            val songName = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            val songData = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)

            do {
                val name = musicCursor.getString(songName)
                val data = musicCursor.getString(songData)

                val file = File(data)
                val uri = Uri.fromFile(file)
                val folderName = getFolderName(file.absolutePath)

                if (folderName == dirName)
                    return FileResult(uri, name)
            } while (musicCursor.moveToNext())
        }
        musicCursor?.close()
        return null
    }

    private fun getFolderName(absolutePath: String): String {
        val array = absolutePath.split("/")
        return array[array.size - 2]
    }


    private fun getAudioFolderUri(): Uri {
        return MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }
}

