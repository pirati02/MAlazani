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
            put(MediaStore.Audio.Media.DISPLAY_NAME, fileNameWithSuffix)
            mimeType?.let { put(MediaStore.Audio.Media.MIME_TYPE, it) }
            subfolderName?.let {
                put(
                    MediaStore.Audio.Media.RELATIVE_PATH,
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

    override fun delete(dirName: String) {
        val selection = "${MediaStore.Video.Media.ARTIST} like ?"
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        contentResolver.delete(musicUri, selection, arrayOf(dirName))
    }

    override suspend fun exists(dirName: String, fileName: String): Boolean {
        return getFile(dirName, fileName) != null
    }

    @SuppressLint("Recycle")
    override suspend fun getFile(dirName: String, fileName: String): FileResult? {
        val selection = "${MediaStore.Video.Media.DISPLAY_NAME} like ?"
        val selectionArgs = arrayOf(
            if (fileName.endsWithMp3()) fileName
            else "${fileName}.mp3"
        )

        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = contentResolver.query(musicUri, null, selection, selectionArgs, null)

        if (musicCursor != null && musicCursor.moveToFirst()) {
            val songArtist = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST)
            val songName = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            val songData = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)

            do {
                val name = musicCursor.getString(songName)
                    val artist = musicCursor.getString(songArtist)?.lowercase()
                val data = musicCursor.getString(songData)
                val uri = Uri.fromFile(File(data))

                if (artist == dirName)
                    return FileResult(uri, name)
            } while (musicCursor.moveToNext())
        }
        musicCursor?.close()
        return null
    }


    private fun getAudioFolderUri(): Uri {
        return MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }
}

