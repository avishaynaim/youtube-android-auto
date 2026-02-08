package com.youtube.auto.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.youtube.auto.auth.GoogleAuthManager
import com.youtube.auto.auth.TokenRepository
import com.youtube.auto.settings.SettingsManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SettingsScreen(carContext: CarContext) : Screen(carContext) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SettingsEntryPoint {
        fun settingsManager(): SettingsManager
        fun authManager(): GoogleAuthManager
        fun tokenRepository(): TokenRepository
    }

    private val entryPoint: SettingsEntryPoint by lazy {
        EntryPointAccessors.fromApplication(
            carContext.applicationContext,
            SettingsEntryPoint::class.java
        )
    }

    private val settingsManager: SettingsManager by lazy { entryPoint.settingsManager() }
    private val authManager: GoogleAuthManager by lazy { entryPoint.authManager() }
    private val tokenRepository: TokenRepository by lazy { entryPoint.tokenRepository() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                scope.cancel()
            }
        })
    }

    @Volatile
    private var isSigningOut = false

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

        if (authManager.isSignedIn() && !isSigningOut) {
            val accountName = authManager.currentAccount.value?.displayName ?: "Signed In"
            listBuilder.addItem(
                Row.Builder()
                    .setTitle("Sign Out")
                    .addText(accountName)
                    .setOnClickListener {
                        isSigningOut = true
                        invalidate()
                        scope.launch {
                            tokenRepository.clearTokens()
                            authManager.signOut()
                            isSigningOut = false
                            invalidate()
                        }
                    }
                    .setBrowsable(false)
                    .build()
            )
        }

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
