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
    private var hostNickname: String? = null

    fun setHostNickname(nickname: String?) {
        this.hostNickname = nickname
        notifyDataSetChanged()
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
        holder.bind(item, hostNickname)
    }

    override fun getItemCount(): Int = flatList.size

    inner class CommentViewHolder(private val binding: ItemGrpChatCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommentItem, hostNickname: String?) {
            val context = itemView.context
            val isReply = (item.parentId != null && item.parentId != 0L)

            // 1. 비밀댓글 표시
            binding.itemGrpChatCardSecretTv.visibility = if (item.secret) View.VISIBLE else View.GONE

            // 2. 닉네임 및 색상 처리
            if (item.deleted) {
                binding.itemGrpChatCardNicknameIv.text = "(알수없음)"
                binding.itemGrpChatCardNicknameIv.setTextColor(ContextCompat.getColor(context, R.color.grey_400))
            } else {
                binding.itemGrpChatCardNicknameIv.text = item.writer.name

                val nickColor = when {
                    item.secret -> R.color.pre_sub
                    item.writer.name == hostNickname -> R.color.pre_main
                    else -> R.color.grey_900
                }
                binding.itemGrpChatCardNicknameIv.setTextColor(ContextCompat.getColor(context, nickColor))
            }

            // 3. 메시지 내용 처리
            when {
                item.deleted -> {
                    binding.itemGrpChatCardChatTv.text = "삭제된 메시지입니다."
                    binding.itemGrpChatCardChatTv.setTextColor(ContextCompat.getColor(context, R.color.grey_400))
                    binding.itemGrpChatCardSecretTv.visibility = View.GONE
                }
                item.secret -> {
                    binding.itemGrpChatCardSecretTv.visibility = View.VISIBLE
                    if (item.content.isNullOrBlank()) {
                        binding.itemGrpChatCardChatTv.text = ""
                    } else {
                        binding.itemGrpChatCardChatTv.text = item.content
                        binding.itemGrpChatCardChatTv.setTextColor(ContextCompat.getColor(context, R.color.grey_700))
                    }
                }
                else -> {
                    binding.itemGrpChatCardSecretTv.visibility = View.GONE
                    binding.itemGrpChatCardChatTv.text = item.content
                    binding.itemGrpChatCardChatTv.setTextColor(ContextCompat.getColor(context, R.color.grey_700))
                }
            }

            // 프로필 이미지
            Glide.with(context)
                .load(item.writer.profileImage)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.itemGrpChatCardProfileIv)

            // 5. 시간 표시
            val (timeNum, timeUnit) = getTimeAgoParts(item.createdAt)
            binding.itemGrpChatCardTimeTv.text = timeNum
            binding.itemGrpChatCardTimeKindTv.text = timeUnit
            binding.itemGrpChatCardTimeAgoTv.text = if (timeNum.isNotEmpty()) "전" else ""

            // 6. 대댓글 들여쓰기
            val indentSize = if (isReply) 40 else 0
            binding.itemGrpChatCardProfileIv.updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginStart = dpToPx(context, indentSize)
            }

            // 7. 클릭 시 답글 작성
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

    private fun getTimeAgoParts(createdAt: String?): Pair<String, String> {
        if (createdAt.isNullOrEmpty()) return Pair("", "")
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
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

    class VerticalSpaceItemDecoration(private val verticalSpaceDp: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.bottom = (verticalSpaceDp * view.context.resources.displayMetrics.density).toInt()
        }
    }
}