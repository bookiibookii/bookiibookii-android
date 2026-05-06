package com.bookiibookii.bookiibookii.myPage.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.data.model.mypage.RelayReview
import com.bookiibookii.bookiibookii.databinding.ItemMypMyReviewBinding
import com.bumptech.glide.Glide

class MypMyReviewAdapter(private var items: List<RelayReview>) : RecyclerView.Adapter<MypMyReviewAdapter.ViewHolder>() {

    private val expandedState = mutableMapOf<Long, Boolean>()

    fun submitList(newItems: List<RelayReview>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMypMyReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RelayReview) {
            val context = binding.root.context

            binding.itemMypReviewTitleTv.text = item.partnerNickname
            binding.itemMypReviewBookTitleTv.text = item.bookTitle
            binding.itemMypReviewBookAuthorTv.text = ""

            // ★ 1. 시작일 ~ 종료일 순서 명확화 및 DateUtils 적용
            val start = DateUtils.formatDate(item.startDate)
            val end = DateUtils.formatDate(item.finishedDate)
            binding.itemMypReviewDateTv.text = "$start ~ $end"

            Glide.with(context)
                .load(item.bookImage)
                .placeholder(R.drawable.bg_round_10dp_gray300)
                .error(R.drawable.bg_round_10dp_gray300)
                .into(binding.itemMypReviewCoverIv)

            val tags = item.partnerBadges.map { it.description }
            val count = tags.size

            if (count >= 1) {
                binding.tag1.text = "#${tags[0]}"
                binding.tag1.visibility = View.VISIBLE
            } else binding.tag1.visibility = View.GONE

            if (count >= 2) {
                binding.tag2.text = "#${tags[1]}"
                binding.tag2.visibility = View.VISIBLE
            } else binding.tag2.visibility = View.GONE

            if (count > 2) {
                binding.tag3.text = "+${count - 2}"
                binding.tag3.visibility = View.VISIBLE
            } else binding.tag3.visibility = View.GONE

            setStars(binding.itemMypReviewRateList, item.partnerBookRating)
            binding.itemMypMyReviewTv.text = item.partnerBookComment
            setStars(binding.itemMypPartnerReviewRateList, item.partnerToMeRating)
            binding.itemMypPartnerCommentTv.text = item.partnerToMeComment

            val expandedTags = listOf(
                binding.partnerTag1, binding.partnerTag2, binding.partnerTag3,
                binding.partnerTag4, binding.partnerTag5, binding.partnerTag6
            )

            expandedTags.forEachIndexed { index, textView ->
                if (index < tags.size) {
                    textView.text = "#${tags[index]}"
                    textView.visibility = View.VISIBLE
                } else {
                    textView.visibility = View.GONE
                }
            }

            val isExpanded = expandedState[item.groupId] ?: false

            if (isExpanded) {
                binding.layoutExpanded.visibility = View.VISIBLE
                binding.layoutTags.visibility = View.GONE
                binding.itemMypArrowIv.rotation = 180f
            } else {
                binding.layoutExpanded.visibility = View.GONE
                binding.layoutTags.visibility = View.VISIBLE
                binding.itemMypArrowIv.rotation = 0f
            }

            // ★ 2. 화살표뿐만 아니라 아이템 전체(root)를 클릭해도 펼쳐지도록 변경
            binding.root.setOnClickListener {
                val newState = !isExpanded
                expandedState[item.groupId] = newState
                notifyItemChanged(bindingAdapterPosition)
            }
        }

        private fun setStars(container: LinearLayout, rating: Double) {
            val ratingInt = rating.toInt()
            val hasHalfStar = (rating - ratingInt) >= 0.5

            for (i in 0 until container.childCount) {
                val star = container.getChildAt(i) as? ImageView ?: continue
                when {
                    i < ratingInt -> star.setImageResource(R.drawable.ic_star_filled)
                    i == ratingInt && hasHalfStar -> star.setImageResource(R.drawable.ic_star_half)
                    else -> star.setImageResource(R.drawable.ic_star_none)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMypMyReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}