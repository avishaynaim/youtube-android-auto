package com.youtube.auto.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class VideoTest {

    private fun createVideo(
        viewCount: Long = 0,
        duration: String = "PT0S"
    ) = Video(
        id = "test123",
        title = "Test Video",
        description = "Test description",
        channelId = "UC123",
        channelTitle = "Test Channel",
        thumbnailUrl = "https://example.com/thumb.jpg",
        publishedAt = "2024-01-01T00:00:00Z",
        duration = duration,
        viewCount = viewCount,
        likeCount = 0,
        isLive = false
    )

    @Test
    fun `formattedViewCount formats billions correctly`() {
        val video = createVideo(viewCount = 1_500_000_000)
        assertEquals("1.5B", video.formattedViewCount)
    }

    @Test
    fun `formattedViewCount formats millions correctly`() {
        val video = createVideo(viewCount = 2_300_000)
        assertEquals("2.3M", video.formattedViewCount)
    }

    @Test
    fun `formattedViewCount formats thousands correctly`() {
        val video = createVideo(viewCount = 15_700)
        assertEquals("15.7K", video.formattedViewCount)
    }

    @Test
    fun `formattedViewCount shows exact count for small numbers`() {
        val video = createVideo(viewCount = 999)
        assertEquals("999", video.formattedViewCount)
    }

    @Test
    fun `formattedDuration formats hours minutes seconds`() {
        val video = createVideo(duration = "PT1H23M45S")
        assertEquals("1:23:45", video.formattedDuration)
    }

    @Test
    fun `formattedDuration formats minutes seconds`() {
        val video = createVideo(duration = "PT5M30S")
        assertEquals("5:30", video.formattedDuration)
    }

    @Test
    fun `formattedDuration formats seconds only`() {
        val video = createVideo(duration = "PT45S")
        assertEquals("0:45", video.formattedDuration)
    }

    @Test
    fun `formattedDuration handles zero duration`() {
        val video = createVideo(duration = "PT0S")
        assertEquals("0:00", video.formattedDuration)
    }
}
