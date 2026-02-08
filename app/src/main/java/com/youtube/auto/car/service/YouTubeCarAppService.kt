package com.youtube.auto.car.service

import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.SessionInfo
import androidx.car.app.validation.HostValidator
import com.youtube.auto.car.screens.HomeScreen

class YouTubeCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator {
        // ALLOW_ALL_HOSTS_VALIDATOR is used for both debug and release because this app
        // is distributed outside the Play Store and needs to work with any Android Auto host.
        // Apps distributed via Play Store should use a restricted HostValidator with specific
        // allowed host package names and signing digests.
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(sessionInfo: SessionInfo): Session {
        return YouTubeCarSession()
    }
}

class YouTubeCarSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return HomeScreen(carContext)
    }
}
