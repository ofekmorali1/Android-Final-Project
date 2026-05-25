package com.example.foodieshare.ui.Review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foodieshare.data.repository.PlacesRepository
import com.example.foodieshare.data.repository.ReviewRepository
import com.example.foodieshare.data.repository.UsersRepository

class ReviewViewModelFactory(
    private val reviewRepository: ReviewRepository,
    private val placesRepository: PlacesRepository,
    private val usersRepository: UsersRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReviewViewModel(reviewRepository, placesRepository, usersRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
