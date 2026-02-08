package com.youtube.auto.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import com.youtube.auto.settings.SettingsManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class SettingsScreen(carContext: CarContext) : Screen(carContext) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SettingsEntryPoint {
        fun settingsManager(): SettingsManager
    }

    private val settingsManager: SettingsManager by lazy {
        EntryPointAccessors.fromApplication(
            carContext.applicationContext,
            SettingsEntryPoint::class.java
        ).settingsManager()
    }

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()

        val currentQuality = settingsManager.getVideoQuality()
        listBuilder.addItem(
            Row.Builder()
                .setTitle("Video Quality")
                .addText(currentQuality)
                .setOnClickListener {
                    val nextQuality = when (currentQuality) {
                        "auto" -> "360p"
                        "360p" -> "720p"
                        "720p" -> "1080p"
                        else -> "auto"
                    }
                    settingsManager.setVideoQuality(nextQuality)
                    invalidate()
                }
                .setBrowsable(false)
                .build()
        )

        val autoplay = settingsManager.isAutoplayEnabled()
        listBuilder.addItem(
            Row.Builder()
                .setTitle("Autoplay")
                .addText(if (autoplay) "On" else "Off")
                .setOnClickListener {
                    settingsManager.setAutoplayEnabled(!autoplay)
                    invalidate()
                }
                .setBrowsable(false)
                .build()
        )

        val dataSaver = settingsManager.isDataSaverEnabled()
        listBuilder.addItem(
            Row.Builder()
                .setTitle("Data Saver")
                .addText(if (dataSaver) "On" else "Off")
                .setOnClickListener {
                    settingsManager.setDataSaverEnabled(!dataSaver)
                    invalidate()
                }
                .setBrowsable(false)
                .build()
        )

        listBuilder.addItem(
            Row.Builder()
                .setTitle("App Version")
                .addText(getAppVersion())
                .setBrowsable(false)
                .build()
        )

        return ListTemplate.Builder()
            .setTitle("Settings")
            .setHeaderAction(Action.BACK)
            .setSingleList(listBuilder.build())
            .build()
    }

    private fun getAppVersion(): String {
        return try {
            val pInfo = carContext.packageManager.getPackageInfo(carContext.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
}
