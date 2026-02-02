package com.bookiibookii.bookiibookii.lib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.LibReview
import com.bookiibookii.bookiibookii.databinding.ItemLibDetailReviewBinding
import com.bumptech.glide.Glide

class LibraryReviewAdapter(
    private val onItemClick: (LibReview) -> Unit,
    private val onBookmarkClick: (LibReview, Int) -> Unit // 북마크 클릭 콜백 추가 (아이템, 포지션)
) : RecyclerView.Adapter<LibraryReviewAdapter.ReviewViewHolder>() {

    private var items: List<LibReview> = emptyList()
    // 간단한 로컬 상태 관리를 위해 북마크 ID 셋을 사용할 수도 있음 (실제로는 ViewModel 연동 권장)

    inner class ReviewViewHolder(private val binding: ItemLibDetailReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LibReview) {
            binding.itemReviewNameIv.text = item.userName
            binding.itemReviewTextTv.text = item.content
            binding.itemReviewPageTv.text = "${item.page}pg" // 페이지 추가됨

            // 이미지 로드
            if (item.reviewImageUri != null) {
                binding.itemReviewPhotoIv.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(item.reviewImageUri)
                    .into(binding.itemReviewPhotoIv)
            } else {
                // 이미지가 없으면 ImageView를 숨기거나 기본 이미지 처리
                // XML 구조상 숨기는게 나을 수 있음
                binding.itemReviewPhotoIv.visibility = View.GONE
            }

            if (item.profileImage != null) {
                binding.itemReviewProfileIv.setImageResource(item.profileImage)
            }

            val isBookmarked = item.isBookmarked ?: false
            binding.itemReviewBookmark.setImageResource(
                if (isBookmarked) R.drawable.ic_bookmark_orange
                else R.drawable.ic_bookmark_gray
            )

            // 아이템 전체 클릭
            itemView.setOnClickListener { onItemClick(item) }

            // [북마크 버튼 클릭]
            binding.itemReviewBookmark.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onBookmarkClick(item, pos)
                }
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

    fun notifyItemChangedAt(position: Int) {
        notifyItemChanged(position)
    }
}