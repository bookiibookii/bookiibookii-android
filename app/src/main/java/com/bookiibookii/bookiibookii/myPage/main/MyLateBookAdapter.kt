package com.bookiibookii.bookiibookii.myPage.main

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.MypLateBook
import com.bookiibookii.bookiibookii.databinding.ItemMypLateBookBinding

class MypLateBookAdapter(private val items: List<MypLateBook>) :
    RecyclerView.Adapter<MypLateBookAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMypLateBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MypLateBook) {
            binding.itemMypLateTitleTv.text = item.title

            // 별점 처리 (LinearLayout 내부의 ImageView들)
            for (i in 0 until binding.itemMypLateRatingLl.childCount) {
                val star = binding.itemMypLateRatingLl.getChildAt(i) as ImageView
                if (i < item.rating) {
                    star.setImageResource(R.drawable.ic_star_filled) // 채워진 별
                } else {
                    // 빈 별 아이콘이 있다면 교체, 없다면 투명도 조절 등
                    // star.setImageResource(R.drawable.ic_star_empty)
                    star.alpha = 0.3f // 예시로 투명도 처리
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