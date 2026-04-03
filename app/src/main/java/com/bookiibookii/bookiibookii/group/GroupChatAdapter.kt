package com.bookiibookii.bookiibookii.group

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class GroupChatAdapter(
    private val onReplyClick: (Long, String) -> Unit,
    private val onDeleteClick: (Long) -> Unit
) : RecyclerView.Adapter<GroupChatAdapter.CommentViewHolder>() {

    private val flatList = ArrayList<CommentItem>()
    private var hostNickname: String? = null

    // ★ [추가] 내 아이디 및 호스트 여부 저장 변수
    private var currentUserId: Long = -1L
    private var selectedCommentId: Long? = null
    private var isCurrentUserHost: Boolean = false

    // ★ [추가] 액티비티에서 내 ID를 넣어주는 함수
    fun setCurrentUserId(id: Long) {
        this.currentUserId = id
    }

    // ★ [추가] 액티비티에서 내가 호스트인지 여부를 전달받는 함수
    fun setIsCurrentUserHost(isHost: Boolean) {
        this.isCurrentUserHost = isHost
        notifyDataSetChanged()
    }

    fun setHostNickname(nickname: String?) {
        this.hostNickname = nickname
        notifyDataSetChanged()
    }

    fun setComments(rawList: List<CommentItem>) {
        flatList.clear()
        rawList.forEach { parent ->
            if (!parent.deleted) {
                flatList.add(parent)
                parent.children?.forEach { child ->
                    if (!child.deleted) {
                        if (child.parentId == null || child.parentId == 0L) {
                            child.parentId = parent.id
                        }
                        flatList.add(child)
                    }
                }
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

            // 들여쓰기 값 설정 (대댓글이면 40dp)
            val indentDp = if (isReply) 40 else 0
            val indentPx = dpToPx(context, indentDp)

            // 0. 배경 선택 효과 (InsetDrawable 활용)
            if (selectedCommentId == item.id) {
                val originalBg = ContextCompat.getDrawable(context, R.drawable.bg_round_10dp_gray100)
                val insetBg = InsetDrawable(originalBg, indentPx, 0, 0, 0)

                binding.root.background = insetBg
                binding.root.setPadding(0, 0, 0, 0)
            } else {
                binding.root.setBackgroundColor(Color.TRANSPARENT)
                // 투명 배경일 때도 혹시 모르니 패딩 초기화 (안전장치)
                binding.root.setPadding(0, 0, 0, 0)
            }

            // 1. 닉네임 설정
            binding.itemGrpChatCardNicknameIv.text = item.writer.name
            val nickColor = when {
                item.secret -> R.color.pre_sub
                item.writer.name == hostNickname -> R.color.pre_main
                else -> R.color.grey_900
            }
            binding.itemGrpChatCardNicknameIv.setTextColor(ContextCompat.getColor(context, nickColor))

            // 2. ★ [핵심 수정] 메시지 내용 & 비밀댓글 권한 체크
            var hasPermissionToRead = false

            if (item.secret) {
                binding.itemGrpChatCardSecretTv.visibility = View.VISIBLE

                // 1) 내가 쓴 댓글인가?
                val isMyComment = (currentUserId != -1L && item.writer.userId == currentUserId)
                // 2) 내가 호스트인가?
                val amIHost = isCurrentUserHost

                // 3) 이 댓글이 대댓글일 경우, 내가 '부모댓글'의 작성자인가?
                var isParentWriter = false
                if (isReply) {
                    val parentComment = flatList.find { it.id == item.parentId }
                    if (parentComment != null && parentComment.writer.userId == currentUserId) {
                        isParentWriter = true
                    }
                }

                // 셋 중 하나라도 해당하면 읽기 권한 부여
                hasPermissionToRead = (isMyComment || amIHost || isParentWriter)

                if (hasPermissionToRead) {
                    // 볼 권한이 있는 사람: 원래 내용 표시
                    val msg = if (item.content.isNullOrBlank()) "" else item.content
                    binding.itemGrpChatCardChatTv.text = msg
                    binding.itemGrpChatCardChatTv.setTextColor(ContextCompat.getColor(context, R.color.grey_700))
                } else {
                    // 볼 권한이 없는 사람: 내용 숨김
                    binding.itemGrpChatCardChatTv.text = "비밀댓글입니다."
                    binding.itemGrpChatCardChatTv.setTextColor(ContextCompat.getColor(context, R.color.grey_400))
                }
            } else {
                // 일반 댓글
                hasPermissionToRead = true
                binding.itemGrpChatCardSecretTv.visibility = View.GONE
                binding.itemGrpChatCardChatTv.text = item.content
                binding.itemGrpChatCardChatTv.setTextColor(ContextCompat.getColor(context, R.color.grey_700))
            }

            // 3. 프로필 이미지
            Glide.with(context)
                .load(item.writer.profileImage)
                .transform(CenterCrop())
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .fallback(R.drawable.ic_profile)
                .into(binding.itemGrpChatCardProfileIv)

            // 4. 시간 표시
            val (timeNum, timeUnit) = getTimeAgoParts(item.createdAt)
            binding.itemGrpChatCardTimeTv.text = timeNum
            binding.itemGrpChatCardTimeKindTv.text = timeUnit
            binding.itemGrpChatCardTimeAgoTv.text = if (timeNum.isNotEmpty()) "전" else ""

            // 5. 프로필 위치 들여쓰기 (배경 들여쓰기와 싱크를 맞춤)
            binding.itemGrpChatCardProfileCard.updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginStart = indentPx
            }

            // 6. ★ [핵심 수정] 클릭 리스너 (답글 작성 모드 진입 제한)
            itemView.setOnClickListener {
                // 비밀댓글이면서 읽을 권한이 없는 경우, 답글 달기 이벤트를 차단함
                if (item.secret && !hasPermissionToRead) {
                    return@setOnClickListener
                }
                onReplyClick(item.id, item.writer.name)
            }

            // 7. 롱 클릭 (삭제 팝업) - 내 댓글일 때만 실행
            itemView.setOnLongClickListener {
                // 내 ID가 설정되어 있고, 작성자 ID와 같을 때만
                if (currentUserId != -1L && item.writer.userId == currentUserId) {
                    selectedCommentId = item.id
                    notifyDataSetChanged() // 배경색 변경 반영
                    showCustomDeletePopup(itemView, item.id)
                    true // 이벤트 소비 (실행됨)
                } else {
                    false // 이벤트 소비 안함 (아무 일도 안 일어남)
                }
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
            popupWindow.elevation = 10f
            popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 투명 배경 필수

            popupBinding.root.setOnClickListener {
                onDeleteClick(commentId)
                popupWindow.dismiss()
            }

            // 팝업이 닫히면 선택 상태 해제
            popupWindow.setOnDismissListener {
                selectedCommentId = null
                notifyDataSetChanged()
            }

            // 팝업 위치 조정
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

    // ★ [핵심 수정] 마지막 아이템 짤림 방지를 위한 하단 여백 추가
    class VerticalSpaceItemDecoration(private val verticalSpaceHeightDp: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val density = parent.context.resources.displayMetrics.density
            val px = (verticalSpaceHeightDp * density).toInt()

            val position = parent.getChildAdapterPosition(view)
            val itemCount = parent.adapter?.itemCount ?: 0

            if (position == itemCount - 1) {
                // 마지막 아이템일 경우: 기본 간격 + 하단 입력창 높이만큼의 여유 공간(예: 80dp)을 추가로 줌
                val extraBottomSpacePx = (80 * density).toInt()
                outRect.bottom = px + extraBottomSpacePx
            } else {
                // 마지막 아이템이 아닐 경우: 기본 간격만 적용
                outRect.bottom = px
            }
        }
    }
}