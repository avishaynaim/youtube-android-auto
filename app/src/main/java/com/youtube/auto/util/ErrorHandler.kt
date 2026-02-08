package com.youtube.auto.util

import android.util.Log
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorHandler {
    private const val TAG = "YouTubeAuto"

    fun getErrorMessage(throwable: Throwable?): String {
        return when (throwable) {
            is UnknownHostException -> "No internet connection"
            is SocketTimeoutException -> "Connection timed out"
            is IOException -> "Network error occurred"
            is HttpException -> handleHttpError(throwable)
            else -> throwable?.message ?: "An unexpected error occurred"
        }
    }

    private fun handleHttpError(e: HttpException): String {
        return when (e.code()) {
            400 -> "Bad request"
            401 -> "Authentication required"
            403 -> "API quota exceeded or access denied"
            404 -> "Content not found"
            429 -> "Too many requests. Please try again later."
            500, 502, 503 -> "YouTube servers are temporarily unavailable"
            else -> "HTTP error ${e.code()}"
        }
    }

    fun logError(tag: String = TAG, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }

    fun logDebug(tag: String = TAG, message: String) {
        if (com.youtube.auto.BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
}
