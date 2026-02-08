package com.youtube.auto.data.local.dao

import androidx.room.*
import com.youtube.auto.data.local.entity.ChannelEntity

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE id = :id")
    suspend fun getChannelById(id: String): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity)

    @Query("DELETE FROM channels WHERE cachedAt < :threshold")
    suspend fun deleteExpiredChannels(threshold: Long)
}
