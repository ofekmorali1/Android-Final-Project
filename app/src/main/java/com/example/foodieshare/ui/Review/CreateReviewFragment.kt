package com.example.foodieshare.ui.Review

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.foodieshare.R
import com.example.foodieshare.data.model.Place
import com.example.foodieshare.data.model.Review
import com.example.foodieshare.data.remote.PlacesRemoteDataSource
import com.example.foodieshare.data.repository.AuthRepository
import com.example.foodieshare.data.repository.PlacesRepository
import com.example.foodieshare.data.repository.ReviewRepository
import com.example.foodieshare.data.repository.UsersRepository
import com.example.foodieshare.databinding.FragmentCreateReviewBinding
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

class CreateReviewFragment : Fragment() {

    private var _binding: FragmentCreateReviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReviewViewModel
    private var selectedImageUri: Uri? = null
    private var selectedPlace: Place? = null

    private lateinit var cityAdapter: PlaceSuggestionAdapter
    private lateinit var restaurantAdapter: PlaceSuggestionAdapter

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivImagePreview.visibility = View.VISIBLE
            binding.ivImagePreview.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupUI()
        observeViewModel()
    }

    private fun setupViewModel() {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
                    val reviewRepo = ReviewRepository()
                    val placesRepo = PlacesRepository(PlacesRemoteDataSource())
                    val usersRepo = UsersRepository()
                    return ReviewViewModel(reviewRepo, placesRepo, usersRepo) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        viewModel = ViewModelProvider(this, factory)[ReviewViewModel::class.java]
    }

    private fun setupUI() {
        cityAdapter = PlaceSuggestionAdapter(requireContext())
        binding.autoCompleteCity.setAdapter(cityAdapter)

        restaurantAdapter = PlaceSuggestionAdapter(requireContext())
        binding.autoCompleteRestaurant.setAdapter(restaurantAdapter)

        binding.autoCompleteCity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.autoCompleteCity.isPerformingCompletion) return
                
                // Requirement 5: Reset Logic
                binding.autoCompleteRestaurant.setText("")
                binding.tilRestaurantName.isEnabled = false
                binding.etAddress.setText("")
                viewModel.clearCityBias()
                
                if (s != null && s.length > 1) {
                    viewModel.searchCities(s.toString())
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.autoCompleteCity.setOnItemClickListener { parent, _, position, _ ->
            val suggestion = parent.getItemAtPosition(position) as PlaceSuggestion
            viewModel.onCitySelected(suggestion.id)
        }

        binding.autoCompleteRestaurant.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.autoCompleteRestaurant.isPerformingCompletion) return

                binding.etAddress.setText("")
                if (s != null && s.length > 2) {
                    viewModel.searchRestaurants(s.toString())
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.autoCompleteRestaurant.setOnItemClickListener { parent, _, position, _ ->
            val suggestion = parent.getItemAtPosition(position) as PlaceSuggestion
            viewModel.onRestaurantSelected(suggestion.id)
        }

        binding.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSubmit.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun observeViewModel() {
        viewModel.citySuggestions.observe(viewLifecycleOwner) { suggestions ->
            cityAdapter.updateData(suggestions)
            if (binding.autoCompleteCity.hasFocus() && suggestions.isNotEmpty()) {
                binding.autoCompleteCity.showDropDown()
            }
        }

        viewModel.restaurantSuggestions.observe(viewLifecycleOwner) { suggestions ->
            restaurantAdapter.updateData(suggestions)
            if (binding.autoCompleteRestaurant.hasFocus() && suggestions.isNotEmpty()) {
                binding.autoCompleteRestaurant.showDropDown()
            }
        }

        viewModel.cityRestrictionReady.observe(viewLifecycleOwner) { ready ->
            binding.tilRestaurantName.isEnabled = ready
            if (ready) {
                binding.autoCompleteRestaurant.requestFocus()
            }
        }

        viewModel.selectedPlace.observe(viewLifecycleOwner) { place ->
            selectedPlace = place
            binding.etAddress.setText(place?.address ?: "")
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSubmit.isEnabled = !isLoading
            setInputsEnabled(!isLoading)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.createReviewSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Review uploaded successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateReviewSuccess()
                findNavController().popBackStack(R.id.feedFragment, false)
            }
        }
    }

    private fun setInputsEnabled(enabled: Boolean) {
        binding.tilCity.isEnabled = enabled
        binding.tilRestaurantName.isEnabled = enabled && binding.autoCompleteCity.text.isNotEmpty()
        binding.tilAddress.isEnabled = enabled
        binding.tilDescription.isEnabled = enabled
        binding.ratingBar.isEnabled = enabled
        binding.btnSelectImage.isEnabled = enabled
    }

    private fun validateAndSubmit() {
        val city = binding.autoCompleteCity.text.toString()
        val restaurantName = binding.autoCompleteRestaurant.text.toString()
        val address = binding.etAddress.text.toString()
        val rating = binding.ratingBar.rating
        val description = binding.etDescription.text.toString()

        if (city.isEmpty()) {
            binding.tilCity.error = "City is required"
            return
        }
        binding.tilCity.error = null

        if (restaurantName.isEmpty()) {
            binding.tilRestaurantName.error = "Restaurant name is required"
            return
        }
        binding.tilRestaurantName.error = null

        if (rating == 0f) {
            Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = AuthRepository().getCurrentUser()
        if (currentUser == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val review = Review(
            userId = currentUser.uid,
            restaurantName = restaurantName,
            address = address,
            rating = rating,
            description = description,
            timestamp = Timestamp.now()
        )

        viewModel.createReview(review, selectedImageUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
