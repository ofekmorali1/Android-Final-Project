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
import androidx.credentials.exceptions.NoCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.foodieshare.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class RegisterFragment : Fragment() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var btnLoginBack: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        etFullName = view.findViewById(R.id.etFullName)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnRegister = view.findViewById(R.id.btnRegister)
        btnLoginBack = view.findViewById(R.id.btnLoginBack)
        progressBar = view.findViewById(R.id.progressBar)

        btnRegister.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(context, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(name, email, password)
        }

        btnLoginBack.setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<Button>(R.id.btnGoogle)?.setOnClickListener {
            signInWithGoogle()
        }

        observeViewModel()

        return view
    }

    private fun signInWithGoogle() {
        val credentialManager = CredentialManager.create(requireContext())

        // Generate a nonce for security
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .setNonce(hashedNonce)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = requireActivity(),
                    request = request
                )
                val credential = result.credential

                if (credential is GoogleIdTokenCredential) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
                    viewModel.signInWithCredential(firebaseCredential)
                } else {
                    Log.e("RegisterFragment", "Unexpected credential type")
                }
            } catch (e: NoCredentialException) {
                Log.e("RegisterFragment", "No credentials available. Check SHA-1 and Package Name in Firebase.", e)
                Toast.makeText(context, "No Google accounts available on this device.", Toast.LENGTH_LONG).show()
            } catch (e: GetCredentialException) {
                Log.e("RegisterFragment", "Google sign in failed", e)
                Toast.makeText(context, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    btnRegister.isEnabled = false
                }
                is AuthViewModel.AuthState.Success -> {
                    progressBar.visibility = View.GONE
                    findNavController().navigate(R.id.action_registerFragment_to_feedFragment)
                }
                is AuthViewModel.AuthState.Error -> {
                    progressBar.visibility = View.GONE
                    btnRegister.isEnabled = true
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                is AuthViewModel.AuthState.Idle -> {
                    progressBar.visibility = View.GONE
                    btnRegister.isEnabled = true
                }
            }
        }
    }
}
