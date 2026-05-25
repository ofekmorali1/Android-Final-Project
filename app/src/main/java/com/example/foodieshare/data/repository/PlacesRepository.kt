package com.example.foodieshare.data.repository

import com.example.foodieshare.data.model.Place
import com.example.foodieshare.data.remote.PlacesRemoteDataSource
import com.example.foodieshare.ui.Review.PlaceSuggestion
import com.google.android.gms.maps.model.LatLng
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

    suspend fun searchRestaurants(
        query: String,
        locationRestriction: LocationRestriction? = null
    ): List<PlaceSuggestion> {
        val predictions = remote.searchPlaces(
            query = query,
            types = listOf(PlaceTypes.RESTAURANT),
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

    suspend fun getCityBounds(placeId: String): LocationRestriction? {
        val fields = listOf(Field.VIEWPORT, Field.LAT_LNG)
        val googlePlace = remote.getPlaceDetails(placeId, fields)

        val viewport = googlePlace?.viewport
        if (viewport != null) {
            return RectangularBounds.newInstance(viewport)
        }

        val latLng = googlePlace?.latLng
        if (latLng != null) {
            val offset = 0.05
            val sw = LatLng(latLng.latitude - offset, latLng.longitude - offset)
            val ne = LatLng(latLng.latitude + offset, latLng.longitude + offset)
            return RectangularBounds.newInstance(sw, ne)
        }

        return null
    }
}