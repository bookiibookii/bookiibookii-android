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

// Activity에서 GroupMemberAdapter { } 형태로 호출할 수 있도록 생성자 파라미터 추가
class GroupMemberAdapter(
    private val onMemberClick: (Long) -> Unit
) : RecyclerView.Adapter<GroupMemberAdapter.MemberViewHolder>() {

    private var memberList = listOf<ParticipantSlot>()
    private var myNickname: String? = null

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

                // 클릭 리스너 연결 (필요 시 사용)
                root.setOnClickListener {
                    // slot에 memberId가 있다면: onMemberClick(slot.memberId)
                }

                if (slot.role == "EMPTY") {
                    itemMemberProfileIv.setImageResource(R.drawable.ic_profile)
                    itemMemberProfileIv.alpha = 0.3f
                    itemMemberNicknameTv.text = "모집중"
                    itemMemberNicknameTv.setTextColor(ContextCompat.getColor(context, R.color.grey_400))
                    itemMemberHostCp.visibility = View.GONE
                } else {
                    itemMemberProfileIv.alpha = 1.0f
                    itemMemberNicknameTv.text = slot.nickname
                    itemMemberNicknameTv.setTextColor(ContextCompat.getColor(context, R.color.grey_900))

                    // ★ 충돌 해결: profileImageUrl 사용 & CenterCrop
                    Glide.with(context)
                        .load(slot.profileImageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .centerCrop()
                        .into(itemMemberProfileIv)

                    // ★ 충돌 해결: 중복된 태그 로직 하나로 통합
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