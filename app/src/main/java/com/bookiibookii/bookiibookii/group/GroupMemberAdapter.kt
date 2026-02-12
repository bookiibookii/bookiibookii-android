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
    private var myNickname: String? = null // 액티비티에서 받아올 내 닉네임

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
                // 1. 공통 초기화
                itemMemberHostCp.visibility = View.GONE

                if (slot.role == "EMPTY") {
                    itemMemberProfileIv.setImageResource(R.drawable.ic_profile)
                    itemMemberProfileIv.alpha = 0.3f
                    itemMemberNicknameTv.text = "모집중"
                    itemMemberNicknameTv.setTextColor(ContextCompat.getColor(context, R.color.grey_400))
                } else {
                    itemMemberProfileIv.alpha = 1.0f
                    itemMemberNicknameTv.text = slot.nickname
                    itemMemberNicknameTv.setTextColor(ContextCompat.getColor(context, R.color.grey_900))

                    // 2. 프로필 이미지 채우기
                    // [수정] 배경이나 이전 이미지가 남지 않도록 Glide 설정 강화
                    Glide.with(context)
                        .load(slot.profileImage)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .centerCrop() // 사진을 카드뷰 모양에 꽉 채우기 위해 centerCrop만 사용!
                        .into(itemMemberProfileIv)
                    // 3. 태그 로직 (HOST 우선, 그다음 ME)
                    when {
                        slot.role == "HOST" -> {
                            itemMemberHostCp.visibility = View.VISIBLE
                            itemMemberHostCp.text = "HOST"
                            // 호스트는 기존처럼 메인 강조색
                            itemMemberHostCp.setChipBackgroundColorResource(R.color.pre_main)
                            itemMemberHostCp.setTextColor(Color.WHITE)
                        }
                        slot.nickname == myNickname -> {
                            itemMemberHostCp.visibility = View.VISIBLE
                            itemMemberHostCp.text = "ME"

                            // ★ [수정] 요청하신 색상 조합 적용
                            // 배경색: sub_pale (연한색)
                            itemMemberHostCp.setChipBackgroundColorResource(R.color.pre_sub_pale)
                            // 텍스트색: pre_sub (강조색)
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