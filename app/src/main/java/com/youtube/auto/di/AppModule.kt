package com.youtube.auto.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.youtube.auto.data.api.YouTubeApiService
import com.youtube.auto.data.local.AppDatabase
import com.youtube.auto.data.local.dao.*
import com.youtube.auto.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (com.youtube.auto.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.YOUTUBE_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideYouTubeApiService(retrofit: Retrofit): YouTubeApiService {
        return retrofit.create(YouTubeApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DB_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideVideoDao(db: AppDatabase): VideoDao = db.videoDao()

    @Provides
    fun provideChannelDao(db: AppDatabase): ChannelDao = db.channelDao()

    @Provides
    fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao()

    @Provides
    fun provideSearchCacheDao(db: AppDatabase): SearchCacheDao = db.searchCacheDao()
}
