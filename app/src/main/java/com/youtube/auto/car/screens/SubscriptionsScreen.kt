package com.youtube.auto.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import com.youtube.auto.data.model.Subscription
import com.youtube.auto.data.repository.YouTubeRepository
import com.youtube.auto.util.Result
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

class SubscriptionsScreen(carContext: CarContext) : Screen(carContext) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SubscriptionsEntryPoint {
        fun youTubeRepository(): YouTubeRepository
    }

    private val repository: YouTubeRepository by lazy {
        EntryPointAccessors.fromApplication(
            carContext.applicationContext,
            SubscriptionsEntryPoint::class.java
        ).youTubeRepository()
    }

    @Volatile private var subscriptions: List<Subscription> = emptyList()
    @Volatile private var isLoading = true
    @Volatile private var errorMessage: String? = null

    init {
        loadSubscriptions()
    }

    private fun loadSubscriptions() {
        lifecycleScope.launch {
            isLoading = true
            invalidate()

            when (val result = repository.getSubscriptions()) {
                is Result.Success -> {
                    subscriptions = result.data.take(6)
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
                .setTitle("Subscriptions")
                .setHeaderAction(Action.BACK)
                .setLoading(true)
                .build()
        }

        if (errorMessage != null) {
            return MessageTemplate.Builder(errorMessage!!)
                .setTitle("Subscriptions")
                .setHeaderAction(Action.BACK)
                .addAction(
                    Action.Builder()
                        .setTitle("Retry")
                        .setOnClickListener { loadSubscriptions() }
                        .build()
                )
                .build()
        }

        if (subscriptions.isEmpty()) {
            return MessageTemplate.Builder("No subscriptions found. Sign in to see your subscriptions.")
                .setTitle("Subscriptions")
                .setHeaderAction(Action.BACK)
                .build()
        }

        val listBuilder = ItemList.Builder()
        for (sub in subscriptions) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(sub.channelTitle)
                    .addText(sub.description.take(60).ifEmpty { "No description" })
                    .setOnClickListener {
                        screenManager.push(ChannelVideosScreen(carContext, sub.channelId, sub.channelTitle))
                    }
                    .setBrowsable(true)
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setTitle("Subscriptions")
            .setHeaderAction(Action.BACK)
            .setSingleList(listBuilder.build())
            .build()
    }
}
