package com.example.foodieshare.ui.Review

import com.example.foodieshare.data.model.Review
import com.example.foodieshare.data.model.User

data class ReviewUiModel(
    val review: Review,
    val user: User?
)
