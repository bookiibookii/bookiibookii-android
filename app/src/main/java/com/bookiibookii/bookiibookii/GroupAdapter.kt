package com.bookiibookii.bookiibookii

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.chip.Chip

class GroupAdapter(private val itemList: List<GroupData>) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grp_card, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val item = itemList[position]

        // --- Glide로 이미지 로딩 ---

        //  책 표지 이미지 (둥근 모서리 처리: 10dp -> 약 30px 정도로 설정)
        Glide.with(holder.itemView.context)
            .load(item.coverImgUrl) // URL 로드
            .transform(CenterCrop(), RoundedCorners(30)) // 꽉 채우고 + 모서리 깎기
            .placeholder(R.drawable.bg_round_10dp_gray300) // 로딩 중에 보여줄 배경
            .error(R.drawable.bg_round_10dp_gray300)       // 에러 났을 때 보여줄 배경
            .into(holder.ivCover)

        // 프로필 이미지 (완전 원형 처리)
        Glide.with(holder.itemView.context)
            .load(item.profileImgUrl)
            .transform(CenterCrop(), CircleCrop()) // 꽉 채우고 + 원형으로 깎기
            .placeholder(R.drawable.bg_circle_gray500)
            .into(holder.ivProfile)


        // ---텍스트 및 칩 데이터 연결 ---
        holder.tvDate.text = "등록일 ${item.date}"
        holder.chipStatus.text = item.status
        holder.tvNickname.text = item.nickname
        holder.tvTitle.text = item.bookTitle
        holder.tvAuthor.text = item.bookAuthor
        holder.tvMemberCount.text = item.memberCount
        holder.tvDeadline.text = item.deadline

        // 해시태그 로직
        val chips = listOf(holder.chipHash1, holder.chipHash2, holder.chipHash3, holder.chipHash4, holder.chipHash5)
        chips.forEach { it.visibility = View.GONE }
        for (i in item.tags.indices) {
            if (i < chips.size) {
                chips[i].text = item.tags[i]
                chips[i].visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int = itemList.size

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.grp_item_cover_Iv)
        val ivProfile: ImageView = itemView.findViewById(R.id.grp_item_profile_Iv)

        val tvDate: TextView = itemView.findViewById(R.id.grp_item_date_Tv)
        val chipStatus: Chip = itemView.findViewById(R.id.grp_item_status_Cp)
        val tvNickname: TextView = itemView.findViewById(R.id.grp_item_nickname_Tv)
        val tvTitle: TextView = itemView.findViewById(R.id.grp_item_book_title_Tv)
        val tvAuthor: TextView = itemView.findViewById(R.id.grp_item_book_author_Tv)
        val tvMemberCount: TextView = itemView.findViewById(R.id.grp_item_group_member_No_Tv)
        val tvDeadline: TextView = itemView.findViewById(R.id.grp_item_deadline_Tv)
        val chipHash1: Chip = itemView.findViewById(R.id.grp_item_hash1_Cp)
        val chipHash2: Chip = itemView.findViewById(R.id.grp_item_hash2_Cp)
        val chipHash3: Chip = itemView.findViewById(R.id.grp_item_hash3_Cp)
        val chipHash4: Chip = itemView.findViewById(R.id.grp_item_hash4_Cp)
        val chipHash5: Chip = itemView.findViewById(R.id.grp_item_hash5_Cp)
    }
}