package com.example.foodieshare.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.foodieshare.R

// Android Fragment for user login
// MVVM architecture
// uses AuthViewModel
// contains EditText for email and password
// contains Login button and Register navigation button
// on successful login navigate to FeedFragment using Navigation component
// uses LiveData observer for authentication state
class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }
}
