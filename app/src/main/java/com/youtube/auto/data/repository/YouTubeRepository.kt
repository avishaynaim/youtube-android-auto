package com.youtube.auto.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.youtube.auto.BuildConfig
import com.youtube.auto.auth.GoogleAuthManager
import com.youtube.auto.data.api.YouTubeApiService
import com.youtube.auto.data.local.dao.*
import com.youtube.auto.data.local.entity.*
import com.youtube.auto.data.model.*
import com.youtube.auto.util.Constants
import com.youtube.auto.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeRepository @Inject constructor(
    private val api: YouTubeApiService,
    private val videoDao: VideoDao,
    private val channelDao: ChannelDao,
    private val searchCacheDao: SearchCacheDao,
    private val authManager: GoogleAuthManager,
    private val gson: Gson
) {
    private val apiKey: String
        get() {
            val key = BuildConfig.YOUTUBE_API_KEY
            if (key.isBlank() || key.startsWith("YOUR_")) {
                throw IllegalStateException("YouTube API key not configured")
            }
            return key
        }

    private fun normalizeQuery(query: String): String {
        return query.trim().replace(Regex("\\s+"), " ").lowercase()
    }

    suspend fun searchVideos(query: String, pageToken: String? = null): Result<SearchResult> =
        withContext(Dispatchers.IO) {
            try {
                val normalizedQuery = normalizeQuery(query)

                // Check cache first (only for first page)
                if (pageToken == null) {
                    val cached = searchCacheDao.getCachedSearch(normalizedQuery)
                    if (cached != null && !isCacheExpired(cached.cachedAt, Constants.CACHE_TTL_SEARCH_MS)) {
                        val type = object : TypeToken<SearchResult>() {}.type
                        val result = gson.fromJson<SearchResult>(cached.resultJson, type)
                        return@withContext Result.Success(result)
                    }
                }

                val response = api.searchVideos(
                    query = normalizedQuery,
                    pageToken = pageToken,
                    maxResults = Constants.MAX_RESULTS_SEARCH,
                    apiKey = apiKey
                )

                val videoIds = response.items.orEmpty().mapNotNull { item ->
                    when (val id = item.id) {
                        is String -> id
                        is Map<*, *> -> (id as? Map<String, Any>)?.get("videoId") as? String
                        else -> {
                            val idStr = gson.toJson(item.id)
                            val idObj = gson.fromJson(idStr, IdObject::class.java)
                            idObj.videoId
                        }
                    }
                }

                val videos = if (videoIds.isNotEmpty()) {
                    val detailsResponse = api.getVideoDetails(
                        id = videoIds.joinToString(","),
                        apiKey = apiKey
                    )
                    detailsResponse.items.orEmpty().map { it.toVideo() }
                } else {
                    emptyList()
                }

                val searchResult = SearchResult(
                    videos = videos,
                    nextPageToken = response.nextPageToken,
                    totalResults = response.pageInfo?.totalResults ?: 0L
                )

                // Cache first page results
                if (pageToken == null) {
                    searchCacheDao.insertSearchCache(
                        SearchCacheEntity(query = normalizedQuery, resultJson = gson.toJson(searchResult))
                    )
                }

                // Cache individual videos
                videoDao.insertVideos(videos.map { it.toEntity() })

                Result.Success(searchResult)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Search failed", e)
            }
        }

    @Volatile
    private var trendingCachedAt = 0L

    @Volatile
    private var trendingCache: SearchResult? = null

    suspend fun getTrendingVideos(pageToken: String? = null): Result<SearchResult> =
        withContext(Dispatchers.IO) {
            try {
                // Use in-memory cache for first page
                if (pageToken == null && trendingCache != null &&
                    !isCacheExpired(trendingCachedAt, Constants.CACHE_TTL_TRENDING_MS)
                ) {
                    return@withContext Result.Success(trendingCache!!)
                }

                val response = api.getTrendingVideos(
                    maxResults = Constants.MAX_RESULTS_TRENDING,
                    pageToken = pageToken,
                    apiKey = apiKey
                )
                val videos = response.items.orEmpty().map { it.toVideo() }
                videoDao.insertVideos(videos.map { it.toEntity() })

                val result = SearchResult(
                    videos = videos,
                    nextPageToken = response.nextPageToken,
                    totalResults = response.pageInfo?.totalResults ?: 0L
                )

                // Cache first page in memory
                if (pageToken == null) {
                    trendingCache = result
                    trendingCachedAt = System.currentTimeMillis()
                }

                Result.Success(result)
            } catch (e: Exception) {
                // Fall back to in-memory or DB cache
                val memCached = trendingCache
                if (pageToken == null && memCached != null) {
                    return@withContext Result.Success(memCached)
                }
                val cached = videoDao.getRecentVideos(50)
                if (cached.isNotEmpty()) {
                    Result.Success(
                        SearchResult(videos = cached.map { it.toDomain() }, nextPageToken = null, totalResults = cached.size.toLong())
                    )
                } else {
                    Result.Error(e.message ?: "Failed to load trending videos", e)
                }
            }
        }

    suspend fun getRelatedVideos(videoId: String): Result<SearchResult> =
        withContext(Dispatchers.IO) {
            try {
                if (!videoId.matches(Regex(Constants.VIDEO_ID_PATTERN))) {
                    return@withContext Result.Error("Invalid video ID")
                }

                val response = api.getRelatedVideos(videoId = videoId, apiKey = apiKey)
                val relatedVideoIds = response.items.orEmpty().mapNotNull { item ->
                    when (val id = item.id) {
                        is String -> id
                        is Map<*, *> -> (id as? Map<String, Any>)?.get("videoId") as? String
                        else -> {
                            val idStr = gson.toJson(item.id)
                            val idObj = gson.fromJson(idStr, IdObject::class.java)
                            idObj.videoId
                        }
                    }
                }

                val videos = if (relatedVideoIds.isNotEmpty()) {
                    val detailsResponse = api.getVideoDetails(
                        id = relatedVideoIds.joinToString(","),
                        apiKey = apiKey
                    )
                    detailsResponse.items.orEmpty().map { it.toVideo() }
                } else {
                    emptyList()
                }

                Result.Success(
                    SearchResult(
                        videos = videos,
                        nextPageToken = response.nextPageToken,
                        totalResults = response.pageInfo?.totalResults ?: 0L
                    )
                )
            } catch (e: Exception) {
                Result.Error(e.message ?: "Failed to load related videos", e)
            }
        }

    suspend fun getVideoDetails(videoId: String): Result<Video> =
        withContext(Dispatchers.IO) {
            try {
                if (!videoId.matches(Regex(Constants.VIDEO_ID_PATTERN))) {
                    return@withContext Result.Error("Invalid video ID")
                }

                // Check cache
                val cached = videoDao.getVideoById(videoId)
                if (cached != null && !isCacheExpired(cached.cachedAt, Constants.CACHE_TTL_VIDEO_MS)) {
                    return@withContext Result.Success(cached.toDomain())
                }

                val response = api.getVideoDetails(id = videoId, apiKey = apiKey)
                val video = response.items.orEmpty().firstOrNull()?.toVideo()
                    ?: return@withContext Result.Error("Video not found")

                videoDao.insertVideo(video.toEntity())
                Result.Success(video)
            } catch (e: Exception) {
                val cached = videoDao.getVideoById(videoId)
                if (cached != null) {
                    Result.Success(cached.toDomain())
                } else {
                    Result.Error(e.message ?: "Failed to load video", e)
                }
            }
        }

    suspend fun getChannelInfo(channelId: String): Result<Channel> =
        withContext(Dispatchers.IO) {
            try {
                if (!channelId.matches(Regex(Constants.CHANNEL_ID_PATTERN))) {
                    return@withContext Result.Error("Invalid channel ID")
                }

                val cached = channelDao.getChannelById(channelId)
                if (cached != null && !isCacheExpired(cached.cachedAt, Constants.CACHE_TTL_CHANNEL_MS)) {
                    return@withContext Result.Success(cached.toDomain())
                }

                val response = api.getChannelInfo(id = channelId, apiKey = apiKey)
                val channel = response.items.orEmpty().firstOrNull()?.toChannel()
                    ?: return@withContext Result.Error("Channel not found")

                channelDao.insertChannel(channel.toEntity())
                Result.Success(channel)
            } catch (e: Exception) {
                val cached = channelDao.getChannelById(channelId)
                if (cached != null) {
                    Result.Success(cached.toDomain())
                } else {
                    Result.Error(e.message ?: "Failed to load channel", e)
                }
            }
        }

    suspend fun getSubscriptions(pageToken: String? = null): Result<List<Subscription>> =
        withContext(Dispatchers.IO) {
            try {
                val authHeader = authManager.getAuthHeader()
                    ?: return@withContext Result.Error("Not signed in")

                val response = api.getSubscriptions(
                    pageToken = pageToken,
                    authHeader = authHeader
                )
                val subs = response.items.orEmpty().map { it.toSubscription() }
                Result.Success(subs)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Failed to load subscriptions", e)
            }
        }

    suspend fun getPlaylists(channelId: String, pageToken: String? = null): Result<List<Playlist>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getPlaylists(
                    channelId = channelId,
                    pageToken = pageToken,
                    apiKey = apiKey
                )
                val playlists = response.items.orEmpty().map { it.toPlaylist() }
                Result.Success(playlists)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Failed to load playlists", e)
            }
        }

    suspend fun getMyPlaylists(pageToken: String? = null): Result<List<Playlist>> =
        withContext(Dispatchers.IO) {
            try {
                val authHeader = authManager.getAuthHeader()
                    ?: return@withContext Result.Error("Not signed in")

                val response = api.getMyPlaylists(
                    pageToken = pageToken,
                    authHeader = authHeader
                )
                val playlists = response.items.orEmpty().map { it.toPlaylist() }
                Result.Success(playlists)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Failed to load playlists", e)
            }
        }

    suspend fun getPlaylistVideos(playlistId: String, pageToken: String? = null): Result<SearchResult> =
        withContext(Dispatchers.IO) {
            try {
                if (!playlistId.matches(Regex(Constants.PLAYLIST_ID_PATTERN))) {
                    return@withContext Result.Error("Invalid playlist ID")
                }

                val response = api.getPlaylistItems(
                    playlistId = playlistId,
                    pageToken = pageToken,
                    apiKey = apiKey
                )
                val videoIds = response.items.orEmpty()
                    .mapNotNull { it.contentDetails?.videoId ?: it.snippet?.resourceId?.videoId }

                val videos = if (videoIds.isNotEmpty()) {
                    val detailsResponse = api.getVideoDetails(
                        id = videoIds.joinToString(","),
                        apiKey = apiKey
                    )
                    detailsResponse.items.orEmpty().map { it.toVideo() }
                } else {
                    emptyList()
                }

                Result.Success(
                    SearchResult(
                        videos = videos,
                        nextPageToken = response.nextPageToken,
                        totalResults = response.pageInfo?.totalResults ?: 0L
                    )
                )
            } catch (e: Exception) {
                Result.Error(e.message ?: "Failed to load playlist videos", e)
            }
        }

    suspend fun cleanExpiredCache() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        videoDao.deleteExpiredVideos(now - Constants.CACHE_TTL_VIDEO_MS)
        channelDao.deleteExpiredChannels(now - Constants.CACHE_TTL_CHANNEL_MS)
        searchCacheDao.deleteExpiredCache(now - Constants.CACHE_TTL_SEARCH_MS)
    }

    private fun isCacheExpired(cachedAt: Long, ttl: Long): Boolean {
        return System.currentTimeMillis() - cachedAt > ttl
    }
}

