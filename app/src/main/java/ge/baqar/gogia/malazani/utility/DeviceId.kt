package ge.baqar.gogia.malazani.utility

import android.content.Context
import android.provider.Settings

class DeviceId(private val context: Context) {
    fun get (): String? {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}