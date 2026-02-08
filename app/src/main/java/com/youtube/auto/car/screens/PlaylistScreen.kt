package com.youtube.auto.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import com.youtube.auto.data.model.Playlist
import com.youtube.auto.data.repository.YouTubeRepository
import com.youtube.auto.util.Result
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

class PlaylistScreen(carContext: CarContext) : Screen(carContext) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PlaylistEntryPoint {
        fun youTubeRepository(): YouTubeRepository
    }

    private val repository: YouTubeRepository by lazy {
        EntryPointAccessors.fromApplication(
            carContext.applicationContext,
            PlaylistEntryPoint::class.java
        ).youTubeRepository()
    }

    @Volatile private var playlists: List<Playlist> = emptyList()
    @Volatile private var isLoading = true
    @Volatile private var errorMessage: String? = null

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        lifecycleScope.launch {
            isLoading = true
            invalidate()

            when (val result = repository.getMyPlaylists()) {
                is Result.Success -> {
                    playlists = result.data.take(6)
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
                .setTitle("Playlists")
                .setHeaderAction(Action.BACK)
                .setLoading(true)
                .build()
        }

        if (errorMessage != null) {
            return MessageTemplate.Builder(errorMessage!!)
                .setTitle("Playlists")
                .setHeaderAction(Action.BACK)
                .addAction(
                    Action.Builder()
                        .setTitle("Retry")
                        .setOnClickListener { loadPlaylists() }
                        .build()
                )
                .build()
        }

        if (playlists.isEmpty()) {
            return MessageTemplate.Builder("No playlists found")
                .setTitle("Playlists")
                .setHeaderAction(Action.BACK)
                .build()
        }

        val listBuilder = ItemList.Builder()
        for (playlist in playlists) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(playlist.title.take(60))
                    .addText("${playlist.itemCount} videos")
                    .setOnClickListener {
                        screenManager.push(PlaylistVideosScreen(carContext, playlist.id, playlist.title))
                    }
                    .setBrowsable(true)
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setTitle("Playlists")
            .setHeaderAction(Action.BACK)
            .setSingleList(listBuilder.build())
            .build()
    }
}
