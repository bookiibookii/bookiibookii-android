package com.bookiibookii.bookiibookii.lib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.CardItem
import com.bookiibookii.bookiibookii.databinding.ItemLibDetailReviewBinding

class LibraryReviewAdapter(
    private val onItemClick: (CardItem) -> Unit,
    private val onBookmarkClick: (CardItem, Int) -> Unit
) : ListAdapter<CardItem, LibraryReviewAdapter.ReviewViewHolder>(DiffCallback) {

    // DiffUtil을 사용하여 변경된 아이템만 부드럽게 갱신합니다.
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CardItem>() {
            override fun areItemsTheSame(oldItem: CardItem, newItem: CardItem): Boolean {
                return oldItem.cardId == newItem.cardId
            }

            override fun areContentsTheSame(oldItem: CardItem, newItem: CardItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemLibDetailReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReviewViewHolder(private val binding: ItemLibDetailReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CardItem) {
            binding.itemReviewNameIv.text = item.creatorName
            binding.itemReviewTextTv.text = item.memo
            binding.itemReviewPageTv.text = "${item.page}pg"

            binding.itemReviewBookmark.setImageResource(
                if (item.isBookmarked) R.drawable.ic_bookmark_orange
                else R.drawable.ic_bookmark_gray
            )

            // 카드 이미지 처리
            val cardImageUrl = item.cardImage?.presignedGetUrl
            if (!cardImageUrl.isNullOrEmpty()) {
                binding.itemReviewPhotoIv.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(cardImageUrl)
                    .transform(CenterCrop(), RoundedCorners(dpToPx(5)))
                    .placeholder(R.drawable.bg_round_8dp_gray300)
                    .error(R.drawable.bg_round_8dp_gray300)
                    .into(binding.itemReviewPhotoIv)
            } else {
                binding.itemReviewPhotoIv.visibility = View.GONE
            }

            // [개선됨] 서버 통신 제거. CardItem에 미리 준비된 프로필 URL 사용
            // 단, CardItem 모델에 profileImageUrl 속성이 추가되어야 합니다.
            val profileUrl = item.profileImageUrl // TODO: CardItem 모델에 해당 필드 추가 필요
            Glide.with(itemView.context)
                .load(profileUrl)
                .transform(CenterCrop(), RoundedCorners(dpToPx(8)))
                .placeholder(R.drawable.img_profile_default)
                .error(R.drawable.img_profile_default)
                .into(binding.itemReviewProfileIv)

            // [개선됨] 서버 통신 제거. CardItem에 미리 준비된 댓글 수 사용
            // 단, CardItem 모델에 commentCount 속성이 추가되어야 합니다.
            binding.itemReviewChatTv.text = (item.commentCount ?: 0).toString()

            itemView.setOnClickListener {
                onItemClick(item)
            }

            binding.itemReviewBookmark.setOnClickListener {
                onBookmarkClick(item, bindingAdapterPosition)
            }
        }

        private fun dpToPx(dp: Int): Int {
            return (dp * itemView.context.resources.displayMetrics.density).toInt()
        }
    }
}