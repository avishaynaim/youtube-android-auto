package com.youtube.auto.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Subscription(
    val id: String,
    val channelId: String,
    val channelTitle: String,
    val channelThumbnailUrl: String,
    val description: String
) : Parcelable
