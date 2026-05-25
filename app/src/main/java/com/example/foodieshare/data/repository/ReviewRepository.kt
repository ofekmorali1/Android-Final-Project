package com.example.foodieshare.data.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodieshare.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ReviewRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val reviewsCollection = firestore.collection("reviews")

    suspend fun createReview(review: Review, imageUri: Uri?): Result<Unit> {
        return try {
            val documentRef = reviewsCollection.document()
            val id = documentRef.id
            var finalReview = review.copy(id = id)
            
            if (imageUri != null) {
                val fileName = UUID.randomUUID().toString()
                val imageRef = storage.reference.child("review_images/$fileName")
                imageRef.putFile(imageUri).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()
                finalReview = finalReview.copy(imageUrl = downloadUrl)
            }
            documentRef.set(finalReview).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReview(review: Review, imageUri: Uri?): Result<Unit> {
        return try {
            var finalReview = review
            if (imageUri != null && !imageUri.toString().startsWith("http")) {
                val fileName = UUID.randomUUID().toString()
                val imageRef = storage.reference.child("review_images/$fileName")
                imageRef.putFile(imageUri).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()
                finalReview = review.copy(imageUrl = downloadUrl)
            }
            reviewsCollection.document(review.id).set(finalReview).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getReviews(): LiveData<List<Review>> {
        val liveData = MutableLiveData<List<Review>>()
        reviewsCollection.orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ReviewRepository", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val reviews = snapshot.toObjects(Review::class.java)
                    Log.d("ReviewRepository", "Fetched ${reviews.size} reviews from Firestore")
                    liveData.value = reviews
                }
            }
        return liveData
    }

    suspend fun getReviewById(reviewId: String): Review? {
        return try {
            reviewsCollection.document(reviewId).get().await().toObject(Review::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteReview(reviewId: String): Result<Unit> {
        return try {
            reviewsCollection.document(reviewId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
