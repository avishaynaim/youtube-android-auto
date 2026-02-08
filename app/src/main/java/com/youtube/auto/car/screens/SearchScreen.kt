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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchScreen(carContext: CarContext) : Screen(carContext) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SearchScreenEntryPoint {
        fun youTubeRepository(): YouTubeRepository
    }

    private val repository: YouTubeRepository by lazy {
        EntryPointAccessors.fromApplication(
            carContext.applicationContext,
            SearchScreenEntryPoint::class.java
        ).youTubeRepository()
    }

    @Volatile private var searchResults: List<Video> = emptyList()
    @Volatile private var isLoading = false
    @Volatile private var hasSearched = false
    @Volatile private var errorMessage: String? = null
    private var searchJob: Job? = null

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            isLoading = true
            hasSearched = true
            invalidate()

            when (val result = repository.searchVideos(query)) {
                is Result.Success -> {
                    searchResults = result.data.videos.take(6)
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
        val searchCallbackBuilder = object : SearchTemplate.SearchCallback {
            override fun onSearchTextChanged(searchText: String) {
                // No-op: only search on submit to avoid excessive API calls
            }

            override fun onSearchSubmitted(searchText: String) {
                if (searchText.isNotBlank()) {
                    performSearch(searchText)
                }
            }
        }

        val builder = SearchTemplate.Builder(searchCallbackBuilder)
            .setHeaderAction(Action.BACK)
            .setShowKeyboardByDefault(true)

        if (isLoading) {
            builder.setLoading(true)
        } else if (hasSearched && searchResults.isNotEmpty()) {
            val listBuilder = ItemList.Builder()
            for (video in searchResults) {
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
            builder.setItemList(listBuilder.build())
        } else if (hasSearched) {
            builder.setItemList(ItemList.Builder().setNoItemsMessage("No results found").build())
        }

        return builder.build()
    }
}
