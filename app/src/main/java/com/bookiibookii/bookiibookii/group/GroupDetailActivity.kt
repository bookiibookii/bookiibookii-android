package com.bookiibookii.bookiibookii.group

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.common.GroupTagMapper
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.GroupItemDto
import com.bookiibookii.bookiibookii.databinding.ActivityGrpHostBinding
import com.bookiibookii.bookiibookii.databinding.DialogGroupJoinBinding
import com.bookiibookii.bookiibookii.group.generation.GroupGenerationActivity
import com.bookiibookii.bookiibookii.group.viewmodel.GroupDetailViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import org.json.JSONObject

class GroupDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpHostBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private val memberAdapter = GroupMemberAdapter {}

    private var allCommentsList: List<GroupItemDto.CommentItem> = emptyList()

    private val commentAdapter = GroupChatAdapter(
        onReplyClick = { commentId, writerName ->
            val isParentItem = allCommentsList.any { it.id == commentId }
            if (isParentItem) {
                enterReplyMode(commentId, writerName)
            }
        },
        onDeleteClick = { commentId ->
            viewModel.deleteComment(currentGroupId.toInt(), commentId.toInt())
        }
    )

    private val viewModel: GroupDetailViewModel by viewModels()
    private val userViewModel: MyPageViewModel by viewModels()
    private var currentGroupId: Long = -1L
    private var targetParentId: Long? = null
    private var isReplyMode = false
    private var isSecretMode = false
    private var currentGroupType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrpHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentGroupType = intent.getStringExtra("GROUP_TYPE")
        currentGroupId = intent.getLongExtra("GROUP_ID", -1L)

        if (currentGroupId <= 0L) {
            showCustomToast("잘못된 접근입니다.", false)
            finish()
            return
        }

        initView()
        initBottomSheet()
        setupObserver()
        initSecretLock()
        initInputListener()

        viewModel.groupDetail.observe(this) { data ->
            if (data != null) {
                setupMoreMenu(data.isHost)
            }
        }

        userViewModel.fetchMypageData()
        viewModel.fetchGroupDetail(currentGroupId.toInt())
        viewModel.fetchComments(currentGroupId)

        binding.grpMgBottomSheetBackIv.setOnClickListener {
            exitReplyMode()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isReplyMode) {
                    exitReplyMode()
                    return
                }
                if (::bottomSheetBehavior.isInitialized && bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    return
                }
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        })
    }

    private fun enterReplyMode(parentId: Long, writerName: String) {
        isReplyMode = true
        targetParentId = parentId
        binding.grpMgBottomSheetBackIv.visibility = View.VISIBLE
        binding.grpMgBottomSheetTitleTv.text = "답글"
        binding.grpMgBottomSheetNumTitleTv.visibility = View.GONE

        val targetThread = allCommentsList.filter {
            it.id == parentId || (it.parentId == parentId && it.parentId != 0L)
        }
        commentAdapter.setComments(targetThread)
        binding.grpMgBottomSheetInputEt.hint = "$writerName 님에게 답글 작성..."
        showKeyboard()

        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun showKeyboard() {
        binding.grpMgBottomSheetInputEt.requestFocus()
        binding.grpMgBottomSheetInputEt.postDelayed({
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.grpMgBottomSheetInputEt, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    private fun exitReplyMode() {
        isReplyMode = false
        targetParentId = null
        binding.grpMgBottomSheetBackIv.visibility = View.GONE
        binding.grpMgBottomSheetTitleTv.text = "댓글"
        binding.grpMgBottomSheetNumTitleTv.visibility = View.VISIBLE
        binding.grpMgBottomSheetNumTitleTv.text = "${allCommentsList.size}"
        commentAdapter.setComments(allCommentsList)
        binding.grpMgBottomSheetInputEt.hint = "텍스트 입력 전"
        binding.grpMgBottomSheetInputEt.clearFocus()
        hideKeyboard()
    }

    private fun initView() {
        binding.actGrpHoBackIv.setOnClickListener { handleBackPress() }
        binding.actGrpHoMemberRv.adapter = memberAdapter

        binding.grpMgBottomSheetInfoRv.apply {
            layoutManager = LinearLayoutManager(this@GroupDetailActivity)
            adapter = commentAdapter
            itemAnimator = null
            addItemDecoration(GroupChatAdapter.VerticalSpaceItemDecoration(12))
            setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                    hideKeyboard()
                }
                false
            }
        }

        val inputEt = binding.grpMgBottomSheetInputEt
        val sendBtn = binding.grpMgBottomSheetSendIv
        sendBtn.isEnabled = false
        sendBtn.alpha = 0.3f

        inputEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val hasText = !s.isNullOrBlank()
                sendBtn.isEnabled = hasText
                sendBtn.alpha = if (hasText) 1.0f else 0.3f
                if (hasText) sendBtn.setImageResource(R.drawable.ic_send_black)
            }
        })

        sendBtn.setOnClickListener {
            val content = inputEt.text.toString().trim()
            if (content.isNotEmpty()) {
                viewModel.postComment(currentGroupId.toLong(), content, targetParentId, isSecretMode)
            }
        }

        binding.grpMgBottomSheetReloadIv.setOnClickListener {
            viewModel.fetchComments(currentGroupId.toLong())
        }
    }

    private fun initBottomSheet() {
        val bottomSheetLayout = binding.persistentBottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.grpMgBottomSheetReloadIv.alpha = slideOffset
            }
        })
    }

    private fun deactivateReplyMode() {
        targetParentId = null
        binding.grpMgBottomSheetInputEt.hint = "텍스트 입력 전"
        binding.grpMgBottomSheetInputEt.clearFocus()
    }

    private fun initInputListener() {
        binding.grpMgBottomSheetSendIv.setOnClickListener {
            val content = binding.grpMgBottomSheetInputEt.text.toString().trim()
            if (content.isNotEmpty()) {
                viewModel.postComment(currentGroupId.toLong(), content, targetParentId, isSecretMode)
            }
        }

        binding.grpMgBottomSheetInfoRv.setOnTouchListener { _, _ ->
            if (targetParentId != null) {
                deactivateReplyMode()
                hideKeyboard()
            }
            false
        }
    }

    private fun initSecretLock() {
        binding.ivLock.setOnClickListener {
            isSecretMode = !isSecretMode
            if (isSecretMode) {
                binding.ivLock.setImageResource(R.drawable.ic_lock_blue)
            } else {
                binding.ivLock.setImageResource(R.drawable.ic_lock)
            }
        }
    }

    private fun setupObserver() {
        userViewModel.profileData.observe(this) { profile ->
            if (profile != null) {
                val detail = viewModel.groupDetail.value
                updateMemberAdapter(detail?.participantSlots, detail?.hostProfileImageUrl)
            }
        }

        viewModel.groupDetail.observe(this) { data ->
            if (data != null) {
                bindUi(data)
                handleButtonStatus(data)
                updateMemberAdapter(data.participantSlots, data.hostProfileImageUrl)
            }
        }

        viewModel.commentList.observe(this) { list ->
            if (list != null) {
                allCommentsList = list
                val hostNickname = viewModel.groupDetail.value?.hostNickname
                commentAdapter.setHostNickname(hostNickname)

                if (isReplyMode && targetParentId != null) {
                    val targetThread = list.filter {
                        it.id == targetParentId || it.parentId == targetParentId
                    }
                    commentAdapter.setComments(targetThread)
                } else {
                    commentAdapter.setComments(list)
                    binding.grpMgBottomSheetNumTitleTv.text = "${list.size}"
                }
            }
        }

        viewModel.commentWriteSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                binding.grpMgBottomSheetInputEt.setText("")
                isSecretMode = false
                binding.ivLock.setImageResource(R.drawable.ic_lock)
                if (isReplyMode) exitReplyMode()
                viewModel.fetchComments(currentGroupId.toLong())
                hideKeyboard()
            }
        }

        viewModel.commentDeleteEvent.observe(this) { isSuccess ->
            if (isSuccess) {
                showCustomToast("댓글이 정상적으로 삭제되었습니다.", true)
            } else {
                showCustomToast("삭제 권한이 없거나 오류가 발생했습니다.", false)
            }
        }

        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrBlank()) showCustomToast(msg, false)
        }
    }

    private fun handleBackPress() {
        if (::bottomSheetBehavior.isInitialized && bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            finish()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.grpMgBottomSheetInputEt.windowToken, 0)
    }

    private fun bindUi(data: GroupItemDto.GroupDetailResult) {
        val isDirectExchange = !data.meetPlace.isNullOrBlank()
        binding.actGrpHoRegionLayout.visibility = if (isDirectExchange) View.VISIBLE else View.GONE
        binding.actGrpHoIntroRealRegionTv.text = if (isDirectExchange) data.meetPlace else data.preferRegion

        with(binding.actGrpHoIncludedItem) {
            when (data.groupStatus) {
                "RECRUITING" -> {
                    grpItemStatusCp.text = "모집 중"
                    grpItemStatusCp.chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.pre_main))
                    grpItemStatusCp.chipStrokeColor = ColorStateList.valueOf(getColor(R.color.pre_main))
                    grpItemStatusCp.setTextColor(getColor(R.color.white))
                }
                "MATCHED" -> {
                    grpItemStatusCp.text = "진행 중"
                    grpItemStatusCp.chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
                    grpItemStatusCp.chipStrokeColor = ColorStateList.valueOf(getColor(R.color.ui_main_105))
                    grpItemStatusCp.setTextColor(getColor(R.color.pre_main))
                }
                "COMPLETED" -> {
                    grpItemStatusCp.text = "종료"
                    grpItemStatusCp.chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.grey_200))
                    grpItemStatusCp.chipStrokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
                    grpItemStatusCp.setTextColor(getColor(R.color.grey_500))
                }
                else -> {
                    grpItemStatusCp.text = "마감"
                }
            }

            grpItemBookTitleTv.text = data.bookTitle
            grpItemBookAuthorTv.text = data.author
            grpItemBookGenreTv.text = if (!data.category.isNullOrEmpty()) "(${data.category})" else ""
            grpItemDateTv.text = data.startDate.replace("-", ".")
            grpItemNicknameTv.text = data.hostNickname
            grpItemDeadlineNoTv.text = data.readingPeriod.toString()
            grpItemMemStatusNoTv.text = "${data.matchedCount}"

            Glide.with(this@GroupDetailActivity).load(data.bookImage).centerCrop().into(grpItemCoverIv)
            Glide.with(this@GroupDetailActivity)
                .load(data.hostProfileImageUrl)
                .transform(CenterCrop(), RoundedCorners(dpToPx(6)))
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(grpItemProfileIv)
            grpItemHotCp.visibility = if (data.isHot) View.VISIBLE else View.GONE

            with(grpItemChipGroup) {
                removeAllViews()
                val displayTags = ArrayList<String>()
                if (!data.customTag.isNullOrBlank()) displayTags.add("#${data.customTag}")
                data.groupTags?.forEach { displayTags.add(GroupTagMapper.toKoreanTag(it)) }

                displayTags.forEach { tagText ->
                    val chip = layoutInflater.inflate(R.layout.item_chip_tag, this, false) as Chip
                    chip.text = tagText
                    this.addView(chip)
                }
            }
            grpItemBottomBtnLayout.visibility = View.VISIBLE
        }

        binding.actGrpHoIntroContTv.text = data.groupComment
        binding.actGrpHoMainTitleTv.text = data.title
        binding.actGrpHoMemberStatus1Tv.text = "${data.matchedCount}"
        binding.actGrpHoMemberStatus3Tv.text = "${data.maxCapacity}"

        val processedSlots = data.participantSlots?.map { slot ->
            if (slot.role == "HOST" && slot.profileImage.isNullOrBlank()) {
                slot.copy(profileImage = data.hostProfileImageUrl)
            } else slot
        }
        updateMemberAdapter(processedSlots, data.hostProfileImageUrl)
    }

    private fun updateMemberAdapter(slots: List<GroupItemDto.ParticipantSlot>?, hostProfile: String?) {
        val myNickname = userViewModel.profileData.value?.nickname
        val processed = slots?.map { slot ->
            if (slot.role == "HOST" && slot.profileImage.isNullOrBlank()) {
                slot.copy(profileImage = hostProfile)
            } else {
                slot
            }
        }
        memberAdapter.submitList(processed, myNickname)
    }

    private fun handleButtonStatus(data: GroupItemDto.GroupDetailResult) {
        val itemBinding = binding.actGrpHoIncludedItem
        val btnLayout = itemBinding.grpItemManageBtn
        val btnTitle = itemBinding.grpItemBtnTitleTv
        val btnCount = itemBinding.grpItemBtnNumTv

        btnLayout.setOnClickListener(null)
        btnCount.visibility = View.GONE
        btnLayout.isEnabled = true

        when (data.buttonStatus) {
            "MANAGE" -> {
                btnTitle.text = "참여 요청 관리"
                btnCount.visibility = View.VISIBLE
                btnCount.text = "(${data.waitingCount})"
                btnLayout.setOnClickListener {
                    val intent = Intent(this, com.bookiibookii.bookiibookii.group.GroupJoinManagementActivity::class.java)
                    intent.putExtra("GROUP_ID", currentGroupId)
                    startActivity(intent)
                }
            }
            "APPLY" -> {
                btnTitle.text = "참여 신청하기"
                btnLayout.setOnClickListener {
                    showJoinDialog(currentGroupId.toLong(), data.hostNickname, data.bookTitle)
                }
            }
            "CANCEL" -> {
                btnTitle.text = "신청 취소하기"
                btnLayout.setOnClickListener { requestCancelGroup(currentGroupId.toLong()) }
            }
            "FULL" -> {
                btnTitle.text = "모집 완료"
                btnLayout.isEnabled = false
            }
            "TRACKER" -> {
                btnTitle.text = if (currentGroupType == "TOGETHER") "서재 보기" else "트래커 보기"
                btnLayout.setOnClickListener { finish() }
            }
        }
    }

    private fun showJoinDialog(groupId: Long, hostNickName: String, bookTitle: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogGroupJoinBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.dialogJoinHostTv.text = "[$hostNickName]"
        dialogBinding.dialogJoinInfoTv.text = bookTitle
        dialogBinding.dialogJoinCountTv.text = "0/200"

        dialogBinding.dialogJoinInputEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dialogBinding.dialogJoinCountTv.text = "${s?.length ?: 0}/200"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        dialogBinding.dialogJoinCloseIv.setOnClickListener { dialog.dismiss() }
        dialogBinding.dialogJoinCancelBtn.setOnClickListener { dialog.dismiss() }

        dialogBinding.dialogJoinEnterBtn.setOnClickListener {
            val message = dialogBinding.dialogJoinInputEt.text.toString().trim()
            if (message.isEmpty()) {
                showCustomToast("호스트에게 보낼 한 마디를 입력해주세요.", false)
                return@setOnClickListener
            }
            requestJoinGroup(groupId, message, dialog)
        }
        dialog.show()
    }

    private fun requestJoinGroup(groupId: Long, message: String, dialog: Dialog) {
        lifecycleScope.launch {
            try {
                val request = GroupItemDto.GroupApplyRequest(applyMsg = message)
                val response = RetrofitClient.api().applyGroup(groupId, request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    showCustomToast("정상적으로 신청되었습니다", true)
                    dialog.dismiss()
                    viewModel.fetchGroupDetail(currentGroupId.toInt())
                } else {
                    val msg = try { JSONObject(response.errorBody()?.string() ?: "{}").getString("message") } catch (e: Exception) { "신청 실패" }
                    showCustomToast(msg, false)
                }
            } catch (e: Exception) {
                showCustomToast("네트워크 오류", false)
            }
        }
    }

    private fun requestCancelGroup(groupId: Long) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().cancelGroupApplication(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    showCustomToast("신청 취소 완료되었습니다.", true)
                    finish()
                } else {
                    val msg = try { JSONObject(response.errorBody()?.string() ?: "{}").getString("message") } catch (e: Exception) { "취소 실패" }
                    showCustomToast(msg, false)
                }
            } catch (e: Exception) {
                showCustomToast("네트워크 오류", false)
            }
        }
    }

    private fun setupMoreMenu(isHost: Boolean) {
        binding.actGrpHoMoreIv.setOnClickListener {
            val currentStatus = viewModel.groupDetail.value?.groupStatus ?: "RECRUITING"
            val bottomSheet = GroupMoreBottomSheet(isHost, currentStatus) { action ->
                when (action) {
                    "EDIT" -> {
                        val currentData = viewModel.groupDetail.value
                        if (currentData != null) {
                            val intent = Intent(this, GroupGenerationActivity::class.java)
                            intent.putExtra("IS_EDIT_MODE", true)
                            intent.putExtra("GROUP_ID", currentGroupId.toInt())
                            intent.putExtra("BOOK_TITLE", currentData.bookTitle)
                            intent.putExtra("START_DATE", currentData.startDate)
                            intent.putExtra("PERIOD", currentData.readingPeriod)
                            intent.putExtra("COMMENT", currentData.groupComment)
                            intent.putExtra("CUSTOM_TAG", currentData.customTag)
                            intent.putStringArrayListExtra("TAGS", ArrayList(currentData.groupTags ?: emptyList()))
                            startActivity(intent)
                        }
                    }
                    "DELETE" -> showDeleteConfirmDialog()
                    "REPORT" -> {
                        val currentData = viewModel.groupDetail.value
                        val intent = Intent(this, GroupDetailReportContainerActivity::class.java)
                        if (currentData != null) {
                            intent.putExtra("GROUP_ID", currentGroupId)
                            intent.putExtra("GROUP_NAME", currentData.bookTitle)
                            val memberNames = currentData.participantSlots?.map { it.nickname }
                            intent.putStringArrayListExtra("MEMBER_LIST", ArrayList(memberNames))
                        }
                        startActivity(intent)
                    }
                }
            }
            bottomSheet.show(supportFragmentManager, "GroupMoreBottomSheet")
        }
    }

    private fun showDeleteConfirmDialog() {
        val bookTitle = viewModel.groupDetail.value?.bookTitle ?: ""
        CommonDialog(
            context = this,
            title = "그룹 삭제",
            subtitle = bookTitle,
            content = "정말로 이 모임을 삭제하시겠습니까?\n삭제 후에는 복구할 수 없습니다.",
            confirmBtnText = "삭제",
            confirmBtnColor = R.color.ui_point_red,
            onConfirmClick = { requestDeleteGroup(currentGroupId.toLong()) }
        ).show()
    }

    private fun requestDeleteGroup(groupId: Long) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().deleteGroup(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    showCustomToast("삭제가 완료되었습니다", true)
                    finish()
                } else {
                    val msg = try { JSONObject(response.errorBody()?.string() ?: "{}").getString("message") } catch (e: Exception) { "삭제 실패" }
                    showCustomToast(msg, false)
                }
            } catch (e: Exception) {
                showCustomToast("네트워크 오류", false)
            }
        }
    }

    private fun showCustomToast(message: String, isSuccess: Boolean) {
        val layout = LayoutInflater.from(this).inflate(R.layout.toast_custom, null)
        layout.findViewById<TextView>(R.id.toast_message_tv).text = message
        val iconRes = if (isSuccess) R.drawable.ic_check else R.drawable.ic_info
        layout.findViewById<ImageView>(R.id.toast_icon_iv).setImageResource(iconRes)

        with(Toast(applicationContext)) {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentGroupId > 0L) {
            viewModel.fetchGroupDetail(currentGroupId.toInt())
            viewModel.fetchComments(currentGroupId)
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}