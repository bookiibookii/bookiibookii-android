package com.bookiibookii.bookiibookii.group

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.GroupItemDto.ParticipantSlot
import com.bookiibookii.bookiibookii.databinding.ItemGrpMemberBinding // XML 파일명 확인 필요
import com.bumptech.glide.Glide

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
                // 클릭 리스너 (필요시)
                root.setOnClickListener {
                    // slot.memberId 등을 이용해 클릭 이벤트 처리
                }

                // 1. "모집중(EMPTY)" 상태 처리
                if (slot.role == "EMPTY") {
                    // 이미지는 기본 이미지
                    itemMemberProfileIv.setImageResource(R.drawable.ic_profile)
                    itemMemberProfileIv.alpha = 0.3f // 흐릿하게

                    // 텍스트 설정
                    itemMemberNicknameTv.text = "모집중"
                    itemMemberNicknameTv.setTextColor(ContextCompat.getColor(context, R.color.grey_400))

                    // 뱃지 숨김
                    itemMemberHostCp.visibility = View.GONE
                }
                // 2. "참여자(HOST, GUEST)" 상태 처리
                else {
                    itemMemberProfileIv.alpha = 1.0f // 투명도 복구
                    itemMemberNicknameTv.text = slot.nickname
                    itemMemberNicknameTv.setTextColor(ContextCompat.getColor(context, R.color.grey_900))

                    // ★ [이미지 로드]
                    Glide.with(context)
                        .load(slot.profileImage) // URL 확인
                        .placeholder(R.drawable.ic_profile) // 로딩 중
                        .error(R.drawable.ic_profile)       // 에러/URL null일 때
                        .fallback(R.drawable.ic_profile)    // URL이 null일 때
                        .into(itemMemberProfileIv)          // ★ ImageView에 넣기

                    // 3. 뱃지(Chip) 처리
                    when {
                        // 호스트인 경우
                        slot.role == "HOST" -> {
                            itemMemberHostCp.visibility = View.VISIBLE
                            itemMemberHostCp.text = "HOST"
                            itemMemberHostCp.setChipBackgroundColorResource(R.color.pre_main)
                            itemMemberHostCp.setTextColor(Color.WHITE)
                            // Stroke(테두리) 없애기
                            itemMemberHostCp.chipStrokeWidth = 0f
                        }
                        // 나(ME)인 경우 (호스트가 아니면서)
                        slot.nickname == myNickname -> {
                            itemMemberHostCp.visibility = View.VISIBLE
                            itemMemberHostCp.text = "ME"
                            itemMemberHostCp.setChipBackgroundColorResource(R.color.pre_sub_pale)
                            itemMemberHostCp.setChipStrokeColorResource(R.color.pre_sub_pale)
                            itemMemberHostCp.setTextColor(ContextCompat.getColor(context, R.color.pre_sub))
                        }
                        // 일반 게스트인 경우
                        else -> {
                            itemMemberHostCp.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
}