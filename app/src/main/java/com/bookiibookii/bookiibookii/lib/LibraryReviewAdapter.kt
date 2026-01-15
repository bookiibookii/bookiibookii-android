package com.bookiibookii.bookiibookii.lib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.bookData.Data.LibReview
import com.bookiibookii.bookiibookii.databinding.ItemLibDetailReviewBinding
import com.bumptech.glide.Glide


class LibraryReviewAdapter(
    private val onItemClick: (LibReview) -> Unit
) : RecyclerView.Adapter<LibraryReviewAdapter.ReviewViewHolder>() {

    private var items: List<LibReview> = emptyList()

    inner class ReviewViewHolder(private val binding: ItemLibDetailReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LibReview) {
            binding.itemReviewNameIv.text = item.userName
            binding.itemReviewTextTv.text = item.content

            if (item.reviewImageUri != null) {
                binding.ivPhoto.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(item.reviewImageUri)
                    .into(binding.ivPhoto)
            } else {
                // 이미지가 없으면 공간을 숨기거나 기본 이미지 처리
                binding.ivPhoto.visibility = View.GONE
                // 또는 binding.ivPhoto.setImageResource(R.drawable.bg_round_8dp_gray300)
            }

            if (item.profileImage != null) {
                binding.itemReviewProfileIv.setImageResource(item.profileImage)
            } else {
                // 기본 프로필 이미지
                // binding.itemReviewProfileIv.setImageResource(R.drawable.default_profile)
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemLibDetailReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<LibReview>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}