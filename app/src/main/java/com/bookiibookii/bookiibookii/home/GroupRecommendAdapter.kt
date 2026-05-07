package com.bookiibookii.bookiibookii.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.recommendation.RecommendedGroupItem

class GroupRecommendAdapter(
    private val onItemClick: ((RecommendedGroupItem) -> Unit)? = null
) : ListAdapter<RecommendedGroupItem, GroupRecommendAdapter.ViewHolder>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_group_card, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onItemClick: ((RecommendedGroupItem) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivCover: ImageView = itemView.findViewById(R.id.iv_book_cover)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_book_title)

        fun bind(item: RecommendedGroupItem) {
            tvTitle.text = item.bookTitle ?: "제목 없음"

            val url = item.bookImageUrl

            // TODO: 추후 로그 삭제
            android.util.Log.d("GROUP_IMG", "url=$url")

            if (!url.isNullOrBlank()) {
                ivCover.load(url) {
                    crossfade(true)
                    placeholder(R.drawable.bg_book_cover_frame)
                    error(R.drawable.bg_book_cover_frame)

                    // TODO: 추후 로그 삭제
                    listener(
                        onSuccess = { _, _ ->
                            android.util.Log.d("COIL", "SUCCESS: $url")
                        },
                        onError = { _, result ->
                            android.util.Log.e("COIL", "ERROR: $url", result.throwable)
                        }
                    )
                }
            } else {
                ivCover.setImageResource(R.drawable.bg_book_cover_frame)

                // TODO: 추후 로그 삭제
                android.util.Log.d("COIL", "SKIP: url is null/blank")
            }

            itemView.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<RecommendedGroupItem>() {
            override fun areItemsTheSame(oldItem: RecommendedGroupItem, newItem: RecommendedGroupItem): Boolean {
                return oldItem.groupId == newItem.groupId
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: RecommendedGroupItem, newItem: RecommendedGroupItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
