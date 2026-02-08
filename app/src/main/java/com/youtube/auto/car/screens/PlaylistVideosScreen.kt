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

class PlaylistVideosScreen(
    carContext: CarContext,
    private val playlistId: String,
    private val playlistTitle: String
) : Screen(carContext) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PlaylistVideosEntryPoint {
        fun youTubeRepository(): YouTubeRepository
    }

    private val repository: YouTubeRepository by lazy {
        EntryPointAccessors.fromApplication(
            carContext.applicationContext,
            PlaylistVideosEntryPoint::class.java
        ).youTubeRepository()
    }

    @Volatile private var videos: List<Video> = emptyList()
    @Volatile private var isLoading = true
    @Volatile private var errorMessage: String? = null

    init {
        loadVideos()
    }

    private fun loadVideos() {
        lifecycleScope.launch {
            isLoading = true
            invalidate()

            when (val result = repository.getPlaylistVideos(playlistId)) {
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
    }

    override fun onGetTemplate(): Template {
        if (isLoading) {
            return MessageTemplate.Builder("")
                .setTitle(playlistTitle)
                .setHeaderAction(Action.BACK)
                .setLoading(true)
                .build()
        }

        if (errorMessage != null) {
            return MessageTemplate.Builder(errorMessage!!)
                .setTitle(playlistTitle)
                .setHeaderAction(Action.BACK)
                .addAction(
                    Action.Builder()
                        .setTitle("Retry")
                        .setOnClickListener { loadVideos() }
                        .build()
                )
                .build()
        }

        if (videos.isEmpty()) {
            return MessageTemplate.Builder("No videos in this playlist")
                .setTitle(playlistTitle)
                .setHeaderAction(Action.BACK)
                .build()
        }

        val listBuilder = ItemList.Builder()
        for (video in videos) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(video.title.take(60))
                    .addText(video.channelTitle)
                    .addText("${video.formattedViewCount} views • ${video.formattedDuration}")
                    .setOnClickListener {
                        screenManager.push(VideoPlayerScreen(carContext, video))
                    }
                    .setBrowsable(true)
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setTitle(playlistTitle)
            .setHeaderAction(Action.BACK)
            .setSingleList(listBuilder.build())
            .build()
    }
}
