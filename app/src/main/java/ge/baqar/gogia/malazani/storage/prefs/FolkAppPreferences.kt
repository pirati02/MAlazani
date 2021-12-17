package ge.baqar.gogia.malazani.storage.prefs

import android.content.Context
import android.content.SharedPreferences
import ge.baqar.gogia.malazani.poko.StorageOption

class FolkAppPreferences(private val context: Context) {
    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)!!
    }
    private val storageOptionKey = "storageOption"
    private val autoPlayEnabledKey = "autoPlayEnabled"

    fun updateAutoPlay(autoPlayEnabled: Boolean) {
        preferences.edit()
            .putBoolean(autoPlayEnabledKey, autoPlayEnabled)
            .apply()
    }

    fun getAutoPlay(): Boolean {
        return preferences.getBoolean(autoPlayEnabledKey, false)
    }

    fun updateStorageOption(storageOption: StorageOption) {
        preferences.edit()
            ?.putString(storageOptionKey, storageOption.toString())
            ?.apply()
    }

    fun getStorageOption(): StorageOption {
        return StorageOption.valueOf(
            preferences.getString(
                storageOptionKey,
                StorageOption.ApplicationCache.toString()
            ) ?: StorageOption.ApplicationCache.toString()
        )
    }
}