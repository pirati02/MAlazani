package ge.baqar.gogia.db

import android.content.Context
import android.content.SharedPreferences
import ge.baqar.gogia.model.AutoPlayState

class FolkAppPreferences(private val context: Context) {
    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)!!
    }
    private val playerControlsAreVisibleKey = "playerControlsAreVisible"
    private val autoPlayEnabledKey = "autoPlayEnabledKey"
    private val ensembleKey = "ensembleKey_"

    fun updateAutoPlay(autoPlayEnabled: Int) {
        preferences.edit()
            .putInt(autoPlayEnabledKey, autoPlayEnabled)
            .apply()
    }

    fun getAutoPlay(): Int {
        return preferences.getInt(autoPlayEnabledKey, AutoPlayState.OFF)
    }

    fun setPlayerState(playerControlsAreVisible: Boolean) {
        preferences.edit()
            .putBoolean(playerControlsAreVisibleKey, playerControlsAreVisible)
            .apply()
    }

    fun getPlayerState(): Boolean {
        return preferences.getBoolean(playerControlsAreVisibleKey, true)
    }

    fun setOfflineEnabled(id: String, enabled: Boolean) {
        preferences.edit()
            .putBoolean("${ensembleKey}${id}", enabled)
            .apply()
    }

    fun getOfflineEnabled(id: String): Boolean {
        return preferences.getBoolean("${ensembleKey}${id}", false)
    }
}