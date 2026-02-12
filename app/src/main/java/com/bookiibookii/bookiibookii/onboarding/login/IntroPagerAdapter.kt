package com.bookiibookii.bookiibookii.onboarding.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R

class IntroPagerAdapter(
    private val items: List<Int>
) : RecyclerView.Adapter<IntroPagerAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_intro_image, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.iv.setImageResource(items[position])
        // ✅ ViewPager2에서는 itemView 폭을 바꾸면 크래시남. (Pages must fill the whole ViewPager2)
        // 캐러셀 효과는 item 레이아웃 내부에서 이미지 크기를 줄여서 구현해야 함.
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv: ImageView = itemView.findViewById(R.id.iv_item)
    }
}