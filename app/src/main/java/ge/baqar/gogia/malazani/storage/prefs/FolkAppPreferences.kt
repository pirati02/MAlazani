package ge.baqar.gogia.malazani.storage.prefs

import android.content.Context
import android.content.SharedPreferences

class FolkAppPreferences(private val context: Context) {
    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)!!
    }

    private val playerControlsAreVisibleKey = "playerControlsAreVisible"
    private val autoPlayEnabledKey = "autoPlayEnabled"
    private val ensembleKey = "ensembleKey_"

    fun updateAutoPlay(autoPlayEnabled: Boolean) {
        preferences.edit()
            .putBoolean(autoPlayEnabledKey, autoPlayEnabled)
            .apply()
    }

    fun getAutoPlay(): Boolean {
        return preferences.getBoolean(autoPlayEnabledKey, false)
    }

    fun setOfflineEnabled(id: String, enabled: Boolean) {
        preferences.edit()
            .putBoolean("${ensembleKey}${id}", enabled)
            .apply()
    }

    fun getOfflineEnabled(id: String): Boolean {
        return preferences.getBoolean("${ensembleKey}${id}", false)
    }

    fun setPlayerState(playerControlsAreVisible: Boolean) {
        preferences.edit()
            .putBoolean(playerControlsAreVisibleKey, playerControlsAreVisible)
            .apply()
    }


    fun getPlayerState(): Boolean {
        return preferences.getBoolean(playerControlsAreVisibleKey, true)
    }
}