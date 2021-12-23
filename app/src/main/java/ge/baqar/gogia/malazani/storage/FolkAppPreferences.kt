package ge.baqar.gogia.malazani.storage

import android.content.Context
import android.content.SharedPreferences
import ge.baqar.gogia.malazani.poko.AutoPlayState

class FolkAppPreferences(private val context: Context) {
    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)!!
    }
    private val playerControlsAreVisibleKey = "playerControlsAreVisible"
    private val autoPlayEnabledKey = "autoPlayEnabledKey"

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
}