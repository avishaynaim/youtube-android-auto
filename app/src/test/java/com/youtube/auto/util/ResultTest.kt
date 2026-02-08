package com.youtube.auto.util

import org.junit.Assert.*
import org.junit.Test

class ResultTest {

    @Test
    fun `Success contains data`() {
        val result = Result.Success("hello")
        assertTrue(result.isSuccess)
        assertFalse(result.isError)
        assertEquals("hello", result.getOrNull())
        assertEquals("hello", result.getOrThrow())
    }

    @Test
    fun `Error contains message`() {
        val result = Result.Error("failed")
        assertTrue(result.isError)
        assertFalse(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `Loading state`() {
        val result = Result.Loading
        assertTrue(result.isLoading)
        assertFalse(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `map transforms Success`() {
        val result = Result.Success(5)
        val mapped = result.map { it * 2 }
        assertEquals(10, (mapped as Result.Success).data)
    }

    @Test
    fun `map preserves Error`() {
        val result: Result<Int> = Result.Error("failed")
        val mapped = result.map { it * 2 }
        assertTrue(mapped.isError)
        assertEquals("failed", (mapped as Result.Error).message)
    }

    @Test(expected = IllegalStateException::class)
    fun `getOrThrow throws on Error`() {
        val result = Result.Error("failed")
        result.getOrThrow()
    }
}
