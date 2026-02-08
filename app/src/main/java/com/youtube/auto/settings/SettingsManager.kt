package com.youtube.auto.settings

import android.content.Context
import android.content.SharedPreferences
import com.youtube.auto.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    fun getVideoQuality(): String =
        prefs.getString(Constants.PREF_VIDEO_QUALITY, Constants.Quality.AUTO) ?: Constants.Quality.AUTO

    fun setVideoQuality(quality: String) {
        prefs.edit().putString(Constants.PREF_VIDEO_QUALITY, quality).apply()
    }

    fun isAutoplayEnabled(): Boolean =
        prefs.getBoolean(Constants.PREF_AUTOPLAY, true)

    fun setAutoplayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(Constants.PREF_AUTOPLAY, enabled).apply()
    }

    fun isDataSaverEnabled(): Boolean =
        prefs.getBoolean(Constants.PREF_DATA_SAVER, false)

    fun setDataSaverEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(Constants.PREF_DATA_SAVER, enabled).apply()
    }
}
