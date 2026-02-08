package com.youtube.auto.data.repository

import com.youtube.auto.data.local.dao.HistoryDao
import com.youtube.auto.data.local.entity.PlaybackHistoryEntity
import com.youtube.auto.data.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val historyDao: HistoryDao
) {
    fun getHistory(): Flow<List<Video>> {
        return historyDao.getHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getRecentHistory(limit: Int = 50): List<Video> = withContext(Dispatchers.IO) {
        historyDao.getRecentHistory(limit).map { it.toDomain() }
    }

    suspend fun addToHistory(video: Video, watchedDurationMs: Long = 0) = withContext(Dispatchers.IO) {
        historyDao.upsertHistory(
            PlaybackHistoryEntity(
                videoId = video.id,
                title = video.title,
                channelTitle = video.channelTitle,
                thumbnailUrl = video.thumbnailUrl,
                duration = video.duration,
                watchedDurationMs = watchedDurationMs
            )
        )
    }

    suspend fun removeFromHistory(videoId: String) = withContext(Dispatchers.IO) {
        historyDao.deleteHistoryEntry(videoId)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearHistory()
    }

    private fun PlaybackHistoryEntity.toDomain(): Video {
        return Video(
            id = videoId,
            title = title,
            description = "",
            channelId = "",
            channelTitle = channelTitle,
            thumbnailUrl = thumbnailUrl,
            publishedAt = "",
            duration = duration,
            viewCount = 0,
            likeCount = 0,
            isLive = false
        )
    }
}
