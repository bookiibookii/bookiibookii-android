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
    private val onDeleteClick: (Long) -> Unit // ★ 삭제 콜백 추가
) : RecyclerView.Adapter<GroupChatAdapter.CommentViewHolder>() {

    private val flatList = ArrayList<CommentItem>()
    private var hostNickname: String? = null

    private var selectedCommentId: Long? = null
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

            if (selectedCommentId == item.id) {
                binding.root.setBackgroundResource(R.drawable.bg_round_10dp_gray100)
            } else {
                binding.root.setBackgroundColor(Color.TRANSPARENT)
            }

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
                .transform(CenterCrop(),RoundedCorners(dpToPx(context, 6))) // 6dp 정도의 부드러운 라운드
                .placeholder(R.drawable.ic_profile) // 로딩 중 기본 이미지
                .error(R.drawable.ic_profile)       // 에러 시 기본 이미지
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

            itemView.setOnClickListener {
                if (!item.deleted) {
                    onReplyClick(item.id, item.writer.name)
                }
            }

            // 롱 클릭: 삭제 팝업
            itemView.setOnLongClickListener {
                if (!item.deleted) {
                    selectedCommentId = item.id
                    notifyDataSetChanged() // 여기서 전체를 다시 그려야 grey_100이 적용됨!
                    showCustomDeletePopup(itemView, item.id)
                }
                true
            }
        }
        private fun showCustomDeletePopup(anchorView: View, commentId: Long) {
            val context = anchorView.context
            val popupBinding = ItemGrpChatDeletePopupBinding.inflate(LayoutInflater.from(context))

            val popupWindow = PopupWindow(
                popupBinding.root,
                dpToPx(context, 160),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            // 삭제 팝업 내의 버튼 클릭 시
            popupBinding.root.setOnClickListener {
                onDeleteClick(commentId) // ★ ViewModel로 ID 전달
                popupWindow.dismiss()
            }

            popupWindow.setOnDismissListener {
                selectedCommentId = null
                notifyDataSetChanged()
            }

            popupWindow.showAsDropDown(anchorView, anchorView.width - dpToPx(context, 180), -dpToPx(context, 60))
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

    private fun showCustomDeletePopup(anchorView: View, commentId: Long) {
        val context = anchorView.context
        val inflater = LayoutInflater.from(context)
        val popupBinding = ItemGrpChatDeletePopupBinding.inflate(inflater)

        val popupWindow = PopupWindow(
            popupBinding.root,
            dpToPx(context, 160), // 너비 지정
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true // 바깥 터치 시 닫힘
        )

        // 팝업 그림자(Elevation) 설정
        popupWindow.elevation = 10f

        // 삭제 클릭 리스너
        popupBinding.root.setOnClickListener {
            // onDeleteClick(commentId) // Activity로 삭제 신호 전달
            popupWindow.dismiss()
        }

        // 팝업이 닫힐 때 배경색 원상복구
        popupWindow.setOnDismissListener {
            selectedCommentId = null
            notifyDataSetChanged()
        }

        // 이미지처럼 댓글 영역의 오른쪽 상단에 위치시키기
        // xOff: 오른쪽 끝에서 살짝 안으로, yOff: 댓글 높이의 절반 정도 위로
        popupWindow.showAsDropDown(anchorView, anchorView.width - dpToPx(context, 180),
            -dpToPx(context, 60))
    }
}