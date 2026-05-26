package com.example.foodieshare.data.remote

import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.LocationRestriction
import kotlinx.coroutines.tasks.await

class PlacesRemoteDataSource {
    private val placesClient by lazy { PlacesClientProvider.getClient() }

    suspend fun searchPlaces(
        query: String,
        types: List<String> = emptyList(),
        locationBias: LocationBias? = null,
        locationRestriction: LocationRestriction? = null
    ): List<AutocompletePrediction> {
        val requestBuilder = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setTypesFilter(types)
        
        if (locationBias != null) {
            requestBuilder.setLocationBias(locationBias)
        }
        
        if (locationRestriction != null) {
            requestBuilder.setLocationRestriction(locationRestriction)
        }

        return try {
            val response = placesClient.findAutocompletePredictions(requestBuilder.build()).await()
            response.autocompletePredictions
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPlaceDetails(placeId: String, fields: List<Place.Field>): Place? {
        val request = FetchPlaceRequest.newInstance(placeId, fields)
        return try {
            val response = placesClient.fetchPlace(request).await()
            response.place
        } catch (e: Exception) {
            null
        }
    }
}
