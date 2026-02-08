package com.youtube.auto.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channelTitle: String,
    val itemCount: Int,
    val publishedAt: String
) : Parcelable
