package com.example.foodieshare.data.model

import com.google.firebase.Timestamp

data class Review(
    val id: String = "",
    val userId: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val address: String = "",
    val rating: Float = 0f,
    val description: String = "",
    val imageUrl: String? = null,
    val timestamp: Timestamp? = null
)
