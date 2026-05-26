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
            val userDoc = firestore.collection("users").document(review.userId).get().await()
            val authorName = userDoc.getString("name") ?: "Unknown User"
            val authorImageUrl = userDoc.getString("profileImageUrl") ?: ""
            val documentRef = reviewsCollection.document()
            val id = documentRef.id
            var finalReview = review.copy(
                id = id,
                authorName = authorName,
                authorImageUrl = authorImageUrl
            )
            
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
                finalReview = finalReview.copy(imageUrl = downloadUrl)
            }
            reviewsCollection.document(review.id).set(finalReview).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAllUserReviews(userId: String, newName: String, newImageUrl: String) {
        try {
            val querySnapshot = reviewsCollection.whereEqualTo("userId", userId).get().await()
            val batch = firestore.batch()

            for (document in querySnapshot.documents) {
                batch.update(document.reference, "authorName", newName)
                batch.update(document.reference, "authorImageUrl", newImageUrl)
            }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
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
                    liveData.value = reviews
                }
            }
        return liveData
    }

    fun getReviewsByUserId(userId: String): LiveData<List<Review>> {
        val liveData = MutableLiveData<List<Review>>()
        reviewsCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ReviewRepository", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val reviews = snapshot.toObjects(Review::class.java)
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

    suspend fun fetchReviewsByUserId(userId: String): List<Review> {
        return try {
            val snapshot = reviewsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val reviewsList = snapshot.toObjects(Review::class.java)
            reviewsList.sortedByDescending { it.timestamp }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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
