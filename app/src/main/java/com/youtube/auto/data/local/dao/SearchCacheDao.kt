package com.youtube.auto.data.local.dao

import androidx.room.*
import com.youtube.auto.data.local.entity.SearchCacheEntity

@Dao
interface SearchCacheDao {
    @Query("SELECT * FROM search_cache WHERE `query` = :query")
    suspend fun getCachedSearch(query: String): SearchCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchCache(entry: SearchCacheEntity)

    @Query("DELETE FROM search_cache WHERE cachedAt < :threshold")
    suspend fun deleteExpiredCache(threshold: Long)

    @Query("DELETE FROM search_cache")
    suspend fun clearCache()
}
