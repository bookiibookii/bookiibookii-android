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
import com.bookiibookii.bookiibookii.databinding.ItemLibDetailBookBinding

class LibraryBookmarkAdapter(
    private val itemClickListener: (CardItem) -> Unit
) : ListAdapter<CardItem, LibraryBookmarkAdapter.BookmarkViewHolder>(DiffCallback) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val binding = ItemLibDetailBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookmarkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookmarkViewHolder(private val binding: ItemLibDetailBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CardItem) {
            binding.itemReviewTitleTv.text = item.bookTitle
            binding.itemReviewPageTv.text = "${item.page}pg"
            binding.itemReviewTextTv.text = item.memo
            binding.itemReviewNameIv.text = item.creatorName

            val imageUrl = item.cardImage?.presignedGetUrl
            if (!imageUrl.isNullOrEmpty()) {
                binding.itemReviewPhotoIv.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .transform(CenterCrop(), RoundedCorners(dpToPx(5)))
                    .placeholder(R.drawable.bg_round_8dp_gray300)
                    .into(binding.itemReviewPhotoIv)
            } else {
                binding.itemReviewPhotoIv.visibility = View.GONE
            }

            // [개선됨] 코루틴 통신 제거 및 CardItem에 포함된 프로필 URL 바로 사용
            val profileUrl = item.profileImageUrl // TODO: CardItem 모델에 해당 필드 추가 필요
            Glide.with(itemView.context)
                .load(profileUrl)
                .transform(CenterCrop(), RoundedCorners(dpToPx(10)))
                .placeholder(R.drawable.img_profile_default)
                .error(R.drawable.img_profile_default)
                .into(binding.itemReviewProfileIv)

            itemView.setOnClickListener {
                itemClickListener(item)
            }
        }

        private fun dpToPx(dp: Int): Int {
            return (dp * itemView.context.resources.displayMetrics.density).toInt()
        }
    }
}