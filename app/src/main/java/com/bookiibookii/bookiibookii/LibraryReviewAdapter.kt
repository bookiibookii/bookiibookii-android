package com.bookiibookii.bookiibookii

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.bookData.Data.LibBook
import com.bookiibookii.bookiibookii.bookData.Data.LibReview
import com.bookiibookii.bookiibookii.databinding.ItemLibDetailReviewBinding

class LibraryReviewAdapter(private var items: List<LibReview>) :
    RecyclerView.Adapter<LibraryReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(private val binding: ItemLibDetailReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LibReview) {
            binding.itemReviewNameIv.text = item.userName
            binding.itemReviewTextTv.text = item.content

            // 이미지 설정
            // Glide.with(binding.root).load(item.reviewImage).into(binding.ivPhoto)

            // 임시
            item.reviewImage?.let { binding.ivPhoto.setImageResource(it) }
            // 프로필 이미지
            item.profileImage?.let { binding.itemReviewProfileIv.setImageResource(it) }
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

    // 데이터 갱신
    fun submitList(newItems: List<LibReview>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}