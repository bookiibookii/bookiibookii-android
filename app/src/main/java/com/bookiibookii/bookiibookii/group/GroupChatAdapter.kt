package com.bookiibookii.bookiibookii.group

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.GroupItemDto.CommentItem
import com.bookiibookii.bookiibookii.databinding.ItemGrpChatCardBinding
import com.bookiibookii.bookiibookii.databinding.ItemGrpChatDeletePopupBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class GroupChatAdapter(
    private val onReplyClick: (Long, String) -> Unit,
    private val onDeleteClick: (Long) -> Unit // 삭제 콜백
) : RecyclerView.Adapter<GroupChatAdapter.CommentViewHolder>() {

    private val flatList = ArrayList<CommentItem>()
    private var hostNickname: String? = null
    private var selectedCommentId: Long? = null // 롱 클릭 시 배경 변경을 위한 ID

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
        holder.bind(flatList[position], hostNickname)
    }

    override fun getItemCount(): Int = flatList.size

    inner class CommentViewHolder(private val binding: ItemGrpChatCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CommentItem, hostNickname: String?) {
            val context = itemView.context
            val isReply = (item.parentId != null && item.parentId != 0L)

            // 롱 클릭 시 선택된 아이템 배경 강조 처리
            if (selectedCommentId == item.id) {
                binding.root.setBackgroundResource(R.drawable.bg_round_10dp_gray100)
            } else {
                binding.root.setBackgroundColor(Color.TRANSPARENT)
            }

            // 1. 비밀댓글 및 삭제 상태 UI 처리
            binding.itemGrpChatCardSecretTv.visibility = if (item.secret && !item.deleted) View.VISIBLE else View.GONE

            // 2. 닉네임 표시 및 색상 처리
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

            // 3. 메시지 본문 내용 처리
            when {
                item.deleted -> {
                    binding.itemGrpChatCardChatTv.text = "삭제된 메시지입니다."
                    binding.itemGrpChatCardChatTv.setTextColor(ContextCompat.getColor(context, R.color.grey_400))
                }
                else -> {
                    binding.itemGrpChatCardChatTv.text = item.content
                    binding.itemGrpChatCardChatTv.setTextColor(ContextCompat.getColor(context, R.color.grey_700))
                }
            }

            // 4. 프로필 이미지 로드
            Glide.with(context)
                .load(item.writer.profileImage)
                .transform(CenterCrop(), RoundedCorners(dpToPx(context, 6)))
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(binding.itemGrpChatCardProfileIv)

            // 5. 시간 정보 표시 (방금, n분 전 등)
            val (timeNum, timeUnit) = getTimeAgoParts(item.createdAt)
            binding.itemGrpChatCardTimeTv.text = timeNum
            binding.itemGrpChatCardTimeKindTv.text = timeUnit
            binding.itemGrpChatCardTimeAgoTv.text = if (timeNum.isNotEmpty()) "전" else ""

            // 6. 답글 들여쓰기 처리
            val indentSize = if (isReply) 40 else 0
            binding.itemGrpChatCardProfileIv.updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginStart = dpToPx(context, indentSize)
            }

            // 클릭 리스너: 답글 모드 진입
            itemView.setOnClickListener {
                if (!item.deleted) {
                    onReplyClick(item.id, item.writer.name)
                }
            }

            // 롱 클릭 리스너: 삭제 팝업 노출
            itemView.setOnLongClickListener {
                if (!item.deleted) {
                    selectedCommentId = item.id
                    notifyDataSetChanged() // 배경색 변경 반영
                    showCustomDeletePopup(itemView, item.id)
                }
                true
            }
        }

         // 커스텀 삭제 팝업 노출 및 처리
        private fun showCustomDeletePopup(anchorView: View, commentId: Long) {
            val context = anchorView.context
            val popupBinding = ItemGrpChatDeletePopupBinding.inflate(LayoutInflater.from(context))

            val popupWindow = PopupWindow(
                popupBinding.root,
                dpToPx(context, 160),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            ).apply {
                elevation = 10f
            }

            // 팝업 내부 '삭제' 클릭 시
            popupBinding.root.setOnClickListener {
                onDeleteClick(commentId)
                popupWindow.dismiss()
            }

            // 팝업 종료 시 선택 상태 초기화
            popupWindow.setOnDismissListener {
                selectedCommentId = null
                notifyDataSetChanged()
            }

            // 앵커 뷰 기준 위치 계산하여 표시
            popupWindow.showAsDropDown(
                anchorView,
                anchorView.width - dpToPx(context, 180),
                -dpToPx(context, 60)
            )
        }
    }


    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).roundToInt()
    }

    private fun getTimeAgoParts(createdAt: String?): Pair<String, String> {
        if (createdAt.isNullOrEmpty()) return Pair("", "")
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val time = sdf.parse(createdAt)?.time ?: return Pair("", "")
            val now = System.currentTimeMillis()
            val diff = now - time
            val minutes = diff / (1000 * 60)
            val hours = minutes / 60
            val days = hours / 24

            when {
                minutes < 1 -> Pair("방금", "")
                minutes < 60 -> Pair(minutes.toString(), "분")
                hours < 24 -> Pair(hours.toString(), "시간")
                else -> Pair(days.toString(), "일")
            }
        } catch (e: Exception) {
            Pair("", "")
        }
    }

    class VerticalSpaceItemDecoration(private val verticalSpaceDp: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.bottom = (verticalSpaceDp * view.context.resources.displayMetrics.density).toInt()
        }
    }

}