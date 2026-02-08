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

class VideoPlayerScreen(
    carContext: CarContext,
    private val video: Video
) : Screen(carContext) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface VideoPlayerEntryPoint {
        fun historyRepository(): HistoryRepository
    }

    private val historyRepository: HistoryRepository by lazy {
        EntryPointAccessors.fromApplication(
            carContext.applicationContext,
            VideoPlayerEntryPoint::class.java
        ).historyRepository()
    }

    init {
        lifecycleScope.launch {
            historyRepository.addToHistory(video)
        }
    }

    override fun onGetTemplate(): Template {
        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle("Play on Phone")
                    .setOnClickListener {
                        playOnPhone()
                    }
                    .build()
            )
            .build()

        return PaneTemplate.Builder(
            Pane.Builder()
                .addRow(
                    Row.Builder()
                        .setTitle(video.title)
                        .addText(video.channelTitle)
                        .addText("${video.formattedViewCount} views • ${video.formattedDuration}")
                        .build()
                )
                .addRow(
                    Row.Builder()
                        .setTitle("Description")
                        .addText(video.description.take(200).ifEmpty { "No description available" })
                        .build()
                )
                .addAction(
                    Action.Builder()
                        .setTitle("Play")
                        .setBackgroundColor(CarColor.RED)
                        .setOnClickListener { playOnPhone() }
                        .build()
                )
                .build()
        )
            .setTitle(video.title.take(50))
            .setHeaderAction(Action.BACK)
            .setActionStrip(actionStrip)
            .build()
    }

    private fun playOnPhone() {
        val videoIdRegex = Regex("^[a-zA-Z0-9_-]{1,20}$")
        if (!videoIdRegex.matches(video.id)) {
            CarToast.makeText(carContext, "Invalid video ID", CarToast.LENGTH_SHORT).show()
            return
        }
        val intent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("https://www.youtube.com/watch?v=${video.id}")
        ).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        carContext.startActivity(intent)
        CarToast.makeText(carContext, "Opening on phone...", CarToast.LENGTH_SHORT).show()
    }
}
