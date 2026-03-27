package com.example.foodieshare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodieshare.data.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import android.content.Intent

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    init {
        _user.value = repository.getCurrentUser()
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.loginUser(email, password)
            if (result.isSuccess) {
                _user.value = repository.getCurrentUser()
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.registerUser(email, password)
            if (result.isSuccess) {
                android.util.Log.d("AUTH_VIEWMODEL", "Google login success")
                _user.value = repository.getCurrentUser()
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun signInWithCredential(credential: AuthCredential) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.signInWithCredential(credential)
            if (result.isSuccess) {
                _user.value = repository.getCurrentUser()
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Sign in failed")
            }
        }
    }

    /**
     * Logs out the current user.
     */
    fun logout() {
        repository.logout()
        _user.value = null
        _authState.value = AuthState.Idle
    }

    /**
     * Represents the different states of the authentication process.
     */
    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
