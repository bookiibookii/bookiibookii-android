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

class GroupAdapter(private val itemList: List<GroupData>) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        // 아이템 레이아웃 XML 이름이 item_grp_card.xml 이라고 가정
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grp_card, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val item = itemList[position]

        // 1. 텍스트 데이터 연결
        holder.tvTitle.text = item.bookTitle
        holder.tvAuthor.text = item.bookAuthor
        holder.tvGenre.text = item.bookGenre
        holder.chipStatus.text = item.status
        holder.tvDeadline.text = item.deadline
        holder.tvMemberCount.text = item.memberCount
        holder.tvNickname.text = item.nickname
        holder.tvDate.text = item.date

        // 2. HOT 태그 표시 로직 (true면 보이고, false면 숨김)
        if (item.isHot) {
            holder.chipHot.visibility = View.VISIBLE
        } else {
            holder.chipHot.visibility = View.GONE
        }

        // 3. 이미지 로딩 (Glide 사용)
        // (1) 책 표지 (둥근 모서리)
        Glide.with(holder.itemView.context)
            .load(item.coverImgUrl)
            .transform(CenterCrop(), RoundedCorners(30)) // 약 10dp 정도의 둥글기 (px 단위)
            .placeholder(R.drawable.bg_round_10dp_gray300)
            .into(holder.ivCover)

        // (2) 프로필 이미지 (원형)
        Glide.with(holder.itemView.context)
            .load(item.profileImgUrl)
            .transform(CenterCrop(), CircleCrop())
            .placeholder(R.drawable.bg_round_10dp_gray500)
            .into(holder.ivProfile)

        // 4. 해시태그 로직
        val chipList = listOf(holder.chipHash1, holder.chipHash2, holder.chipHash3, holder.chipHash4, holder.chipHash5)

        // 일단 다 숨김
        chipList.forEach { it.visibility = View.GONE }

        // 데이터 개수만큼만 보여주고 텍스트 설정
        for (i in item.tags.indices) {
            if (i < chipList.size) {
                chipList[i].text = item.tags[i]
                chipList[i].visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int = itemList.size

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // XML의 ID들과 연결
        val ivCover: ImageView = itemView.findViewById(R.id.grp_item_cover_Iv)
        val tvTitle: TextView = itemView.findViewById(R.id.grp_item_book_title_Tv)
        val tvAuthor: TextView = itemView.findViewById(R.id.grp_item_book_author_Tv)
        val tvGenre: TextView = itemView.findViewById(R.id.grp_item_book_sort_Tv)
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