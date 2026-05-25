package com.example.foodieshare.ui.Review

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.foodieshare.R
import com.example.foodieshare.data.remote.PlacesRemoteDataSource
import com.example.foodieshare.data.repository.PlacesRepository
import com.example.foodieshare.data.repository.ReviewRepository
import com.example.foodieshare.data.repository.UsersRepository
import com.example.foodieshare.databinding.FragmentReviewDetailBinding
import com.squareup.picasso.Picasso

class ReviewDetailFragment : Fragment() {

    private var _binding: FragmentReviewDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReviewViewModel
    private val args: ReviewDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        observeViewModel()

        viewModel.loadReviewById(args.reviewId)
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

    private fun observeViewModel() {
        viewModel.reviewDetailLiveData.observe(viewLifecycleOwner) { uiModel ->
            uiModel?.let {
                bindReviewData(it)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun bindReviewData(uiModel: ReviewUiModel) {
        val review = uiModel.review
        val user = uiModel.user

        binding.tvRestaurantName.text = review.restaurantName
        binding.tvAddress.text = review.address
        binding.ratingBar.rating = review.rating
        binding.tvDescription.text = review.description
        
        binding.tvUserName.text = user?.name ?: "Anonymous"
        
        review.timestamp?.let {
            val relativeTime = DateUtils.getRelativeTimeSpanString(
                it.toDate().time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            binding.tvTimestamp.text = relativeTime
        }

        if (!user?.profileImageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(user?.profileImageUrl)
                .placeholder(R.drawable.ic_user_placeholder)
                .into(binding.ivUserImage)
        } else {
            binding.ivUserImage.setImageResource(R.drawable.ic_user_placeholder)
        }

        if (!review.imageUrl.isNullOrEmpty()) {
            binding.ivReviewImage.visibility = View.VISIBLE
            Picasso.get()
                .load(review.imageUrl)
                .into(binding.ivReviewImage)
        } else {
            binding.ivReviewImage.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
