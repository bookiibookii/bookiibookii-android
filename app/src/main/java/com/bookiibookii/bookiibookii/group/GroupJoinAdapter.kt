package com.bookiibookii.bookiibookii.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.GroupTagMapper
import com.bookiibookii.bookiibookii.databinding.ItemGroupJoinManagementBinding
import com.bumptech.glide.Glide // Glide 임포트 필수

class GroupJoinAdapter(
    private var dataList: MutableList<GroupJoinData>,
    private val onItemClick: (GroupJoinData, Boolean) -> Unit
) : RecyclerView.Adapter<GroupJoinAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemGroupJoinManagementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

    inner class Holder(private val binding: ItemGroupJoinManagementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GroupJoinData) {
            with(binding) {
                // 1. 텍스트 설정
                itemGrpJoinManageNicknameTv.text = item.nickname
                itemGrpJoinManageDateTv.text = item.date
                itemGrpJoinManageContentTv.text = item.intro

                // 2. [변경] 프로필 이미지 (Glide 사용)
                Glide.with(root.context)
                    .load(item.profileImgUrl)          // URL 로드
                    .placeholder(R.drawable.ic_profile) // 로딩/실패/null 시 기본 이미지
                    .error(R.drawable.ic_profile)
                    .circleCrop()                       // 원형으로 자르기
                    .into(itemGrpJoinManageProfileIv)

                // 3. 태그 칩 설정 (기존 로직 유지)
                val chipViews = listOf(
                    grpItemJoinMgHash1Cp,
                    grpItemJoinMgHash2Cp,
                    grpItemJoinMgHash3Cp,
                    grpItemJoinMgHash4Cp,
                    grpItemJoinMgHash5Cp
                )
                GroupTagMapper.bindTags(chipViews, item.tags)


                chipViews.forEachIndexed { index, chipView ->
                    if (index < item.tags.size) {
                        chipView.text = item.tags[index]
                        chipView.visibility = View.VISIBLE
                    } else {
                        chipView.visibility = View.GONE
                    }
                }

                // 4. 버튼 리스너
                itemGrpJoinManageYesBtn.setOnClickListener { onItemClick(item, true) }
                itemGrpJoinManageNoBtn.setOnClickListener { onItemClick(item, false) }
            }
        }
    }

    fun updateData(newItemList: List<GroupJoinData>) {
        dataList.clear()
        dataList.addAll(newItemList)
        notifyDataSetChanged()
    }
}