// Extension functions for model conversion
private fun VideoItem.toVideo(): Video {
    val videoId = when (val rawId = id) {
        is String -> rawId
        is Map<*, *> -> (rawId as? Map<String, Any>)?.get("videoId") as? String ?: ""
        else -> {
            val idStr = com.google.gson.Gson().toJson(rawId)
            val idObj = com.google.gson.Gson().fromJson(idStr, IdObject::class.java)
            idObj.videoId ?: ""
        }
    }
    return Video(
        id = videoId,
        title = snippet?.title.orEmpty(),
        description = snippet?.description.orEmpty(),
        channelId = snippet?.channelId.orEmpty(),
        channelTitle = snippet?.channelTitle.orEmpty(),
        thumbnailUrl = snippet?.thumbnails?.high?.url
            ?: snippet?.thumbnails?.medium?.url
            ?: snippet?.thumbnails?.default_?.url.orEmpty(),
        publishedAt = snippet?.publishedAt.orEmpty(),
        duration = contentDetails?.duration.orEmpty(),
        viewCount = statistics?.viewCount?.toLongOrNull() ?: 0,
        likeCount = statistics?.likeCount?.toLongOrNull() ?: 0,
        commentCount = statistics?.commentCount?.toLongOrNull() ?: 0,
        isLive = snippet?.liveBroadcastContent == "live"
    )
}

