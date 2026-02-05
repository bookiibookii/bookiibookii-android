package com.bookiibookii.bookiibookii.myPage.main

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.MypReview // 기존 데이터 클래스 유지한다고 가정
import com.bookiibookii.bookiibookii.databinding.ItemMypReviewTagBinding

class MypReviewAdapter(private val items: List<MypReview>) :
    RecyclerView.Adapter<MypReviewAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMypReviewTagBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MypReview) {
            val countStr = item.count.toString()
            val fullText = "${item.content} $countStr" // 공백 명시적 추가
            val spannable = SpannableString(fullText)

            val color = ContextCompat.getColor(itemView.context, R.color.pre_main)

            // 뒤에서부터 숫자 위치 찾기 (안전한 방식)
            val start = fullText.lastIndexOf(countStr)

            if (start != -1) {
                spannable.setSpan(
                    ForegroundColorSpan(color),
                    start,
                    fullText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
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