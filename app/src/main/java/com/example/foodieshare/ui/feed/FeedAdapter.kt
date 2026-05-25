package com.example.foodieshare.ui.feed

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodieshare.data.model.Review
import com.example.foodieshare.databinding.ItemReviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class FeedAdapter(
    private val onReviewClick: (Review) -> Unit,
    private val onEditClick: (Review) -> Unit
) : ListAdapter<Review, FeedAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review) {
            binding.tvRestaurantName.text = review.restaurantName
            binding.tvAddress.text = review.address // Binding the address
            binding.tvDescription.text = review.description
            binding.ratingBar.rating = review.rating
            
            review.timestamp?.let {
                val relativeTime = DateUtils.getRelativeTimeSpanString(
                    it.toDate().time,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
                binding.tvTimestamp.text = relativeTime
            }

            // check if review belongs to current user
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            binding.ivEdit.visibility = if (review.userId == currentUserId) View.VISIBLE else View.GONE

            if (!review.imageUrl.isNullOrEmpty()) {
                binding.ivReviewImage.visibility = View.VISIBLE
                Picasso.get().load(review.imageUrl).into(binding.ivReviewImage)
            } else {
                binding.ivReviewImage.visibility = View.GONE
            }

            binding.root.setOnClickListener { onReviewClick(review) }
            binding.ivEdit.setOnClickListener { onEditClick(review) }
        }
    }

    class ReviewDiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem == newItem
        }
    }
}
