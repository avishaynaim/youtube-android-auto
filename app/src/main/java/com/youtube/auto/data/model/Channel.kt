package com.youtube.auto.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Channel(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val subscriberCount: Long,
    val videoCount: Long,
    val customUrl: String
) : Parcelable {
    val formattedSubscriberCount: String
        get() = when {
            subscriberCount >= 1_000_000 -> String.format("%.1fM", subscriberCount / 1_000_000.0)
            subscriberCount >= 1_000 -> String.format("%.1fK", subscriberCount / 1_000.0)
            else -> subscriberCount.toString()
        }
}
