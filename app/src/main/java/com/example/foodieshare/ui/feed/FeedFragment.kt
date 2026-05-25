package com.example.foodieshare.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.foodieshare.R

// Fragment showing list of reviews in RecyclerView
// Each item shows:
// - User profile image + name
// - Restaurant name
// - Rating (hearts)
// - Description
// - Review image
// Clicking item opens ReviewDetailFragment
// Uses ReviewViewModel.reviewsLiveData
class FeedFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }
}
