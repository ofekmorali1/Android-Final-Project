package com.example.foodieshare.data.remote

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

object PlacesClientProvider {
    private var isInitialized = false
    private lateinit var _placesClient: PlacesClient

    fun init(context: Context, apiKey: String) {
        if (!isInitialized) {
            Places.initialize(context, apiKey)
            _placesClient = Places.createClient(context)
            isInitialized = true
        }
    }

    fun getClient(): PlacesClient {
        if (!isInitialized) {
            throw IllegalStateException("PlacesClientProvider must be initialized with context and API key")
        }
        return _placesClient
    }
}
