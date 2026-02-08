package com.youtube.auto.data.local.dao

import androidx.room.*
import com.youtube.auto.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoById(id: String): VideoEntity?

    @Query("SELECT * FROM videos ORDER BY cachedAt DESC LIMIT :limit")
    suspend fun getRecentVideos(limit: Int = 50): List<VideoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Query("DELETE FROM videos WHERE cachedAt < :threshold")
    suspend fun deleteExpiredVideos(threshold: Long)

    @Query("DELETE FROM videos")
    suspend fun deleteAllVideos()
}
