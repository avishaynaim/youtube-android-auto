package com.youtube.auto.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ChannelTest {

    @Test
    fun `formattedSubscriberCount formats millions`() {
        val channel = Channel(
            id = "UC123", title = "Test", description = "",
            thumbnailUrl = "", subscriberCount = 5_200_000,
            videoCount = 100, customUrl = "@test"
        )
        assertEquals("5.2M", channel.formattedSubscriberCount)
    }

    @Test
    fun `formattedSubscriberCount formats thousands`() {
        val channel = Channel(
            id = "UC123", title = "Test", description = "",
            thumbnailUrl = "", subscriberCount = 45_000,
            videoCount = 100, customUrl = "@test"
        )
        assertEquals("45.0K", channel.formattedSubscriberCount)
    }

    @Test
    fun `formattedSubscriberCount shows exact for small numbers`() {
        val channel = Channel(
            id = "UC123", title = "Test", description = "",
            thumbnailUrl = "", subscriberCount = 500,
            videoCount = 100, customUrl = "@test"
        )
        assertEquals("500", channel.formattedSubscriberCount)
    }
}
