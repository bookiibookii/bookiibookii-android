package com.bookiibookii.bookiibookii.group

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.GroupItemDto.CommentItem
import com.bookiibookii.bookiibookii.databinding.ItemGrpChatCardBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// onReplyClick: 아이템 클릭 시 (댓글ID, 작성자이름) 전달 -> 답글 모드
class GroupChatAdapter(
    private val onReplyClick: (Long, String) -> Unit
) : RecyclerView.Adapter<GroupChatAdapter.CommentViewHolder>() {

    // [평탄화된 리스트] 부모-자식-자식 순서로 저장
    private val flatList = ArrayList<CommentItem>()

    // ★ 데이터를 받아서 [부모 -> 자식] 순서로 쫙 펴주는 함수
    fun setComments(rawList: List<CommentItem>) {
        flatList.clear()
        rawList.forEach { parent ->
            // 1. 부모 추가
            flatList.add(parent)
            // 2. 자식이 있다면 바로 뒤에 추가
            parent.children?.forEach { child ->
                flatList.add(child)
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemGrpChatCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(flatList[position])
    }

    override fun getItemCount(): Int = flatList.size

    inner class CommentViewHolder(private val binding: ItemGrpChatCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommentItem) {
            val context = itemView.context

            // 1. 내용 표시
            // 삭제된 댓글 처리
            if (item.deleted) {
                binding.itemGrpChatCardChatTv.text = "삭제된 메시지입니다."
                binding.itemGrpChatCardNicknameIv.text = "(알수없음)"
            } else {
                binding.itemGrpChatCardChatTv.text = item.content
                binding.itemGrpChatCardNicknameIv.text = item.writer.name
            }

            // 2. 프로필 이미지
            Glide.with(context)
                .load(item.writer.profileImage)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.itemGrpChatCardProfileIv)

            // 3. 시간 계산 및 표시 ("2", "시간", "전")
            val timeParts = getTimeAgoParts(item.createdAt)
            binding.itemGrpChatCardTimeTv.text = timeParts.first // 숫자 (ex: 2)
            binding.itemGrpChatCardTimeKindTv.text = timeParts.second // 단위 (ex: 시간)
            binding.itemGrpChatCardTimeAgoTv.text = "전" // 고정 텍스트

            // 4. ★ 대댓글 들여쓰기 처리
            val params = binding.itemGrpChatCardProfileIv.layoutParams as ConstraintLayout.LayoutParams

            if (item.parentId != null && item.parentId != 0L) {
                // 대댓글이면 왼쪽 여백 50dp (들여쓰기)
                params.marginStart = dpToPx(context, 50)
            } else {
                // 원댓글이면 기본 여백 (XML 값과 동일하게, 보통 0dp or marginStart값)
                params.marginStart = dpToPx(context, 0)
            }
            binding.itemGrpChatCardProfileIv.layoutParams = params

            // 5. 클릭 시 답글 달기 (삭제된 댓글은 클릭 안 되게)
            itemView.setOnClickListener {
                if (!item.deleted) {
                    onReplyClick(item.id, item.writer.name)
                }
            }
        }
    }

    // dp -> px 변환 함수
    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    // 시간 파싱 헬퍼 함수 (서버 시간 -> "2", "시간" 반환)
    private fun getTimeAgoParts(createdAt: String): Pair<String, String> {
        try {
            // 서버 시간 포맷 (ISO 8601)
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC") // 서버가 UTC라면

            val time = sdf.parse(createdAt)?.time ?: return Pair("", "")
            val now = System.currentTimeMillis()
            val diff = now - time

            val minutes = diff / (1000 * 60)
            val hours = minutes / 60
            val days = hours / 24

            return when {
                minutes < 1 -> Pair("방금", "")
                minutes < 60 -> Pair(minutes.toString(), "분")
                hours < 24 -> Pair(hours.toString(), "시간")
                else -> Pair(days.toString(), "일")
            }
        } catch (e: Exception) {
            return Pair("", "")
        }
    }
}