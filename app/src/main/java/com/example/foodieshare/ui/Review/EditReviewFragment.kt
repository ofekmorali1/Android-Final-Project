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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.foodieshare.data.model.Place
import com.example.foodieshare.data.model.Review
import com.example.foodieshare.data.remote.PlacesRemoteDataSource
import com.example.foodieshare.data.repository.AuthRepository
import com.example.foodieshare.data.repository.PlacesRepository
import com.example.foodieshare.data.repository.ReviewRepository
import com.example.foodieshare.data.repository.UsersRepository
import com.example.foodieshare.databinding.FragmentEditReviewBinding
import com.squareup.picasso.Picasso

class EditReviewFragment : Fragment() {

    private var isImageDeleted = false
    private var _binding: FragmentEditReviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReviewViewModel
    private val args: EditReviewFragmentArgs by navArgs()
    
    private var selectedImageUri: Uri? = null
    private var originalReview: Review? = null
    private var selectedPlace: Place? = null

    private lateinit var cityAdapter: PlaceSuggestionAdapter
    private lateinit var restaurantAdapter: PlaceSuggestionAdapter

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivImagePreview.setImageURI(it)
            binding.ivImagePreview.visibility = View.VISIBLE
            binding.btnRemoveImage.visibility = View.VISIBLE
            binding.btnSelectImage.text = "Change Image"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupUI()
        observeViewModel()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })

        viewModel.loadReviewById(args.reviewId)
    }

    private fun setupViewModel() {
        val factory = ReviewViewModelFactory(
            ReviewRepository(),
            PlacesRepository(PlacesRemoteDataSource()),
            UsersRepository()
        )
        viewModel = ViewModelProvider(this, factory)[ReviewViewModel::class.java]
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            handleBackPress()
        }

        cityAdapter = PlaceSuggestionAdapter(requireContext())
        binding.autoCompleteCity.setAdapter(cityAdapter)

        restaurantAdapter = PlaceSuggestionAdapter(requireContext())
        binding.autoCompleteRestaurant.setAdapter(restaurantAdapter)

        binding.autoCompleteCity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.autoCompleteCity.isPerformingCompletion) return
                
                // If user changes city, we reset restaurant
                binding.autoCompleteRestaurant.setText("")
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
            binding.tilRestaurantName.isEnabled = true
        }

        binding.autoCompleteRestaurant.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.autoCompleteRestaurant.isPerformingCompletion) return
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
            updateReview()
        }

        binding.btnRemoveImage.setOnClickListener {
            selectedImageUri = null
            isImageDeleted = true
            binding.ivImagePreview.setImageDrawable(null)
            binding.ivImagePreview.visibility = View.GONE
            binding.btnRemoveImage.visibility = View.GONE
            binding.btnSelectImage.text = "Select Image"
        }
    }

    private fun observeViewModel() {
        viewModel.reviewDetailLiveData.observe(viewLifecycleOwner) { detail ->
            detail?.let {
                originalReview = it.review
                prefillFields(it.review)
            }
        }

        viewModel.citySuggestions.observe(viewLifecycleOwner) { suggestions ->
            cityAdapter.updateData(suggestions)
            if (binding.autoCompleteCity.hasFocus()) binding.autoCompleteCity.showDropDown()
        }

        viewModel.restaurantSuggestions.observe(viewLifecycleOwner) { suggestions ->
            restaurantAdapter.updateData(suggestions)
            if (binding.autoCompleteRestaurant.hasFocus()) binding.autoCompleteRestaurant.showDropDown()
        }

        viewModel.selectedPlace.observe(viewLifecycleOwner) { place ->
            selectedPlace = place
            binding.etAddress.setText(place?.address ?: "")
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSubmit.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.createReviewSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Review updated successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateReviewSuccess()
                findNavController().popBackStack()
            }
        }
    }

    private fun prefillFields(review: Review) {
        binding.autoCompleteCity.setText(review.city, false)
        binding.autoCompleteRestaurant.setText(review.restaurantName, false)

        binding.etAddress.setText(review.address)
        binding.ratingBar.rating = review.rating
        binding.etDescription.setText(review.description)

        if (review.imageUrl.isNullOrEmpty()) {
            binding.ivImagePreview.visibility = View.GONE
            binding.btnRemoveImage.visibility = View.GONE
            binding.btnSelectImage.text = "Select Image"
        } else {
            binding.ivImagePreview.visibility = View.VISIBLE
            binding.btnRemoveImage.visibility = View.VISIBLE
            binding.btnSelectImage.text = "Change Image"
            Picasso.get().load(review.imageUrl).into(binding.ivImagePreview)
        }

        binding.tilRestaurantName.isEnabled = true
    }

    private fun updateReview() {
        val city = binding.autoCompleteCity.text.toString()
        val restaurantName = binding.autoCompleteRestaurant.text.toString()
        val address = binding.etAddress.text.toString()
        val rating = binding.ratingBar.rating
        val description = binding.etDescription.text.toString()
        val imageUrlToSave = if (isImageDeleted) "" else originalReview?.imageUrl ?: ""

        if (city.isEmpty()) {
            binding.tilCity.error = "Required"
            return
        }
        binding.tilCity.error = null

        if (restaurantName.isEmpty()) {
            binding.tilRestaurantName.error = "Required"
            return
        }
        binding.tilRestaurantName.error = null

        val updatedReview = originalReview?.copy(
            city = city,
            restaurantName = restaurantName,
            address = address,
            rating = rating,
            description = description,
            imageUrl = imageUrlToSave
        )

        updatedReview?.let {
            viewModel.updateReview(it, selectedImageUri)
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        if (originalReview == null) return false

        val currentCity = binding.autoCompleteCity.text.toString()
        val currentRestaurant = binding.autoCompleteRestaurant.text.toString()
        val currentAddress = binding.etAddress.text.toString()
        val currentRating = binding.ratingBar.rating
        val currentDescription = binding.etDescription.text.toString()
        val isImageChanged = selectedImageUri != null || isImageDeleted

        return currentCity != originalReview?.city ||
                currentRestaurant != originalReview?.restaurantName ||
                currentAddress != originalReview?.address ||
                currentRating != originalReview?.rating ||
                currentDescription != originalReview?.description ||
                isImageChanged
    }

    private fun handleBackPress() {
        if (hasUnsavedChanges()) {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Unsaved Changes")
                .setMessage("Do you want to save your changes before leaving?")
                .setPositiveButton("Save") { _, _ ->
                    updateReview()
                }
                .setNegativeButton("Discard") { _, _ ->
                    findNavController().popBackStack()
                }
                .setNeutralButton("Cancel", null)
                .show()
        } else {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
