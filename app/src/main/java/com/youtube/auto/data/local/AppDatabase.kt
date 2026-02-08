package com.youtube.auto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.youtube.auto.data.local.dao.*
import com.youtube.auto.data.local.entity.*

@Database(
    entities = [
        VideoEntity::class,
        ChannelEntity::class,
        PlaybackHistoryEntity::class,
        SearchCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun channelDao(): ChannelDao
    abstract fun historyDao(): HistoryDao
    abstract fun searchCacheDao(): SearchCacheDao
}
