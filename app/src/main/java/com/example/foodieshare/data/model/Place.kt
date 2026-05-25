package com.example.foodieshare.data.model

data class Place(
    val name: String,
    val address: String,
    val imageUrls: List<String> = emptyList()
) {
    override fun toString(): String = name
}
