package com.youtube.auto.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import com.youtube.auto.data.model.Video
import com.youtube.auto.data.repository.YouTubeRepository
import com.youtube.auto.util.Result
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

class ChannelVideosScreen(
    carContext: CarContext,
    private val channelId: String,
    private val channelTitle: String
) : Screen(carContext) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ChannelVideosEntryPoint {
        fun youTubeRepository(): YouTubeRepository
    }

    private val repository: YouTubeRepository by lazy {
        EntryPointAccessors.fromApplication(
            carContext.applicationContext,
            ChannelVideosEntryPoint::class.java
        ).youTubeRepository()
    }

    @Volatile private var videos: List<Video> = emptyList()
    @Volatile private var isLoading = true
    @Volatile private var errorMessage: String? = null

    init {
        loadChannelVideos()
    }

    private fun loadChannelVideos() {
        lifecycleScope.launch {
            isLoading = true
            invalidate()

            // Get channel info to find uploads playlist ID
            when (val channelResult = repository.getChannelInfo(channelId)) {
                is Result.Success -> {
                    val channel = channelResult.data
                    // Use uploads playlist if available, otherwise search by channel name
                    val uploadsPlaylistId = "UU${channelId.removePrefix("UC")}"
                    when (val videosResult = repository.getPlaylistVideos(uploadsPlaylistId)) {
                        is Result.Success -> {
                            videos = videosResult.data.videos.take(6)
                            isLoading = false
                            errorMessage = null
                        }
                        is Result.Error -> {
                            // Fallback: search for videos by channel name
                            loadViaSearch()
                            return@launch
                        }
                        is Result.Loading -> {}
                    }
                }
                is Result.Error -> {
                    // Fallback: search for videos by channel name
                    loadViaSearch()
                    return@launch
                }
                is Result.Loading -> {}
            }
            invalidate()
        }
    }

    private suspend fun loadViaSearch() {
        when (val result = repository.searchVideos(channelTitle)) {
            is Result.Success -> {
                videos = result.data.videos.take(6)
                isLoading = false
                errorMessage = null
            }
            is Result.Error -> {
                isLoading = false
                errorMessage = result.message
            }
            is Result.Loading -> {}
        }
        invalidate()
    }

    override fun onGetTemplate(): Template {
        if (isLoading) {
            return MessageTemplate.Builder("")
                .setTitle(channelTitle)
                .setHeaderAction(Action.BACK)
                .setLoading(true)
                .build()
        }

        if (errorMessage != null) {
            return MessageTemplate.Builder(errorMessage!!)
                .setTitle(channelTitle)
                .setHeaderAction(Action.BACK)
                .addAction(
                    Action.Builder()
                        .setTitle("Retry")
                        .setOnClickListener { loadChannelVideos() }
                        .build()
                )
                .build()
        }

        if (videos.isEmpty()) {
            return MessageTemplate.Builder("No videos found")
                .setTitle(channelTitle)
                .setHeaderAction(Action.BACK)
                .build()
        }

        val listBuilder = ItemList.Builder()
        for (video in videos) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(video.title.take(60))
                    .addText("${video.formattedViewCount} views \u2022 ${video.formattedDuration}")
                    .setOnClickListener {
                        screenManager.push(VideoPlayerScreen(carContext, video))
                    }
                    .setBrowsable(true)
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setTitle(channelTitle)
            .setHeaderAction(Action.BACK)
            .setSingleList(listBuilder.build())
            .build()
    }
}
