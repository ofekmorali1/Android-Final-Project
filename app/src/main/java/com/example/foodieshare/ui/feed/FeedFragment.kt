package com.example.foodieshare.ui.feed

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.foodieshare.data.remote.PlacesRemoteDataSource
import com.example.foodieshare.data.repository.PlacesRepository
import com.example.foodieshare.data.repository.ReviewRepository
import com.example.foodieshare.data.repository.UsersRepository
import com.example.foodieshare.databinding.FragmentFeedBinding
import com.example.foodieshare.ui.Review.ReviewViewModel
import com.example.foodieshare.ui.Review.ReviewViewModelFactory

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReviewViewModel
    private lateinit var adapter: FeedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupViewModel() {
        val factory = ReviewViewModelFactory(
            ReviewRepository(),
            PlacesRepository(PlacesRemoteDataSource()),
            UsersRepository()
        )
        viewModel = ViewModelProvider(this, factory)[ReviewViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = FeedAdapter(
            onReviewClick = { review ->
            },
            onEditClick = { review ->
                val action = FeedFragmentDirections.actionGlobalEditReviewFragment(review.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { review ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Review")
                    .setMessage("Are you sure you want to permanently delete this review?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteReview(review.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.rvFeed.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.reviewsLiveData.observe(viewLifecycleOwner) { reviews ->
            adapter.submitList(reviews)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}