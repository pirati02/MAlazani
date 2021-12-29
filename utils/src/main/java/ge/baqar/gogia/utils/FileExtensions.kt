package ge.baqar.gogia.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

class FileExtensions(private val context: Context) {

    fun read(file: Uri?): ByteArray? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            file?.let {
                val fileDescriptor = context.contentResolver.openFile(file, "r", null)?.fileDescriptor
                return FileInputStream(fileDescriptor!!).readBytes()
            }
        } else {
            file?.let {
                try {
                    val inputStream: InputStream = context.openFileInput(file.toString())
                    val outputStream = ByteArrayOutputStream()
                    var nextByte: Int = inputStream.read()
                    while (nextByte != -1) {
                        outputStream.write(nextByte)
                        nextByte = inputStream.read()
                    }
                    outputStream.toByteArray()
                } catch (ignored: FileNotFoundException) {
                    null
                }
            }
        }
        return null
    }
}