private fun ChannelItem.toChannel(): Channel {
    return Channel(
        id = id,
        title = snippet?.title.orEmpty(),
        description = snippet?.description.orEmpty(),
        thumbnailUrl = snippet?.thumbnails?.high?.url
            ?: snippet?.thumbnails?.medium?.url
            ?: snippet?.thumbnails?.default_?.url.orEmpty(),
        subscriberCount = statistics?.subscriberCount?.toLongOrNull() ?: 0,
        videoCount = statistics?.videoCount?.toLongOrNull() ?: 0,
        customUrl = snippet?.customUrl.orEmpty()
    )
}

private fun PlaylistItem.toPlaylist(): Playlist {
    return Playlist(
        id = id,
        title = snippet?.title.orEmpty(),
        description = snippet?.description.orEmpty(),
        thumbnailUrl = snippet?.thumbnails?.high?.url
            ?: snippet?.thumbnails?.medium?.url
            ?: snippet?.thumbnails?.default_?.url.orEmpty(),
        channelTitle = snippet?.channelTitle.orEmpty(),
        itemCount = contentDetails?.itemCount ?: 0,
        publishedAt = snippet?.publishedAt.orEmpty()
    )
}

private fun SubscriptionItem.toSubscription(): Subscription {
    return Subscription(
        id = id,
        channelId = snippet?.resourceId?.channelId.orEmpty(),
        channelTitle = snippet?.title.orEmpty(),
        channelThumbnailUrl = snippet?.thumbnails?.high?.url
            ?: snippet?.thumbnails?.medium?.url
            ?: snippet?.thumbnails?.default_?.url.orEmpty(),
        description = snippet?.description.orEmpty()
    )
}

private fun Video.toEntity(): VideoEntity {
    return VideoEntity(
        id = id, title = title, description = description,
        channelId = channelId, channelTitle = channelTitle,
        thumbnailUrl = thumbnailUrl, publishedAt = publishedAt,
        duration = duration, viewCount = viewCount, likeCount = likeCount,
        commentCount = commentCount, isLive = isLive
    )
}

private fun VideoEntity.toDomain(): Video {
    return Video(
        id = id, title = title, description = description,
        channelId = channelId, channelTitle = channelTitle,
        thumbnailUrl = thumbnailUrl, publishedAt = publishedAt,
        duration = duration, viewCount = viewCount, likeCount = likeCount,
        commentCount = commentCount, isLive = isLive
    )
}

private fun Channel.toEntity(): ChannelEntity {
    return ChannelEntity(
        id = id, title = title, description = description,
        thumbnailUrl = thumbnailUrl, subscriberCount = subscriberCount,
        videoCount = videoCount, customUrl = customUrl
    )
}

private fun ChannelEntity.toDomain(): Channel {
    return Channel(
        id = id, title = title, description = description,
        thumbnailUrl = thumbnailUrl, subscriberCount = subscriberCount,
        videoCount = videoCount, customUrl = customUrl
    )
}
