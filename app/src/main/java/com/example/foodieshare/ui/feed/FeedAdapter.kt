package com.example.foodieshare.ui.feed

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodieshare.R
import com.example.foodieshare.data.model.Review
import com.example.foodieshare.databinding.ItemReviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import androidx.core.content.ContextCompat
import android.view.MotionEvent

class FeedAdapter(
    private val onReviewClick: (Review) -> Unit,
    private val onEditClick: (Review) -> Unit,
    private val onDeleteClick: (Review) -> Unit
) : ListAdapter<Review, FeedAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(review: Review) {
            // Bind Author Info
            binding.tvUserName.text = review.authorName.ifEmpty { "Anonymous" }
            if (review.authorImageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(review.authorImageUrl)
                    .placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.ic_user_placeholder)
                    .into(binding.ivUserImage)
            } else {
                binding.ivUserImage.setImageResource(R.drawable.ic_user_placeholder)
            }

            binding.tvRestaurantName.text = review.restaurantName
            binding.tvAddress.text = review.address
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

            // Strict Ownership Filter
            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            val isOwner = review.userId == currentUserId

            binding.ivEdit.visibility = if (isOwner) View.VISIBLE else View.GONE
            binding.ivDelete.visibility = if (isOwner) View.VISIBLE else View.GONE

            if (!review.imageUrl.isNullOrEmpty()) {
                binding.ivReviewImage.visibility = View.VISIBLE
                Picasso.get().load(review.imageUrl).into(binding.ivReviewImage)
            } else {
                binding.ivReviewImage.visibility = View.GONE
            }

            val context = binding.root.context
            binding.ivDelete.setColorFilter(ContextCompat.getColor(context, R.color.icon_gray_default))
            binding.ivEdit.setColorFilter(ContextCompat.getColor(context, R.color.icon_gray_default))

            binding.ivDelete.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        binding.ivDelete.setColorFilter(ContextCompat.getColor(context, R.color.icon_red_pressed))
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        binding.ivDelete.setColorFilter(ContextCompat.getColor(context, R.color.icon_gray_default))
                    }
                }
                false
            }

            binding.ivEdit.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        binding.ivEdit.setColorFilter(ContextCompat.getColor(context, R.color.icon_blue_pressed))
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        binding.ivEdit.setColorFilter(ContextCompat.getColor(context, R.color.icon_gray_default))
                    }
                }
                false
            }

            binding.root.setOnClickListener { onReviewClick(review) }
            binding.ivEdit.setOnClickListener { onEditClick(review) }
            binding.ivDelete.setOnClickListener { onDeleteClick(review) }
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
