package com.bookiibookii.bookiibookii.group.main

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.GroupTagMapper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
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

        // 1. 기본 텍스트 정보 바인딩
        holder.apply {
            tvTitle.text = item.bookTitle
            tvAuthor.text = item.bookAuthor
            tvDeadline.text = item.readingPeriod.toString()
            tvMemberCount.text = item.memberCount
            tvNickname.text = item.nickname
            tvDate.text = item.date

            // 장르가 있을 경우에만 괄호와 함께 표시
            tvGenre.text = if (item.genre.isNotEmpty()) "(${item.genre})" else ""

            // 카드 하단 공통 레이아웃 숨김 (필요 시 보이도록 설정 가능)
            bottomBtnLayout.visibility = View.GONE
            // 인기 그룹일 경우에만 HOT 뱃지 노출
            chipHot.visibility = if (item.isHot) View.VISIBLE else View.GONE
        }

        //  2. 그룹 상태 칩 스타일링
        // 함께 읽기(TOGETHER)와 교환(RELAY) 그룹에 따라 배경색과 텍스트 구성이 다름
        val bgGrey900 = ContextCompat.getColorStateList(context, R.color.grey_900)
        val bgMain = ContextCompat.getColorStateList(context, R.color.pre_main)
        val textWhite = ContextCompat.getColor(context, R.color.white)

        holder.chipStatus.apply {
            setTextColor(textWhite)
            if (item.groupType == "TOGETHER") {
                chipBackgroundColor = bgGrey900
                text = "${item.badgeContent}(${item.maxMemberCount ?: 0})"
            } else {
                chipBackgroundColor = bgMain
                text = item.badgeContent
            }
        }

        // 3. 이미지 로딩 - Glide

        // 책 표지: 중앙 크롭 및 라운드 처리 적용
        Glide.with(context)
            .load(item.coverImgUrl)
            .transform(CenterCrop(), RoundedCorners(30))
            .placeholder(R.drawable.bg_round_10dp_gray300)
            .into(holder.ivCover)

        // 사용자 프로필: 원형 크롭 및 로드 실패 대응
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
                    // URL은 전달되었으나 실제 로드에 실패한 경우만 로그 기록
                    if (model != null) {
                        Log.w("GlideWarning", "프로필 로드 실패: ${item.nickname} | URL: ${item.profileImgUrl}")
                    }
                    return false
                }
                override fun onResourceReady(r: Bitmap, m: Any, t: Target<Bitmap?>?, d: DataSource, i: Boolean) = false
            })
            .circleCrop()
            .placeholder(R.drawable.bg_round_10dp_gray500)
            .error(R.drawable.bg_round_10dp_gray500)
            .into(holder.ivProfile)


        // 4. 태그 리스트 처리

        // 커스텀 태그(#)와 서버 태그를 합쳐서 최대 3개까지만 노출, 초과 시 +N 표시
        with(holder.chipGroup) {
            removeAllViews()

            val allDisplayTags = ArrayList<String>()
            if (!item.customTag.isNullOrBlank()) allDisplayTags.add("#${item.customTag}")
            item.tags.forEach { allDisplayTags.add(GroupTagMapper.toKoreanTag(it)) }

            if (allDisplayTags.size < 4) {
                allDisplayTags.forEach { text -> addTagChip(context, this, text) }
            } else {
                for (i in 0..2) { addTagChip(context, this, allDisplayTags[i]) }
                addTagChip(context, this, "+${allDisplayTags.size - 3}")
            }
        }

        holder.itemView.setOnClickListener { itemClick(item) }
    }

    override fun getItemCount(): Int = itemList.size
    fun updateList(newList: List<GroupData>) {
        this.itemList = newList
        notifyDataSetChanged()
    }

    private fun addTagChip(context: Context, group: ChipGroup, text: String) {
        val chip = LayoutInflater.from(context).inflate(R.layout.item_chip_tag, group, false) as Chip
        chip.text = text
        group.addView(chip)
    }

    // ViewHolder 뷰 홀더 클래스
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
        val chipGroup: ChipGroup = itemView.findViewById(R.id.grp_item_chip_group)
        val bottomBtnLayout: View = itemView.findViewById(R.id.grp_item_bottom_btn_layout)
    }
}