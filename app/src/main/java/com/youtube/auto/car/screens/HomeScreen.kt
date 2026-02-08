package com.youtube.auto.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*

class HomeScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()

        listBuilder.addItem(
            Row.Builder()
                .setTitle("Trending")
                .addText("Popular videos right now")
                .setOnClickListener {
                    screenManager.push(TrendingScreen(carContext))
                }
                .setBrowsable(true)
                .build()
        )

        listBuilder.addItem(
            Row.Builder()
                .setTitle("Search")
                .addText("Search for videos")
                .setOnClickListener {
                    screenManager.push(SearchScreen(carContext))
                }
                .setBrowsable(true)
                .build()
        )

        listBuilder.addItem(
            Row.Builder()
                .setTitle("Subscriptions")
                .addText("Your subscribed channels")
                .setOnClickListener {
                    screenManager.push(SubscriptionsScreen(carContext))
                }
                .setBrowsable(true)
                .build()
        )

        listBuilder.addItem(
            Row.Builder()
                .setTitle("Playlists")
                .addText("Your playlists")
                .setOnClickListener {
                    screenManager.push(PlaylistScreen(carContext))
                }
                .setBrowsable(true)
                .build()
        )

        listBuilder.addItem(
            Row.Builder()
                .setTitle("History")
                .addText("Recently watched")
                .setOnClickListener {
                    screenManager.push(HistoryScreen(carContext))
                }
                .setBrowsable(true)
                .build()
        )

        listBuilder.addItem(
            Row.Builder()
                .setTitle("Settings")
                .addText("App preferences")
                .setOnClickListener {
                    screenManager.push(SettingsScreen(carContext))
                }
                .setBrowsable(true)
                .build()
        )

        return ListTemplate.Builder()
            .setTitle("YouTube Auto")
            .setHeaderAction(Action.APP_ICON)
            .setSingleList(listBuilder.build())
            .build()
    }
}
