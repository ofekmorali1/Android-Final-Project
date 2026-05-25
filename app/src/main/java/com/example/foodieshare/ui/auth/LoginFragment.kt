package com.example.foodieshare.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.foodieshare.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        btnRegister = view.findViewById(R.id.btnRegister)
        progressBar = view.findViewById(R.id.progressBar)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        view.findViewById<MaterialButton>(R.id.btnGoogle)?.setOnClickListener {
            signInWithGoogle()
        }

        observeViewModel()

        return view
    }

    private fun signInWithGoogle() {
        val credentialManager = CredentialManager.create(requireContext())

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = requireContext(),
                    request = request
                )
                val credential = result.credential

                Log.d("LoginFragment", "Credential Class: ${credential.javaClass.name}")
                Log.d("LoginFragment", "Credential Type: ${credential.type}")

                if (credential is androidx.credentials.CustomCredential) {
                    if (credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                        viewModel.signInWithCredential(firebaseCredential)
                    } else {
                        Toast.makeText(context, "Unexpected Custom Type: ${credential.type}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Unexpected Class: ${credential.javaClass.simpleName}", Toast.LENGTH_LONG).show()
                }
            } catch (e: GetCredentialException) {
                Log.e("LoginFragment", "Google sign in failed", e)
                Toast.makeText(context, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    btnLogin.isEnabled = false
                }
                is AuthViewModel.AuthState.Success -> {
                    progressBar.visibility = View.GONE
                    Log.d("AUTH_STATE", "Success reached")
                    findNavController().navigate(R.id.action_loginFragment_to_feedFragment)
                }
                is AuthViewModel.AuthState.Error -> {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                is AuthViewModel.AuthState.Idle -> {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                }
            }
        }
    }
}