package com.bookiibookii.bookiibookii.myPage.main

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.mypage.MypageBook
import com.bookiibookii.bookiibookii.databinding.ItemMypLateBookBinding

class MypLateBookAdapter(private val items: List<MypageBook>) :
    RecyclerView.Adapter<MypLateBookAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMypLateBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MypageBook) {
            binding.itemMypLateTitleTv.text = item.bookTitle

            // toInt()를 빼고 원본 소수점 값(Double or Float)을 그대로 사용합니다.
            val rating = item.rating

            // 별점 처리 (LinearLayout 내부의 ImageView들)
            for (i in 0 until binding.itemMypLateRatingLl.childCount) {
                val star = binding.itemMypLateRatingLl.getChildAt(i) as ImageView

                when {
                    rating >= (i + 1.0) -> {
                        // 1.0 이상 채워져야 하면 꽉 찬 별
                        star.setImageResource(R.drawable.ic_star_filled)
                    }
                    rating >= (i + 0.5) -> {
                        // 0.5 이상 채워져야 하면 반쪽 별
                        star.setImageResource(R.drawable.ic_star_half)
                    }
                    else -> {
                        // 그 외에는 빈 별
                        star.setImageResource(R.drawable.ic_star_none)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMypLateBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}