package com.youtube.auto.auth

import android.content.Context
import android.content.Intent
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
            context.getSharedPreferences("auth_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unknown)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentAccount = MutableStateFlow<GoogleSignInAccount?>(null)
    val currentAccount: StateFlow<GoogleSignInAccount?> = _currentAccount.asStateFlow()

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                Scope("https://www.googleapis.com/auth/youtube.readonly"),
                Scope("https://www.googleapis.com/auth/youtube.force-ssl")
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
        try {
            val scope = "oauth2:https://www.googleapis.com/auth/youtube.readonly https://www.googleapis.com/auth/youtube.force-ssl"
            com.google.android.gms.auth.GoogleAuthUtil.getToken(context, account.account!!, scope)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAuthHeader(): String? {
        val token = getAccessToken() ?: return null
        return "Bearer $token"
    }

    fun isSignedIn(): Boolean = _authState.value is AuthState.Authenticated

    suspend fun signOut() {
        googleSignInClient.signOut().await()
        encryptedPrefs.edit().clear().apply()
        _currentAccount.value = null
        _authState.value = AuthState.Unauthenticated
    }

    suspend fun revokeAccess() {
        googleSignInClient.revokeAccess().await()
        encryptedPrefs.edit().clear().apply()
        _currentAccount.value = null
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    data object Unknown : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val account: GoogleSignInAccount) : AuthState()
    data class Error(val message: String) : AuthState()
}
