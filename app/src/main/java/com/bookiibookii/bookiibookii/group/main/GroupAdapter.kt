package com.bookiibookii.bookiibookii.group.main

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat // ★ 컬러 처리를 위해 추가
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.GroupTagMapper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

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
        val context = holder.itemView.context

        // 1. 텍스트 정보 설정
        holder.tvTitle.text = item.bookTitle
        holder.tvAuthor.text = item.bookAuthor
        holder.tvDeadline.text = item.readingPeriod.toString()
        holder.tvMemberCount.text = item.memberCount
        holder.tvNickname.text = item.nickname
        holder.tvDate.text = item.date

        // 2. 장르 텍스트 처리
        val genreText = item.genre
        holder.tvGenre.text = if (genreText.isNotEmpty()) "($genreText)" else ""

        // 3. Hot 뱃지 및 하단 버튼 숨김 처리
        holder.bottomBtnLayout.visibility = View.GONE
        holder.chipHot.visibility = if (item.isHot) View.VISIBLE else View.GONE

        // 4. 칩(Chip) 스타일 및 텍스트 커스텀 로직
        val bgGrey900 = ContextCompat.getColorStateList(context, R.color.grey_900)
        val bgMain = ContextCompat.getColorStateList(context, R.color.pre_main)
        val textWhite = ContextCompat.getColor(context, R.color.white)

        if (item.groupType == "TOGETHER") {
            holder.chipStatus.apply {
                chipBackgroundColor = bgGrey900
                setTextColor(textWhite)
                text = "${item.badgeContent}(${item.maxMemberCount ?: 0})"
            }
        } else {
            holder.chipStatus.apply {
                chipBackgroundColor = bgMain
                setTextColor(textWhite)
                text = item.badgeContent
            }
        }

        // 5. 책 표지 이미지 (Cover)
        Glide.with(context)
            .load(item.coverImgUrl)
            .transform(CenterCrop(), RoundedCorners(30))
            .placeholder(R.drawable.bg_round_10dp_gray300)
            .into(holder.ivCover)

        Glide.with(context)
            .asBitmap()
            .load(item.profileImgUrl)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap?>,
                    isFirstResource: Boolean
                ): Boolean {
                    // 1. URL이 아예 없어서 실패한 경우 (정상 상황) -> 로그 찍지 않음
                    if (model == null) {
                        return false // false를 리턴하면 .error() 이미지를 보여줌
                    }

                    // 2. URL은 있는데 로드에 실패한 경우 ('테스트2' 같은 경우) -> 이때만 로그 확인
                    Log.w("GlideWarning", "이미지 로드 실패 (닉네임: ${item.nickname})\n - URL: ${item.profileImgUrl}\n - 원인: ${e?.message}")
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    // 성공하면 아무것도 안 함 (이미지 보여줌)
                    return false
                }
            })
            .circleCrop()
            .placeholder(R.drawable.bg_round_10dp_gray500)
            .error(R.drawable.bg_round_10dp_gray500)
            .fallback(R.drawable.bg_round_10dp_gray500)
            .into(holder.ivProfile)

        // 6. 태그(HashTag) 처리
        with(holder.chipGroup) {
            removeAllViews()

            val allDisplayTags = ArrayList<String>()
            if (!item.customTag.isNullOrBlank()) allDisplayTags.add("#${item.customTag}")
            item.tags.forEach { allDisplayTags.add(GroupTagMapper.toKoreanTag(it)) }

            // [태그1][태그2][태그3][+N] 로직
            if (allDisplayTags.size < 4) {
                allDisplayTags.forEach { text -> addTagChip(context, this, text) }
            } else {
                for (i in 0..2) { addTagChip(context, this, allDisplayTags[i]) }
                addTagChip(context, this, "+${allDisplayTags.size - 3}")
            }
        }

        // 7. 클릭 리스너
        holder.itemView.setOnClickListener {
            itemClick(item)
        }
    }

    override fun getItemCount(): Int = itemList.size

    fun updateList(newList: List<GroupData>) {
        this.itemList = newList
        notifyDataSetChanged()
    }

    // 칩 생성을 위한 공통 함수 (Adapter 내부 또는 Util)
    private fun addTagChip(context: android.content.Context, group: ChipGroup, text: String) {
        val chip = LayoutInflater.from(context).inflate(R.layout.item_chip_tag, group, false) as Chip
        chip.text = text
        group.addView(chip)
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.grp_item_cover_Iv)
        val tvTitle: TextView = itemView.findViewById(R.id.grp_item_book_title_Tv)
        val tvAuthor: TextView = itemView.findViewById(R.id.grp_item_book_author_Tv)
        val tvGenre: TextView = itemView.findViewById(R.id.grp_item_book_genre_Tv)
        val chipStatus: Chip = itemView.findViewById(R.id.grp_item_status_Cp) // ★ 여기가 변경됨
        val tvDeadline: TextView = itemView.findViewById(R.id.grp_item_deadlineNo_Tv)
        val tvMemberCount: TextView = itemView.findViewById(R.id.grp_item_mem_statusNo_Tv)
        val chipHot: Chip = itemView.findViewById(R.id.grp_item_hot_Cp)
        val ivProfile: ImageView = itemView.findViewById(R.id.grp_item_profile_Iv)
        val tvNickname: TextView = itemView.findViewById(R.id.grp_item_nickname_Tv)
        val tvDate: TextView = itemView.findViewById(R.id.grp_item_date_Tv)
        val chipGroup: ChipGroup = itemView.findViewById(R.id.grp_item_chip_group)
        val bottomBtnLayout: View = itemView.findViewById(R.id.grp_item_bottom_btn_layout)
    }
}