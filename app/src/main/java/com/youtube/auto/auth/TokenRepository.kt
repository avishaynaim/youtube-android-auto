package com.youtube.auto.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "token_store",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAccessToken(token: String, expiresInSeconds: Long) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + (expiresInSeconds * 1000))
            .apply()
    }

    fun saveRefreshToken(token: String) {
        prefs.edit()
            .putString(KEY_REFRESH_TOKEN, token)
            .apply()
    }

    fun getAccessToken(): String? {
        val expiry = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
        if (System.currentTimeMillis() >= expiry) return null
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun isTokenValid(): Boolean {
        val expiry = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
        return System.currentTimeMillis() < expiry && getAccessToken() != null
    }

    fun clearTokens() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
    }
}
