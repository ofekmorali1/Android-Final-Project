package com.example.foodieshare.ui.Review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foodieshare.data.remote.PlacesRemoteDataSource
import com.example.foodieshare.data.repository.PlacesRepository
import com.example.foodieshare.data.repository.ReviewRepository
import com.example.foodieshare.data.repository.UsersRepository

class ReviewViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            val reviewRepo = ReviewRepository()
            val placesRepo = PlacesRepository(PlacesRemoteDataSource())
            val usersRepo = UsersRepository()
            @Suppress("UNCHECKED_CAST")
            return ReviewViewModel(reviewRepo, placesRepo, usersRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
