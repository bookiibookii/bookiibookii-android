package com.bookiibookii.bookiibookii.group

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.GroupTagMapper
import com.bookiibookii.bookiibookii.databinding.ItemGroupJoinManagementBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlin.math.roundToInt

/**
 * 그룹 참여 신청자 리스트를 관리하는 어댑터
 * @param dataList 신청자 데이터 리스트
 * @param onItemClick (데이터, 수락여부)를 반환하는 콜백 함수
 */
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
            val context = binding.root.context

            with(binding) {
                // 1. 기본 텍스트 정보 설정 (닉네임, 신청일, 한마디)
                itemGrpJoinManageNicknameTv.text = item.nickname
                itemGrpJoinManageDateTv.text = item.date
                itemGrpJoinManageContentTv.text = item.intro

                // 2. 프로필 이미지 로드 (Glide)
                // CenterCrop으로 꽉 채우고 6dp 라운드 처리
                Glide.with(context)
                    .load(item.profileImgUrl)
                    .transform(CenterCrop(), RoundedCorners(dpToPx(context, 6)))
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(itemGrpJoinManageProfileIv)

                // 3. 관심사 태그(Chip) 바인딩
                val chipViews = listOf(
                    grpItemJoinMgHash1Cp,
                    grpItemJoinMgHash2Cp,
                    grpItemJoinMgHash3Cp,
                    grpItemJoinMgHash4Cp,
                    grpItemJoinMgHash5Cp
                )

                // 중복 로직을 정리하여 태그 개수만큼 노출하고 나머지는 숨김 처리
                chipViews.forEachIndexed { index, chipView ->
                    if (index < item.tags.size) {
                        chipView.text = item.tags[index]
                        chipView.visibility = View.VISIBLE
                    } else {
                        chipView.visibility = View.GONE
                    }
                }

                // 4. 수락/거절 버튼 클릭 리스너 연결
                // Boolean 값을 통해 수락(true), 거절(false) 구분
                itemGrpJoinManageYesBtn.setOnClickListener { onItemClick(item, true) }
                itemGrpJoinManageNoBtn.setOnClickListener { onItemClick(item, false) }
            }
        }
    }

    /**
     * 새로운 신청자 목록으로 리스트 갱신
     */
    fun updateData(newItemList: List<GroupJoinData>) {
        dataList.clear()
        dataList.addAll(newItemList)
        notifyDataSetChanged()
    }

    /**
     * 픽셀 변환 유틸리티 함수
     */
    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).roundToInt()
    }
}