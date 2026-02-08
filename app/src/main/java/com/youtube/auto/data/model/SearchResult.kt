package com.youtube.auto.data.model

data class SearchResult(
    val videos: List<Video>,
    val nextPageToken: String?,
    val totalResults: Long
)
