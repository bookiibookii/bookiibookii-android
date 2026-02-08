package com.bookiibookii.bookiibookii.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.chip.Chip

class GroupAdapter(
    private var itemList: List<GroupData>,
    private val itemClick: (GroupData) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grp_card, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val item = itemList[position]

        holder.tvTitle.text = item.bookTitle
        holder.tvAuthor.text = item.bookAuthor
        holder.chipStatus.text = item.status
        holder.tvDeadline.text = item.readingPeriod
        holder.tvMemberCount.text = item.memberCount
        holder.tvNickname.text = item.nickname
        holder.tvDate.text = item.date

        val genreText = item.genre // "소설"
        if (genreText.isNotEmpty()) {
            holder.tvGenre.text = "($genreText)" // "소설" -> "(소설)"
        } else {
            holder.tvGenre.text = "" // 장르가 비어있으면 아무것도 표시 안 함
        }

        if (item.isHot) {
            holder.chipHot.visibility = View.VISIBLE
        } else {
            holder.chipHot.visibility = View.GONE
        }

        Glide.with(holder.itemView.context)
            .load(item.coverImgUrl)
            .transform(CenterCrop(), RoundedCorners(30))
            .placeholder(R.drawable.bg_round_10dp_gray300)
            .into(holder.ivCover)

        Glide.with(holder.itemView.context)
            .load(item.profileImgUrl)
            .transform(CenterCrop(), CircleCrop())
            .placeholder(R.drawable.bg_round_10dp_gray500)
            .into(holder.ivProfile)

        val chipList = listOf(holder.chipHash1, holder.chipHash2, holder.chipHash3, holder.chipHash4, holder.chipHash5)

        chipList.forEach { it.visibility = View.GONE }

        for (i in item.tags.indices) {
            if (i < chipList.size) {
                chipList[i].text = item.tags[i]
                chipList[i].visibility = View.VISIBLE
            }
        }

        holder.itemView.setOnClickListener {
            itemClick(item)
        }
    }

    override fun getItemCount(): Int = itemList.size

    // 외부(Activity)에서 검색 결과를 새로 넣어주는 함수 추가
    fun updateList(newList: List<GroupData>) {
        this.itemList = newList
        notifyDataSetChanged() // 리스트 뷰 새로고침
    }
    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.grp_item_cover_Iv)
        val tvTitle: TextView = itemView.findViewById(R.id.grp_item_book_title_Tv)
        val tvAuthor: TextView = itemView.findViewById(R.id.grp_item_book_author_Tv)
        val tvGenre: TextView = itemView.findViewById(R.id.grp_item_book_genre_Tv)
        val chipStatus: Chip = itemView.findViewById(R.id.grp_item_status_Cp)
        val tvDeadline: TextView = itemView.findViewById(R.id.grp_item_deadlineNo_Tv)
        val tvMemberCount: TextView = itemView.findViewById(R.id.grp_item_mem_statusNo_Tv)
        val chipHot: Chip = itemView.findViewById(R.id.grp_item_hot_Cp)
        val ivProfile: ImageView = itemView.findViewById(R.id.grp_item_profile_Iv)
        val tvNickname: TextView = itemView.findViewById(R.id.grp_item_nickname_Tv)
        val tvDate: TextView = itemView.findViewById(R.id.grp_item_date_Tv)
        val chipHash1: Chip = itemView.findViewById(R.id.grp_item_hash1_Cp)
        val chipHash2: Chip = itemView.findViewById(R.id.grp_item_hash2_Cp)
        val chipHash3: Chip = itemView.findViewById(R.id.grp_item_hash3_Cp)
        val chipHash4: Chip = itemView.findViewById(R.id.grp_item_hash4_Cp)
        val chipHash5: Chip = itemView.findViewById(R.id.grp_item_hash5_Cp)
    }
}