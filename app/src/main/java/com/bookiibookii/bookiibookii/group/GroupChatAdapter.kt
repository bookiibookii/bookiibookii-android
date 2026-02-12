package com.bookiibookii.bookiibookii.group

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.GroupItemDto
import com.bookiibookii.bookiibookii.data.model.GroupItemDto.CommentItem
import com.bookiibookii.bookiibookii.databinding.ItemGrpChatCardBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class GroupChatAdapter(
    private val onReplyClick: (Long, String) -> Unit
) : RecyclerView.Adapter<GroupChatAdapter.CommentViewHolder>() {

    private val flatList = ArrayList<CommentItem>()
    // ★ 추가: 호스트 닉네임을 저장할 변수
    private var hostNickname: String? = null

    // ★ 추가: 액티비티에서 호스트 닉네임을 설정해줄 함수
    fun setHostNickname(nickname: String?) {
        this.hostNickname = nickname
        notifyDataSetChanged() // 닉네임 색상을 다시 그려야 하므로 갱신
    }
    fun setComments(rawList: List<CommentItem>) {
        flatList.clear()
        rawList.forEach { parent ->
            flatList.add(parent)
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
        val item = flatList[position]
        holder.bind(item,hostNickname)
    }

    override fun getItemCount(): Int = flatList.size

    inner class CommentViewHolder(private val binding: ItemGrpChatCardBinding) : RecyclerView.ViewHolder(binding.root) {
        // ★ 수정: 파라미터에 hostNickname 추가
        fun bind(item: CommentItem, hostNickname: String?) {
            val context = itemView.context
            val isReply = (item.parentId != null && item.parentId != 0L)


            // 1. 비밀댓글 표시 (자물쇠 아이콘 등)
            binding.itemGrpChatCardSecretTv.visibility = if (item.secret) View.VISIBLE else View.GONE

            // 2. 닉네임 및 색상 처리
            if (item.deleted) {
                binding.itemGrpChatCardNicknameIv.text = "(알수없음)"
                binding.itemGrpChatCardNicknameIv.setTextColor(ContextCompat.getColor(context, R.color.grey_400))
            } else {
                binding.itemGrpChatCardNicknameIv.text = item.writer.name

                // --- 색상 우선순위 로직 ---
                val nickColor = when {
                    item.secret -> R.color.pre_sub  // 1순위: 비밀댓글이면 sub 색상
                    item.writer.name == hostNickname -> R.color.pre_main // 2순위: 호스트면 메인 색상
                    else -> R.color.grey_900 // 기본: 검정 계열
                }
                binding.itemGrpChatCardNicknameIv.setTextColor(ContextCompat.getColor(context, nickColor))
            }

            // 3. 메시지 내용 처리 (기존과 동일)
            when {
                item.deleted -> {
                    binding.itemGrpChatCardChatTv.text = "삭제된 메시지입니다."
                    binding.itemGrpChatCardChatTv.setTextColor(ContextCompat.getColor(context, R.color.grey_400))
                    binding.itemGrpChatCardSecretTv.visibility = View.GONE
                }
                // [보완] 비밀글인데 내용이 없거나 가려져야 하는 경우
                item.secret -> {
                    binding.itemGrpChatCardSecretTv.visibility = View.VISIBLE

                    if (item.content.isNullOrBlank()) {
                        // 서버에서 권한이 없어 내용을 비워 보낸 경우
                        binding.itemGrpChatCardChatTv.text = ""
                        binding.itemGrpChatCardChatTv.setTextColor(ContextCompat.getColor(context, R.color.grey_400))
                    } else {
                        // 본인이거나 호스트라서 내용이 보이는 경우
                        binding.itemGrpChatCardChatTv.text = item.content
                        binding.itemGrpChatCardChatTv.setTextColor(ContextCompat.getColor(context, R.color.grey_700))
                    }
                }
                else -> {
                    // 일반 댓글
                    binding.itemGrpChatCardSecretTv.visibility = View.GONE
                    binding.itemGrpChatCardChatTv.text = item.content
                    binding.itemGrpChatCardChatTv.setTextColor(ContextCompat.getColor(context, R.color.grey_700))
                }
            }

            // 프로필 이미지
            Glide.with(context)
                .load(item.writer.profileImage)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.itemGrpChatCardProfileIv)

            // 시간 표시 (UTC 보정 적용)
            val (timeNum, timeUnit) = getTimeAgoParts(item.createdAt)
            binding.itemGrpChatCardTimeTv.text = timeNum
            binding.itemGrpChatCardTimeKindTv.text = timeUnit
            binding.itemGrpChatCardTimeAgoTv.text = if (timeNum.isNotEmpty()) "전" else ""

            // 대댓글 들여쓰기
            val indentSize = if (isReply) 40 else 0
            binding.itemGrpChatCardProfileIv.updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginStart = dpToPx(context, indentSize)
            }
            //  클릭 시 동작
            itemView.setOnClickListener {
                if (!item.deleted) {
                    onReplyClick(item.id, item.writer.name)
                }
            }


        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).roundToInt()
    }

    // ★ [핵심] UTC 시간 보정 로직
    private fun getTimeAgoParts(createdAt: String?): Pair<String, String> {
        if (createdAt.isNullOrEmpty()) return Pair("", "")
        try {
            // 1. 포맷 설정 (서버 형식에 맞게, 예: 2024-02-12T14:00:00)
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

            // 2. ★ TimeZone을 UTC로 설정 (이 부분이 없으면 9시간 차이 발생)
            sdf.timeZone = TimeZone.getTimeZone("UTC")

            val time = sdf.parse(createdAt)?.time ?: return Pair("", "")
            val now = System.currentTimeMillis() // 현재 단말기 시간 (UTC 기준 milliseconds 반환)

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
            e.printStackTrace()
            return Pair("", "")
        }
    }

    class VerticalSpaceItemDecoration(private val verticalSpaceDp: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.bottom = (verticalSpaceDp * view.context.resources.displayMetrics.density).toInt()
        }
    }
}