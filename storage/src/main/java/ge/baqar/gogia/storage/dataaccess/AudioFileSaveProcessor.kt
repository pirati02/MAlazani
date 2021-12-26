package ge.baqar.gogia.storage.dataaccess

import android.content.ContentResolver
import android.content.ContentUris
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

    override suspend fun getFile(dirName: String, fileName: String): FileResult? {
        val selection = "${MediaStore.Video.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(
            if (fileName.endsWith(".mp3")) fileName
            else "${fileName}.mp3"
        )

        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = contentResolver.query(musicUri, null, selection, selectionArgs, null)

        if (musicCursor != null && musicCursor.moveToFirst()) {
            val idColumn = musicCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val songArtist = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songName = musicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)

            do {
                val id = musicCursor.getLong(idColumn)
                val name = musicCursor.getString(songName)
                val artist = musicCursor.getString(songArtist)?.lowercase()

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                if (artist == dirName)
                    return FileResult(contentUri, name)
            } while (musicCursor.moveToNext())
        }
        return null
    }


    private fun getAudioFolderUri(): Uri {
        return MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }
}

