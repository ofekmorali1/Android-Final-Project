package com.example.foodieshare.ui.profile

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.foodieshare.R
import com.example.foodieshare.data.repository.ReviewRepository
import com.example.foodieshare.data.repository.UsersRepository
import com.example.foodieshare.databinding.FragmentProfileBinding
import com.example.foodieshare.ui.feed.FeedAdapter
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var adapter: FeedAdapter
    private var selectedImageUri: Uri? = null
    private var isEditMode = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfileImage.setImageURI(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        
        toggleEditMode(false)
    }

    private fun setupViewModel() {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                    return ProfileViewModel(UsersRepository(), ReviewRepository()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = FeedAdapter(
            onReviewClick = { /* Optional: Navigate to detail */ },
            onEditClick = { review ->
                val action = ProfileFragmentDirections.actionGlobalEditReviewFragment(review.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { review ->
                showDeleteConfirmationDialog(review.id)
            }
        )
        binding.rvMyReviews.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnEditProfile.setOnClickListener {
            isEditMode = !isEditMode
            toggleEditMode(isEditMode)
        }

        binding.ivProfileImage.setOnClickListener {
            if (isEditMode) {
                pickImageLauncher.launch("image/*")
            }
        }

        binding.btnSaveChanges.setOnClickListener {
            val newName = binding.etUserName.text.toString()
            if (newName.isNotEmpty()) {
                viewModel.updateProfile(newName, selectedImageUri)
            }
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.loginFragment)
        }
    }

    private fun toggleEditMode(enabled: Boolean) {
        binding.etUserName.isEnabled = enabled
        binding.ivProfileImage.isClickable = enabled
        binding.btnSaveChanges.visibility = if (enabled) View.VISIBLE else View.GONE
        
        val editIcon = if (enabled) R.drawable.ic_back else R.drawable.ic_edit
        binding.btnEditProfile.setImageResource(editIcon)
    }

    private fun observeViewModel() {
        viewModel.userLiveData.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.etUserName.setText(it.name)
                if (!it.profileImageUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(it.profileImageUrl)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(binding.ivProfileImage)
                }
            }
        }

        viewModel.myReviewsLiveData.observe(viewLifecycleOwner) { reviews ->
            adapter.submitList(reviews)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                isEditMode = false
                toggleEditMode(false)
                selectedImageUri = null
            }
        }
    }

    private fun showDeleteConfirmationDialog(reviewId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Review")
            .setMessage("Are you sure you want to permanently delete this review?")
            .setPositiveButton("Delete") { _, _ ->
                // Optimistic UI update
                val currentList = adapter.currentList.toMutableList()
                val itemToRemove = currentList.find { it.id == reviewId }
                itemToRemove?.let {
                    currentList.remove(it)
                    adapter.submitList(currentList)
                }
                viewModel.deleteReview(reviewId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}