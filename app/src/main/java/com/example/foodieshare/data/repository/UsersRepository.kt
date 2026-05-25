package com.example.foodieshare.data.repository

import android.net.Uri
import com.example.foodieshare.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UsersRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun getUserById(userId: String): User? {
        return try {
            usersCollection.document(userId).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfileImage(userId: String, imageUri: Uri): String {
        val fileName = "profile_images/$userId/${UUID.randomUUID()}"
        val ref = storage.reference.child(fileName)
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }
}
