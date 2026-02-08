package com.youtube.auto.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import com.youtube.auto.data.model.Video
import com.youtube.auto.data.repository.HistoryRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

class HistoryScreen(carContext: CarContext) : Screen(carContext) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HistoryEntryPoint {
        fun historyRepository(): HistoryRepository
    }

    private val historyRepository: HistoryRepository by lazy {
        EntryPointAccessors.fromApplication(
            carContext.applicationContext,
            HistoryEntryPoint::class.java
        ).historyRepository()
    }

    @Volatile private var historyVideos: List<Video> = emptyList()
    @Volatile private var isLoading = true
    @Volatile private var errorMessage: String? = null

    init {
        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            isLoading = true
            errorMessage = null
            invalidate()

            try {
                historyVideos = historyRepository.getRecentHistory(6)
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Failed to load history"
            }
            invalidate()
        }
    }

    override fun onGetTemplate(): Template {
        if (isLoading) {
            return MessageTemplate.Builder("")
                .setTitle("History")
                .setHeaderAction(Action.BACK)
                .setLoading(true)
                .build()
        }

        if (errorMessage != null) {
            return MessageTemplate.Builder(errorMessage!!)
                .setTitle("History")
                .setHeaderAction(Action.BACK)
                .addAction(
                    Action.Builder()
                        .setTitle("Retry")
                        .setOnClickListener { loadHistory() }
                        .build()
                )
                .build()
        }

        if (historyVideos.isEmpty()) {
            return MessageTemplate.Builder("No watch history yet")
                .setTitle("History")
                .setHeaderAction(Action.BACK)
                .build()
        }

        val listBuilder = ItemList.Builder()
        for (video in historyVideos) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(video.title.take(60))
                    .addText(video.channelTitle)
                    .setOnClickListener {
                        screenManager.push(VideoPlayerScreen(carContext, video))
                    }
                    .setBrowsable(true)
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setTitle("History")
            .setHeaderAction(Action.BACK)
            .setSingleList(listBuilder.build())
            .build()
    }
}
