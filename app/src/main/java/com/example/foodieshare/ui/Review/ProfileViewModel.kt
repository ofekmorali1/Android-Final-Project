package com.example.foodieshare.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodieshare.data.model.Review
import com.example.foodieshare.data.model.User
import com.example.foodieshare.data.repository.ReviewRepository
import com.example.foodieshare.data.repository.UsersRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val usersRepository: UsersRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    private val _userLiveData = MutableLiveData<User?>()
    val userLiveData: LiveData<User?> = _userLiveData

    // עכשיו זה משתנה שאנחנו שולטים עליו ידנית!
    private val _myReviewsLiveData = MutableLiveData<List<Review>>()
    val myReviewsLiveData: LiveData<List<Review>> = _myReviewsLiveData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    init {
        loadUserProfile()
        loadMyReviews() // טעינה ראשונית כשנכנסים למסך
    }

    private fun loadUserProfile() {
        if (currentUserId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            val user = usersRepository.getUserById(currentUserId)
            _userLiveData.value = user
            _isLoading.value = false
        }
    }

    // הפונקציה החדשה שמנקה את המסך ומושכת הכל מחדש
    private fun loadMyReviews() {
        if (currentUserId.isEmpty()) return
        viewModelScope.launch {
            _myReviewsLiveData.value = emptyList() // מנקה את המסך מיד!

            // שולף מחדש מהשרת
            val freshReviews = reviewRepository.fetchReviewsByUserId(currentUserId)
            _myReviewsLiveData.value = freshReviews
        }
    }

    fun updateProfile(newName: String, newImageUri: Uri?) {
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                var photoUrl = _userLiveData.value?.profileImageUrl ?: ""

                if (newImageUri != null) {
                    photoUrl = usersRepository.uploadProfileImage(currentUserId, newImageUri)
                }

                val updatedUser = User(
                    id = currentUserId,
                    name = newName,
                    email = _userLiveData.value?.email ?: "",
                    profileImageUrl = photoUrl
                )

                val result = usersRepository.saveUser(updatedUser)
                if (result.isSuccess) {
                    // מעדכן את כל הביקורות בפיירבייס
                    reviewRepository.updateAllUserReviews(currentUserId, newName, photoUrl)

                    _userLiveData.value = updatedUser
                    _updateSuccess.value = true

                    // --- כאן הקסם! טוען את כל הביקורות מחדש אחרי השמירה ---
                    loadMyReviews()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            reviewRepository.deleteReview(reviewId)
        }
    }
}

