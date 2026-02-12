package com.bookiibookii.bookiibookii.group

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.GroupItemDto.ParticipantSlot
import com.bookiibookii.bookiibookii.databinding.ItemGrpMemberBinding
import com.bumptech.glide.Glide

class GroupMemberAdapter(
    private val onMemberClick: (Long) -> Unit
) : RecyclerView.Adapter<GroupMemberAdapter.MemberViewHolder>() {

    private var memberList = listOf<ParticipantSlot>()
    private var myNickname: String? = null

    /**
     * 멤버 리스트 및 내 닉네임 정보를 업데이트
     */
    fun submitList(list: List<ParticipantSlot>?, myNick: String? = null) {
        memberList = list ?: emptyList()
        myNickname = myNick
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemGrpMemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(memberList[position])
    }

    override fun getItemCount(): Int = memberList.size

    inner class MemberViewHolder(private val binding: ItemGrpMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(slot: ParticipantSlot) {
            val context = itemView.context
            with(binding) {

                // 1. 빈 슬롯 (모집 중) 상태 UI 처리
                if (slot.role == "EMPTY") {
                    itemMemberProfileIv.setImageResource(R.drawable.ic_profile)
                    itemMemberProfileIv.alpha = 0.3f
                    itemMemberNicknameTv.text = "모집중"
                    itemMemberNicknameTv.setTextColor(ContextCompat.getColor(context, R.color.grey_400))
                    itemMemberHostCp.visibility = View.GONE
                }
                // 2. 실제 참여 멤버 UI 처리
                else {
                    itemMemberProfileIv.alpha = 1.0f
                    itemMemberNicknameTv.text = slot.nickname
                    itemMemberNicknameTv.setTextColor(ContextCompat.getColor(context, R.color.grey_900))

                    // 프로필 이미지 로드 (CenterCrop 적용)
                    Glide.with(context)
                        .load(slot.profileImage)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .centerCrop()
                        .into(itemMemberProfileIv)

                    // 역할별 칩(HOST / ME) 표시 로직
                    when {
                        slot.role == "HOST" -> {
                            itemMemberHostCp.visibility = View.VISIBLE
                            itemMemberHostCp.text = "HOST"
                            itemMemberHostCp.setChipBackgroundColorResource(R.color.pre_main)
                            itemMemberHostCp.setTextColor(Color.WHITE)
                        }
                        slot.nickname == myNickname -> {
                            itemMemberHostCp.visibility = View.VISIBLE
                            itemMemberHostCp.text = "ME"
                            itemMemberHostCp.setChipBackgroundColorResource(R.color.pre_sub_pale)
                            itemMemberHostCp.setTextColor(ContextCompat.getColor(context, R.color.pre_sub))
                        }
                        else -> {
                            itemMemberHostCp.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
}