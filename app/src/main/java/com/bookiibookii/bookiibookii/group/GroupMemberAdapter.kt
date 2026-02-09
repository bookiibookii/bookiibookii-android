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

class GroupMemberAdapter : RecyclerView.Adapter<GroupMemberAdapter.MemberViewHolder>() {

    private var memberList = listOf<ParticipantSlot>()

    // 데이터 갱신용 함수
    fun submitList(list: List<ParticipantSlot>?) {
        memberList = list ?: emptyList()
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
            with(binding) {

                val context = itemView.context

                // 1. 상태별 분기 처리 (EMPTY / MEMBER / HOST)
                if (slot.role == "EMPTY") {
                    // 대기중 상태
                    itemMemberProfileIv.setImageResource(R.drawable.ic_profile)
                    itemMemberProfileIv.alpha = 0.3f
                    itemMemberNicknameTv.text = "모집중"
                    itemMemberNicknameTv.setTextColor(ContextCompat.getColor(context, R.color.grey_700))
                    itemMemberHostCp.visibility = View.GONE
                } else {
                    // 참여중 상태
                    itemMemberProfileIv.alpha = 1.0f
                    itemMemberNicknameTv.text = slot.nickname
                    itemMemberNicknameTv.setTextColor(ContextCompat.getColor(context, R.color.grey_900))// 검정

                    Glide.with(itemView.context)
                        .load(slot.profileImage)
                        .placeholder(R.drawable.ic_profile)
                        .circleCrop()
                        .into(itemMemberProfileIv)

                    // 호스트 뱃지
                    if (slot.role == "HOST") {
                        itemMemberHostCp.visibility = View.VISIBLE
                    } else {
                        itemMemberHostCp.visibility = View.GONE
                    }
                }
            }
        }
    }
}