package com.youtube.auto.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorHandlerTest {

    @Test
    fun `getErrorMessage returns no internet for UnknownHostException`() {
        val message = ErrorHandler.getErrorMessage(UnknownHostException())
        assertEquals("No internet connection", message)
    }

    @Test
    fun `getErrorMessage returns timeout for SocketTimeoutException`() {
        val message = ErrorHandler.getErrorMessage(SocketTimeoutException())
        assertEquals("Connection timed out", message)
    }

    @Test
    fun `getErrorMessage returns network error for IOException`() {
        val message = ErrorHandler.getErrorMessage(IOException("connection reset"))
        assertEquals("Network error occurred", message)
    }

    @Test
    fun `getErrorMessage returns message for generic exception`() {
        val message = ErrorHandler.getErrorMessage(RuntimeException("custom error"))
        assertEquals("custom error", message)
    }

    @Test
    fun `getErrorMessage returns default for null throwable`() {
        val message = ErrorHandler.getErrorMessage(null)
        assertEquals("An unexpected error occurred", message)
    }
}
