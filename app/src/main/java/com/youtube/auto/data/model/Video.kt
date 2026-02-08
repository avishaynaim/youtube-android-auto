package com.youtube.auto.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Video(
    val id: String,
    val title: String,
    val description: String,
    val channelId: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val publishedAt: String,
    val duration: String,
    val viewCount: Long,
    val likeCount: Long,
    val isLive: Boolean
) : Parcelable {
    val formattedViewCount: String
        get() = when {
            viewCount >= 1_000_000_000 -> String.format("%.1fB", viewCount / 1_000_000_000.0)
            viewCount >= 1_000_000 -> String.format("%.1fM", viewCount / 1_000_000.0)
            viewCount >= 1_000 -> String.format("%.1fK", viewCount / 1_000.0)
            else -> viewCount.toString()
        }

    val formattedDuration: String
        get() {
            val regex = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
            val match = regex.matchEntire(duration) ?: return duration
            val hours = match.groupValues[1].toIntOrNull() ?: 0
            val minutes = match.groupValues[2].toIntOrNull() ?: 0
            val seconds = match.groupValues[3].toIntOrNull() ?: 0
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }
}
