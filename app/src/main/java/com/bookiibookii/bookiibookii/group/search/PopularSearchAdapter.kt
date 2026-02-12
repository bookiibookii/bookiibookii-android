package com.bookiibookii.bookiibookii.group.search

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

    // Data Update
    fun submitList(newItems: List<PopularSearchItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }


    // Adapter Overrides
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemPopularSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size


    // [ViewHolder
    inner class Holder(private val binding: ItemPopularSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PopularSearchItem) {
            with(binding) {
                // 데이터 매핑
                itemPopularSearchRankTv.text = item.rank.toString()
                itemPopularSearchKeywordTv.text = item.keyword

                // 순위에 따른 스타일링 (1~3위 강조)
                val isTopRank = item.rank <= 3
                val rankColor = if (isTopRank) R.color.pre_main else R.color.grey_500
                val keywordColor = if (isTopRank) R.color.grey_900 else R.color.grey_500

                itemPopularSearchRankTv.setTextColor(ContextCompat.getColor(root.context, rankColor))
                itemPopularSearchKeywordTv.setTextColor(ContextCompat.getColor(root.context, keywordColor))

                // 클릭 리스너
                root.setOnClickListener { onItemClick(item.keyword) }
            }
        }
    }
}