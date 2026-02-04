package com.bookiibookii.bookiibookii.group

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.ItemPopularSearchBinding

class PopularSearchAdapter(
    private var items: List<PopularSearchItem> = emptyList(),
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<PopularSearchAdapter.Holder>() {

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }
    fun submitList(newItems: List<PopularSearchItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemPopularSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }


    override fun getItemCount(): Int = items.size

    inner class Holder(private val binding: ItemPopularSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PopularSearchItem) {
            binding.itemPopularSearchRankTv.text = item.rank.toString()
            binding.itemPopularSearchKeywordTv.text = item.keyword

            val context = binding.root.context

            // ★ [핵심 로직] 4위부터는 회색(grey_500), 1~3위는 원래 색상
            if (item.rank >= 4) {
                // 4위 이상: 회색
                val greyColor = ContextCompat.getColor(context, R.color.grey_500) // colors.xml에 정의된 색상
                binding.itemPopularSearchRankTv.setTextColor(greyColor)
                binding.itemPopularSearchKeywordTv.setTextColor(greyColor)
            } else {
                // 1~3위: 랭킹은 메인컬러, 키워드는 검정 (원복)
                binding.itemPopularSearchRankTv.setTextColor(ContextCompat.getColor(context, R.color.pre_main))
                binding.itemPopularSearchKeywordTv.setTextColor(ContextCompat.getColor(context, R.color.grey_900))
            }

            itemView.setOnClickListener {
                onItemClick(item.keyword)
            }
        }
    }
}