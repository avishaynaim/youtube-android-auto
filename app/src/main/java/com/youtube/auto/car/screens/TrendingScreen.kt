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

class TrendingScreen(carContext: CarContext) : Screen(carContext) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TrendingScreenEntryPoint {
        fun youTubeRepository(): YouTubeRepository
    }

    private val repository: YouTubeRepository by lazy {
        EntryPointAccessors.fromApplication(
            carContext.applicationContext,
            TrendingScreenEntryPoint::class.java
        ).youTubeRepository()
    }

    @Volatile private var videos: List<Video> = emptyList()
    @Volatile private var isLoading = true
    @Volatile private var errorMessage: String? = null

    init {
        loadTrending()
    }

    private fun loadTrending() {
        lifecycleScope.launch {
            isLoading = true
            invalidate()

            when (val result = repository.getTrendingVideos()) {
                is Result.Success -> {
                    videos = result.data.videos.take(6)
                    isLoading = false
                    errorMessage = null
                }
                is Result.Error -> {
                    isLoading = false
                    errorMessage = result.message
                }
                is Result.Loading -> { /* handled by isLoading flag */ }
            }
            invalidate()
        }
    }

    override fun onGetTemplate(): Template {
        if (isLoading) {
            return MessageTemplate.Builder("")
                .setTitle("Trending")
                .setHeaderAction(Action.BACK)
                .setLoading(true)
                .build()
        }

        if (errorMessage != null) {
            return MessageTemplate.Builder(errorMessage!!)
                .setTitle("Trending")
                .setHeaderAction(Action.BACK)
                .addAction(
                    Action.Builder()
                        .setTitle("Retry")
                        .setOnClickListener { loadTrending() }
                        .build()
                )
                .build()
        }

        if (videos.isEmpty()) {
            return MessageTemplate.Builder("No trending videos available")
                .setTitle("Trending")
                .setHeaderAction(Action.BACK)
                .addAction(
                    Action.Builder()
                        .setTitle("Retry")
                        .setOnClickListener { loadTrending() }
                        .build()
                )
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
            .setTitle("Trending")
            .setHeaderAction(Action.BACK)
            .setSingleList(listBuilder.build())
            .build()
    }
}
