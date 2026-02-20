package com.bookiibookii.bookiibookii.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.RecommendedBookmateDto

class MateRecommendAdapter(
    private val onClick: (RecommendedBookmateDto) -> Unit
) : RecyclerView.Adapter<MateRecommendAdapter.VH>() {

    private var items: List<RecommendedBookmateDto> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<RecommendedBookmateDto>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_mate_card, parent, false)
        return VH(v, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(
        itemView: View,
        private val onClick: (RecommendedBookmateDto) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivProfile: ImageView = itemView.findViewById(R.id.iv_mate_profile)
        private val tvName: TextView = itemView.findViewById(R.id.tv_mate_name)
        private val tvRecent: TextView = itemView.findViewById(R.id.tv_recent_book)
        private val tvTag1: TextView = itemView.findViewById(R.id.tv_tag_1)
        private val tvTag2: TextView = itemView.findViewById(R.id.tv_tag_2)

        private fun translateTag(raw: String): String {
            return when (raw.trim().uppercase()) {
                "MEMO" -> "메모환영"
                "POSTIT" -> "포스트잇"
                "CLEAN" -> "깔끔"
                "SERIOUS" -> "진지함"
                "LIGHT_FUN" -> "재미있게"
                "INSIGHT" -> "인사이트"
                else -> raw
            }
        }

        fun bind(item: RecommendedBookmateDto) {
            tvName.text = item.nickname
            tvRecent.text = item.recentBookTitle ?: "최근 완독한 책이 없어요"

            val tags = item.matchedTags.orEmpty()

            if (tags.isNotEmpty()) {
                tvTag1.visibility = View.VISIBLE
                tvTag1.text = "#${translateTag(tags[0])}"
            } else {
                tvTag1.visibility = View.GONE
            }

            if (tags.size >= 2) {
                tvTag2.visibility = View.VISIBLE
                tvTag2.text = "#${translateTag(tags[1])}"
            } else {
                tvTag2.visibility = View.GONE
            }

            itemView.setOnClickListener { onClick(item) }

            val url = item.profileImageUrl
            if (!url.isNullOrBlank()) {
                ivProfile.load(url) {
                    crossfade(true)
                    placeholder(R.drawable.img_profile_default)
                    error(R.drawable.img_profile_default)
                }
            } else {
                ivProfile.setImageResource(R.drawable.img_profile_default)
            }
        }
    }
}