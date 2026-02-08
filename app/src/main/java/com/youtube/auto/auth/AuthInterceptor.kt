package com.youtube.auto.auth

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

@Singleton
class AuthTokenRefreshInterceptor @Inject constructor(
    private val authManager: GoogleAuthManager
) : Interceptor {

    private val refreshLock = ReentrantLock()
    @Volatile
    private var lastRefreshTimeMs = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Only handle 401 on authenticated endpoints (those with an Authorization header)
        if (response.code == 401 && request.header("Authorization") != null) {
            response.close()

            Log.d(TAG, "Received 401, attempting token refresh")

            val newToken = refreshLock.withLock {
                val now = System.currentTimeMillis()
                // If another thread just refreshed within the last 5 seconds, just get a fresh token
                if (now - lastRefreshTimeMs < REFRESH_COOLDOWN_MS) {
                    authManager.getAccessTokenSync()
                } else {
                    // Clear the old token so GoogleAuthUtil returns a fresh one
                    val oldToken = request.header("Authorization")?.removePrefix("Bearer ")
                    if (oldToken != null) {
                        authManager.clearCachedToken(oldToken)
                    }
                    lastRefreshTimeMs = now
                    authManager.getAccessTokenSync()
                }
            }

            if (newToken != null) {
                val newRequest = request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(newRequest)
            }

            // Token refresh failed -- return a fresh 401 by re-executing the original request
            // This is necessary since we already closed the original response
            return chain.proceed(request)
        }

        return response
    }

    companion object {
        private const val TAG = "AuthTokenRefresh"
        private const val REFRESH_COOLDOWN_MS = 5_000L
    }
}
