package com.example.foodieshare.data.repository

import com.example.foodieshare.data.model.Place
import com.example.foodieshare.data.remote.PlacesRemoteDataSource
import com.example.foodieshare.ui.Review.PlaceSuggestion
import com.google.android.libraries.places.api.model.LocationRestriction
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.Place as GooglePlace

class PlacesRepository(private val remote: PlacesRemoteDataSource) {

    suspend fun searchCities(query: String): List<PlaceSuggestion> {
        val predictions = remote.searchPlaces(
            query = query,
            types = listOf(PlaceTypes.CITIES)
        )
        return predictions.map { prediction ->
            PlaceSuggestion(
                id = prediction.placeId,
                primaryText = prediction.getPrimaryText(null).toString(),
                secondaryText = prediction.getSecondaryText(null).toString()
            )
        }
    }

    /**
     * Strictly bound results to the location restriction and filter for restaurants.
     */
    suspend fun searchRestaurants(
        query: String,
        locationRestriction: LocationRestriction? = null
    ): List<PlaceSuggestion> {
        val predictions = remote.searchPlaces(
            query = query,
            types = listOf(PlaceTypes.ESTABLISHMENT),
            locationRestriction = locationRestriction
        )
        return predictions.map { prediction ->
            PlaceSuggestion(
                id = prediction.placeId,
                primaryText = prediction.getPrimaryText(null).toString(),
                secondaryText = prediction.getSecondaryText(null).toString()
            )
        }
    }

    suspend fun getPlaceDetails(placeId: String): Place? {
        val fields = listOf(Field.NAME, Field.ADDRESS, Field.LAT_LNG)
        val googlePlace = remote.getPlaceDetails(placeId, fields) ?: return null
        return Place(
            name = googlePlace.name ?: "",
            address = googlePlace.address ?: "",
            imageUrls = emptyList()
        )
    }

    /**
     * Step 1 & 5: Fetches the strict geographic bounds of a city.
     * Uses Viewport if available for strict bounding, otherwise falls back to a point.
     */
    suspend fun getCityBounds(placeId: String): LocationRestriction? {
        val fields = listOf(Field.VIEWPORT, Field.LAT_LNG)
        val googlePlace = remote.getPlaceDetails(placeId, fields)

        val viewport = googlePlace?.viewport
        if (viewport != null) {
            return RectangularBounds.newInstance(viewport)
        }

        val latLng = googlePlace?.latLng
        if (latLng != null) {
            // If no viewport, fallback to a small area around the LatLng point
            return RectangularBounds.newInstance(latLng, latLng)
        }

        return null
    }
}
