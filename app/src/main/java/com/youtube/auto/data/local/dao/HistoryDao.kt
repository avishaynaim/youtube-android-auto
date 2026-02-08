package com.youtube.auto.data.local.dao

import androidx.room.*
import com.youtube.auto.data.local.entity.PlaybackHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM playback_history ORDER BY lastWatchedAt DESC")
    fun getHistory(): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history ORDER BY lastWatchedAt DESC LIMIT :limit")
    suspend fun getRecentHistory(limit: Int = 50): List<PlaybackHistoryEntity>

    @Transaction
    suspend fun upsertHistory(entry: PlaybackHistoryEntity) {
        val existing = getHistoryEntry(entry.videoId)
        if (existing != null) {
            updateLastWatched(entry.videoId, System.currentTimeMillis())
        } else {
            insertHistoryInternal(entry)
        }
    }

    @Query("SELECT * FROM playback_history WHERE videoId = :videoId")
    suspend fun getHistoryEntry(videoId: String): PlaybackHistoryEntity?

    @Query("UPDATE playback_history SET lastWatchedAt = :timestamp WHERE videoId = :videoId")
    suspend fun updateLastWatched(videoId: String, timestamp: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryInternal(entry: PlaybackHistoryEntity)

    @Query("DELETE FROM playback_history WHERE videoId = :videoId")
    suspend fun deleteHistoryEntry(videoId: String)

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()
}
