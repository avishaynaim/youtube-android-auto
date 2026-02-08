package com.youtube.auto.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val encryptedPrefs by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "auth_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.w(TAG, "EncryptedSharedPreferences failed, using fallback", e)
            context.getSharedPreferences("auth_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    private val stateMutex = Mutex()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unknown)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentAccount = MutableStateFlow<GoogleSignInAccount?>(null)
    val currentAccount: StateFlow<GoogleSignInAccount?> = _currentAccount.asStateFlow()

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                Scope(SCOPE_YOUTUBE_READONLY),
                Scope(SCOPE_YOUTUBE_FORCE_SSL)
            )
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    fun checkExistingSignIn() {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null && !account.isExpired) {
            _currentAccount.value = account
            _authState.value = AuthState.Authenticated(account)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun handleSignInResult(account: GoogleSignInAccount) {
        _currentAccount.value = account
        _authState.value = AuthState.Authenticated(account)
    }

    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val account = _currentAccount.value ?: return@withContext null
        val googleAccount = account.account ?: return@withContext null
        try {
            val scope = "oauth2:$SCOPE_YOUTUBE_READONLY $SCOPE_YOUTUBE_FORCE_SSL"
            com.google.android.gms.auth.GoogleAuthUtil.getToken(context, googleAccount, scope)
        } catch (e: com.google.android.gms.auth.UserRecoverableAuthException) {
            Log.w(TAG, "User recoverable auth exception", e)
            _authState.value = AuthState.Error("Re-authentication required")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get access token", e)
            null
        }
    }

    /**
     * Synchronous version for use in OkHttp interceptors (runs on OkHttp thread pool).
     * Must NOT be called from the main thread.
     */
    fun getAccessTokenSync(): String? {
        val account = _currentAccount.value ?: return null
        val googleAccount = account.account ?: return null
        return try {
            val scope = "oauth2:$SCOPE_YOUTUBE_READONLY $SCOPE_YOUTUBE_FORCE_SSL"
            com.google.android.gms.auth.GoogleAuthUtil.getToken(context, googleAccount, scope)
        } catch (e: com.google.android.gms.auth.UserRecoverableAuthException) {
            Log.w(TAG, "User recoverable auth exception (sync)", e)
            _authState.value = AuthState.Error("Re-authentication required")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get access token (sync)", e)
            null
        }
    }

    /**
     * Clears a cached token from GoogleAuthUtil's cache so the next call
     * to getToken returns a fresh one. Used by the interceptor on 401.
     */
    fun clearCachedToken(token: String) {
        try {
            com.google.android.gms.auth.GoogleAuthUtil.clearToken(context, token)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear cached token", e)
        }
    }

    suspend fun getAuthHeader(): String? {
        val token = getAccessToken() ?: return null
        return "Bearer $token"
    }

    fun isSignedIn(): Boolean = _authState.value is AuthState.Authenticated

    suspend fun signOut() {
        try {
            googleSignInClient.signOut().await()
        } catch (e: Exception) {
            Log.w(TAG, "Sign out failed", e)
        }
        clearLocalData()
    }

    suspend fun revokeAccess() {
        try {
            googleSignInClient.revokeAccess().await()
        } catch (e: Exception) {
            Log.w(TAG, "Revoke access failed", e)
        }
        clearLocalData()
    }

    private fun clearLocalData() {
        encryptedPrefs.edit().clear().commit()
        _currentAccount.value = null
        _authState.value = AuthState.Unauthenticated
    }

    companion object {
        private const val TAG = "GoogleAuthManager"
        const val SCOPE_YOUTUBE_READONLY = "https://www.googleapis.com/auth/youtube.readonly"
        const val SCOPE_YOUTUBE_FORCE_SSL = "https://www.googleapis.com/auth/youtube.force-ssl"
    }
}

sealed class AuthState {
    data object Unknown : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val account: GoogleSignInAccount) : AuthState()
    data class Error(val message: String) : AuthState()
}
