package com.example.foodieshare.ui.Review

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodieshare.data.model.Place
import com.example.foodieshare.data.model.Review
import com.example.foodieshare.data.repository.PlacesRepository
import com.example.foodieshare.data.repository.ReviewRepository
import com.example.foodieshare.data.repository.UsersRepository
import com.google.android.libraries.places.api.model.LocationRestriction
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val reviewRepository: ReviewRepository,
    private val placesRepository: PlacesRepository,
    private val usersRepository: UsersRepository,
) : ViewModel() {

    private val _reviewsLiveData = reviewRepository.getReviews()
    val reviewsLiveData: LiveData<List<Review>> = _reviewsLiveData
    private val _cityRestrictionReady = MutableLiveData<Boolean>()
    val cityRestrictionReady: LiveData<Boolean> = _cityRestrictionReady
    private val _citySuggestions = MutableLiveData<List<PlaceSuggestion>>()
    val citySuggestions: LiveData<List<PlaceSuggestion>> = _citySuggestions

    private val _restaurantSuggestions = MutableLiveData<List<PlaceSuggestion>>()
    val restaurantSuggestions: LiveData<List<PlaceSuggestion>> = _restaurantSuggestions

    private val _selectedCityRestriction = MutableLiveData<LocationRestriction?>()

    private val _selectedPlace = MutableLiveData<Place?>()
    val selectedPlace: LiveData<Place?> = _selectedPlace

    private val _reviewDetailLiveData = MutableLiveData<ReviewUiModel?>()
    val reviewDetailLiveData: LiveData<ReviewUiModel?> = _reviewDetailLiveData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _createReviewSuccess = MutableLiveData<Boolean>()
    val createReviewSuccess: LiveData<Boolean> = _createReviewSuccess
    fun createReview(review: Review, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = reviewRepository.createReview(review, imageUri)
            _isLoading.value = false
            if (result.isSuccess) {
                _createReviewSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to upload review"
            }
        }
    }

    fun loadReviewById(reviewId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val review = reviewRepository.getReviewById(reviewId)
            if (review != null) {
                val user = usersRepository.getUserById(review.userId)
                _reviewDetailLiveData.value = ReviewUiModel(review, user)
            } else {
                _error.value = "Review not found"
            }
            _isLoading.value = false
        }
    }

    fun searchCities(query: String) {
        viewModelScope.launch {
            val results = placesRepository.searchCities(query)
            _citySuggestions.value = results
        }
    }

    fun onCitySelected(cityId: String) {
        viewModelScope.launch {
            _isLoading.value = true // מציג אנימציית טעינה בזמן שמביאים את הגבולות
            val bounds = placesRepository.getCityBounds(cityId)
            _selectedCityRestriction.value = bounds
            _cityRestrictionReady.value = true // מודיע ל-UI שהגבולות מוכנים!
            _isLoading.value = false
        }
    }

    fun clearCityBias() {
        _selectedCityRestriction.value = null
        _restaurantSuggestions.value = emptyList()
        _selectedPlace.value = null
        _cityRestrictionReady.value = false // נועל בחזרה את שדה המסעדה
    }

    fun searchRestaurants(query: String) {
        viewModelScope.launch {
            val results = placesRepository.searchRestaurants(query, _selectedCityRestriction.value)
            _restaurantSuggestions.value = results
        }
    }

    fun onRestaurantSelected(placeId: String) {
        viewModelScope.launch {
            val details = placesRepository.getPlaceDetails(placeId)
            _selectedPlace.value = details
        }
    }
    
    fun resetCreateReviewSuccess() {
        _createReviewSuccess.value = false
    }
}
