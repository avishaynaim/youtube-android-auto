package com.youtube.auto.data.api

import com.youtube.auto.data.model.*
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 25,
        @Query("pageToken") pageToken: String? = null,
        @Query("order") order: String = "relevance",
        @Query("regionCode") regionCode: String = "US",
        @Query("key") apiKey: String
    ): YouTubeListResponse<VideoItem>

    @GET("videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "snippet,contentDetails,statistics,liveStreamingDetails",
        @Query("id") id: String,
        @Query("key") apiKey: String
    ): YouTubeListResponse<VideoItem>

    @GET("videos")
    suspend fun getTrendingVideos(
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("chart") chart: String = "mostPopular",
        @Query("maxResults") maxResults: Int = 50,
        @Query("regionCode") regionCode: String = "US",
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String
    ): YouTubeListResponse<VideoItem>

    @GET("channels")
    suspend fun getChannelInfo(
        @Query("part") part: String = "snippet,statistics,contentDetails",
        @Query("id") id: String,
        @Query("key") apiKey: String
    ): YouTubeListResponse<ChannelItem>

    @GET("playlists")
    suspend fun getPlaylists(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("channelId") channelId: String,
        @Query("maxResults") maxResults: Int = 25,
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String
    ): YouTubeListResponse<PlaylistItem>

    @GET("playlists")
    suspend fun getMyPlaylists(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("mine") mine: Boolean = true,
        @Query("maxResults") maxResults: Int = 25,
        @Query("pageToken") pageToken: String? = null,
        @Header("Authorization") authHeader: String
    ): YouTubeListResponse<PlaylistItem>

    @GET("playlistItems")
    suspend fun getPlaylistItems(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 25,
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String
    ): YouTubeListResponse<PlaylistItemEntry>

    @GET("search")
    suspend fun searchRelatedVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 15,
        @Query("key") apiKey: String
    ): YouTubeListResponse<VideoItem>

    @GET("subscriptions")
    suspend fun getSubscriptions(
        @Query("part") part: String = "snippet",
        @Query("mine") mine: Boolean = true,
        @Query("maxResults") maxResults: Int = 25,
        @Query("order") order: String = "alphabetical",
        @Query("pageToken") pageToken: String? = null,
        @Header("Authorization") authHeader: String
    ): YouTubeListResponse<SubscriptionItem>
}
