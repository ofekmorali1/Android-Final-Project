package com.example.foodieshare.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Review(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorImageUrl: String = "",
    val city: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val address: String = "",
    val rating: Float = 0f,
    val description: String = "",
    val imageUrl: String? = null,
    val timestamp: Timestamp? = null
)
