package com.youtube.auto.data.model

import com.google.gson.annotations.SerializedName

data class YouTubeListResponse<T>(
    @SerializedName("kind") val kind: String,
    @SerializedName("nextPageToken") val nextPageToken: String?,
    @SerializedName("prevPageToken") val prevPageToken: String?,
    @SerializedName("pageInfo") val pageInfo: PageInfo?,
    @SerializedName("items") val items: List<T>?
)

data class PageInfo(
    @SerializedName("totalResults") val totalResults: Long,
    @SerializedName("resultsPerPage") val resultsPerPage: Int
)

data class VideoItem(
    @SerializedName("kind") val kind: String,
    @SerializedName("id") val id: Any, // Can be String or IdObject
    @SerializedName("snippet") val snippet: VideoSnippet?,
    @SerializedName("contentDetails") val contentDetails: ContentDetails?,
    @SerializedName("statistics") val statistics: Statistics?,
    @SerializedName("liveStreamingDetails") val liveStreamingDetails: LiveStreamingDetails?
)

data class IdObject(
    @SerializedName("kind") val kind: String?,
    @SerializedName("videoId") val videoId: String?,
    @SerializedName("channelId") val channelId: String?,
    @SerializedName("playlistId") val playlistId: String?
)

data class VideoSnippet(
    @SerializedName("publishedAt") val publishedAt: String?,
    @SerializedName("channelId") val channelId: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("thumbnails") val thumbnails: Thumbnails?,
    @SerializedName("channelTitle") val channelTitle: String?,
    @SerializedName("liveBroadcastContent") val liveBroadcastContent: String?
)

data class Thumbnails(
    @SerializedName("default") val default_: ThumbnailInfo?,
    @SerializedName("medium") val medium: ThumbnailInfo?,
    @SerializedName("high") val high: ThumbnailInfo?,
    @SerializedName("maxres") val maxres: ThumbnailInfo?
)

data class ThumbnailInfo(
    @SerializedName("url") val url: String,
    @SerializedName("width") val width: Int?,
    @SerializedName("height") val height: Int?
)

data class ContentDetails(
    @SerializedName("duration") val duration: String?,
    @SerializedName("dimension") val dimension: String?,
    @SerializedName("definition") val definition: String?
)

data class Statistics(
    @SerializedName("viewCount") val viewCount: String?,
    @SerializedName("likeCount") val likeCount: String?,
    @SerializedName("commentCount") val commentCount: String?
)

data class LiveStreamingDetails(
    @SerializedName("actualStartTime") val actualStartTime: String?,
    @SerializedName("concurrentViewers") val concurrentViewers: String?
)

data class ChannelItem(
    @SerializedName("kind") val kind: String,
    @SerializedName("id") val id: String,
    @SerializedName("snippet") val snippet: ChannelSnippet?,
    @SerializedName("statistics") val statistics: ChannelStatistics?,
    @SerializedName("contentDetails") val contentDetails: ChannelContentDetails?
)

data class ChannelSnippet(
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("customUrl") val customUrl: String?,
    @SerializedName("thumbnails") val thumbnails: Thumbnails?
)

data class ChannelStatistics(
    @SerializedName("subscriberCount") val subscriberCount: String?,
    @SerializedName("videoCount") val videoCount: String?
)

data class ChannelContentDetails(
    @SerializedName("relatedPlaylists") val relatedPlaylists: RelatedPlaylists?
)

data class RelatedPlaylists(
    @SerializedName("likes") val likes: String?,
    @SerializedName("uploads") val uploads: String?
)

data class PlaylistItem(
    @SerializedName("kind") val kind: String,
    @SerializedName("id") val id: String,
    @SerializedName("snippet") val snippet: PlaylistSnippet?,
    @SerializedName("contentDetails") val contentDetails: PlaylistContentDetails?
)

data class PlaylistSnippet(
    @SerializedName("publishedAt") val publishedAt: String?,
    @SerializedName("channelTitle") val channelTitle: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("thumbnails") val thumbnails: Thumbnails?
)

data class PlaylistContentDetails(
    @SerializedName("itemCount") val itemCount: Int?
)

data class PlaylistItemEntry(
    @SerializedName("kind") val kind: String,
    @SerializedName("snippet") val snippet: PlaylistItemSnippet?,
    @SerializedName("contentDetails") val contentDetails: PlaylistItemContentDetails?
)

data class PlaylistItemSnippet(
    @SerializedName("publishedAt") val publishedAt: String?,
    @SerializedName("channelId") val channelId: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("thumbnails") val thumbnails: Thumbnails?,
    @SerializedName("channelTitle") val channelTitle: String?,
    @SerializedName("resourceId") val resourceId: ResourceId?
)

data class ResourceId(
    @SerializedName("kind") val kind: String?,
    @SerializedName("videoId") val videoId: String?,
    @SerializedName("channelId") val channelId: String?
)

data class PlaylistItemContentDetails(
    @SerializedName("videoId") val videoId: String?
)

data class SubscriptionItem(
    @SerializedName("kind") val kind: String,
    @SerializedName("id") val id: String,
    @SerializedName("snippet") val snippet: SubscriptionSnippet?
)

data class SubscriptionSnippet(
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("resourceId") val resourceId: ResourceId?,
    @SerializedName("thumbnails") val thumbnails: Thumbnails?
)
