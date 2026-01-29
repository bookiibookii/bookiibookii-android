package com.bookiibookii.bookiibookii.myPage.main

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.MypReview
import com.bookiibookii.bookiibookii.databinding.ItemMypReviewTagBinding

class MypReviewAdapter(private val items: List<MypReview>) :
    RecyclerView.Adapter<MypReviewAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMypReviewTagBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MypReview) {
            // 텍스트 합치기 (예: "친절하고 매너가 좋아요 8")
            val fullText = "${item.content} ${item.count}"
            val spannable = SpannableString(fullText)

            // 숫자 부분만 색상 변경 (pre_main)
            val color = ContextCompat.getColor(itemView.context, R.color.pre_main)
            val start = fullText.length - item.count.toString().length
            val end = fullText.length

            spannable.setSpan(
                ForegroundColorSpan(color),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            binding.tvReviewTag.text = spannable
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMypReviewTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}