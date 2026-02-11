package com.bookiibookii.bookiibookii.group

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.common.GroupTagMapper
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.GroupItemDto
import com.bookiibookii.bookiibookii.databinding.ActivityGrpHostBinding
import com.bookiibookii.bookiibookii.databinding.DialogGroupJoinBinding
import com.bookiibookii.bookiibookii.group.generation.GroupGenerationActivity
import com.bookiibookii.bookiibookii.group.viewmodel.GroupDetailViewModel
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import org.json.JSONObject

class GroupDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpHostBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    // 어댑터 2개 (멤버 목록, 댓글 목록)
    private val memberAdapter = GroupMemberAdapter()

    // ★ [추가] 댓글 어댑터: 클릭 시 답글 모드로 전환
    private val commentAdapter = GroupChatAdapter { parentId, writerName ->
        activateReplyMode(parentId, writerName)
    }

    private val viewModel: GroupDetailViewModel by viewModels()
    private var currentGroupId: Long = -1L

    // ★ 대댓글 타겟 ID (null이면 일반 댓글)
    private var targetParentId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrpHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentGroupId = intent.getLongExtra("GROUP_ID", -1L)
        if (currentGroupId <= 0L) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initView()
        initBottomSheet()
        setupObserver()

        // 화면 진입 시 데이터 로드 (그룹 정보 + 댓글 목록)
        viewModel.fetchGroupDetail(currentGroupId.toInt())   // ✅ Int로 변환해서 호출
        viewModel.fetchComments(currentGroupId)              // ✅ Long 그대로
    }

    private fun initView() {
        // 1. 뒤로가기 등 기본 버튼
        binding.actGrpHoBackIv.setOnClickListener { handleBackPress() }

        // 2. 멤버 리스트 (상단)
        binding.actGrpHoMemberRv.adapter = memberAdapter

        // =========================================================
        // ★ 3. 댓글 리스트 연결 (새로운 XML ID 적용)
        // =========================================================
        binding.grpMgBottomSheetInfoRv.apply {
            layoutManager = LinearLayoutManager(this@GroupDetailActivity)
            adapter = commentAdapter
            // 아이템이 추가될 때 부드러운 애니메이션 (선택)
            itemAnimator = null
        }

        // =========================================================
        // ★ 4. 입력창 & 전송 버튼 로직
        // =========================================================
        val inputEt = binding.grpMgBottomSheetInputEt
        val sendBtn = binding.grpMgBottomSheetSendIv

        // 초기 상태: 비활성 색상
        sendBtn.setImageResource(R.drawable.ic_send)
        sendBtn.isEnabled = false

        // 텍스트 입력 감지 (글자가 있을 때만 전송 버튼 활성화)
        inputEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val hasText = !s.isNullOrBlank()
                if (hasText) {
                    // ★ 글자가 있으면 -> 검정색 아이콘(ic_send_black)으로 교체
                    sendBtn.setImageResource(R.drawable.ic_send_black)
                    sendBtn.isEnabled = true
                } else {
                    // ★ 글자가 없으면 -> 다시 회색 아이콘(ic_send)으로 복구
                    sendBtn.setImageResource(R.drawable.ic_send)
                    sendBtn.isEnabled = false
                }
            }
        })

        // 전송 버튼 클릭 리스너
        sendBtn.setOnClickListener {
            val content = inputEt.text.toString().trim()
            if (content.isNotEmpty()) {
                // ViewModel에 전송 요청
                viewModel.postComment(
                    groupId = currentGroupId.toLong(),
                    content = content,
                    parentId = targetParentId // 답글 모드일 경우 부모 ID 포함
                )
            }
        }

        // 새로고침 버튼 (바텀시트 헤더)
        binding.grpMgBottomSheetReloadIv.setOnClickListener {
            viewModel.fetchComments(currentGroupId.toLong())
            Toast.makeText(this, "댓글을 새로고침했습니다.", Toast.LENGTH_SHORT).show()
        }

        // (선택) 댓글 영역 빈 공간 클릭 시 키보드 내리기
        binding.grpMgBottomSheetInfoRv.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                hideKeyboard(inputEt)
                // 답글 모드였다면 취소할 수도 있음
                if (targetParentId != null) deactivateReplyMode()
            }
            false
        }
    }

    // 답글 모드 활성화 (어댑터에서 호출)
    private fun activateReplyMode(parentId: Long, writerName: String) {
        targetParentId = parentId
        binding.grpMgBottomSheetInputEt.hint = "$writerName 님에게 답글 작성..."

        // 키보드 올리기
        binding.grpMgBottomSheetInputEt.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.grpMgBottomSheetInputEt, InputMethodManager.SHOW_IMPLICIT)
    }

    // 답글 모드 해제 (전송 후 또는 취소 시)
    private fun deactivateReplyMode() {
        targetParentId = null
        binding.grpMgBottomSheetInputEt.hint = "텍스트 입력 전"
        binding.grpMgBottomSheetInputEt.clearFocus()
    }

    private fun setupObserver() {
        // [1] 그룹 상세 데이터 관찰
        viewModel.groupDetail.observe(this) { data ->
            if (data != null) {
                bindUi(data)
                handleButtonStatus(data)
                setupMoreMenu(data.isHost)
            }
        }

        // [2] ★ 댓글 리스트 관찰 (서버에서 오면 어댑터에 넣기)
        viewModel.commentList.observe(this) { list ->
            if (list != null) {
                commentAdapter.setComments(list)

                // (선택) 댓글 개수 표시 UI가 있다면
                binding.grpMgBottomSheetNumTitleTv.text = "${list.size}"
            }
        }

        // [3] ★ 댓글 작성 성공 관찰
        viewModel.commentWriteSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "등록 완료!", Toast.LENGTH_SHORT).show()

                // 입력창 초기화 & 답글 모드 해제
                binding.grpMgBottomSheetInputEt.setText("")
                binding.grpMgBottomSheetInputEt.hint = "댓글을 입력하세요"
                targetParentId = null

                hideKeyboard(binding.grpMgBottomSheetInputEt)

                // ★ 목록 새로고침! (내가 쓴 댓글 바로 보이게)
                viewModel.fetchComments(currentGroupId.toLong())
            }
        }

        // [4] 에러 메시지
        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrBlank()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // 키보드 내리기
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun bindUi(data: GroupItemDto.GroupDetailResult) {
        with(binding.actGrpHoIncludedItem) {
            grpItemBookTitleTv.text = data.bookTitle
            grpItemBookAuthorTv.text = data.author
            val genreText = data.category
            grpItemBookGenreTv.text = if (!genreText.isNullOrEmpty()) "($genreText)" else ""
            grpItemDateTv.text = data.startDate.replace("-", ".")
            grpItemNicknameTv.text = data.hostNickname
            grpItemStatusCp.text = if (data.groupStatus == "RECRUITING") "모집 중" else "마감"
            grpItemDeadlineNoTv.text = data.readingPeriod.toString()
            grpItemMemStatusNoTv.text = "${data.matchedCount}"

            Glide.with(this@GroupDetailActivity).load(data.bookImage).centerCrop().into(grpItemCoverIv)
            Glide.with(this@GroupDetailActivity).load(data.hostProfileImage).placeholder(R.drawable.ic_profile).circleCrop().into(grpItemProfileIv)

            grpItemHotCp.visibility = if (data.isHot) View.VISIBLE else View.GONE

            val chipList = listOf(grpItemHash1Cp, grpItemHash2Cp, grpItemHash3Cp, grpItemHash4Cp, grpItemHash5Cp)
            chipList.forEach { it.visibility = View.GONE }
            val displayTags = ArrayList<String>()
            data.groupTags?.forEach { displayTags.add(GroupTagMapper.toKoreanTag(it)) }
            if (!data.customTag.isNullOrBlank()) displayTags.add("#${data.customTag}")

            for (i in displayTags.indices) {
                if (i < chipList.size) {
                    chipList[i].text = displayTags[i]
                    chipList[i].visibility = View.VISIBLE
                }
            }
            grpItemBottomBtnLayout.visibility = View.VISIBLE
        }

        binding.actGrpHoIntroContTv.text = data.groupComment
        binding.actGrpHoMainTitleTv.text = data.title
        binding.actGrpHoMemberStatus1Tv.text = "${data.matchedCount}"
        binding.actGrpHoMemberStatus3Tv.text = "${data.maxCapacity}"
        memberAdapter.submitList(data.participantSlots)
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
                btnTitle.text = "활동/배송 현황"
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

        val closeListener = View.OnClickListener { dialog.dismiss() }
        dialogBinding.dialogJoinCloseIv.setOnClickListener(closeListener)
        dialogBinding.dialogJoinCancelBtn.setOnClickListener(closeListener)

        dialogBinding.dialogJoinEnterBtn.setOnClickListener {
            val message = dialogBinding.dialogJoinInputEt.text.toString().trim()
            if (message.isEmpty()) {
                Toast.makeText(this, "호스트에게 보낼 한 마디를 입력해주세요.", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@GroupDetailActivity, "신청되었습니다!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    viewModel.fetchGroupDetail(currentGroupId.toInt())

                } else {
                    val msg = try { JSONObject(response.errorBody()?.string() ?: "{}").getString("message") } catch (e: Exception) { "신청 실패" }
                    Toast.makeText(this@GroupDetailActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GroupDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestCancelGroup(groupId: Long) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().cancelGroupApplication(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(this@GroupDetailActivity, "신청 취소 완료", Toast.LENGTH_SHORT).show()
                    viewModel.fetchGroupDetail(currentGroupId.toInt())
                } else {
                    val msg = try { JSONObject(response.errorBody()?.string() ?: "{}").getString("message") } catch (e: Exception) { "취소 실패" }
                    Toast.makeText(this@GroupDetailActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GroupDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupMoreMenu(isHost: Boolean) {
        binding.actGrpHoMoreIv.setOnClickListener {
            val bottomSheet = GroupMoreBottomSheet(isHost) { action ->
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
                    Toast.makeText(this@GroupDetailActivity, "삭제 완료", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val msg = try { JSONObject(response.errorBody()?.string() ?: "{}").getString("message") } catch (e: Exception) { "삭제 실패" }
                    Toast.makeText(this@GroupDetailActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GroupDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentGroupId > 0L) {
            viewModel.fetchGroupDetail(currentGroupId.toInt())
            viewModel.fetchComments(currentGroupId)
        }
    }

    private fun initBottomSheet() {
        val bottomSheetLayout = binding.persistentBottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)

        // 1. 초기 설정: 입력창 높이(대략 60~80dp) + 여유분만큼만 빼꼼 나오게 설정
        // (너무 높으면 리스트가 보여서 지저분해 보임)
        bottomSheetBehavior.peekHeight = dpToPx(170)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // 2. 초기 상태: 헤더(새로고침, 댓글 글자)는 숨겨두기
        setSheetHeaderAlpha(0f)

        // 3. ★ 핵심: 드래그할 때마다 헤더 투명도 조절 (애니메이션 효과)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // 다 접혔을 때 확실하게 숨김 처리
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    setSheetHeaderAlpha(0f)
                }
                // 다 펼쳐졌을 때 확실하게 보임 처리
                else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    setSheetHeaderAlpha(1f)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // slideOffset: 0.0(접힘) ~ 1.0(펼쳐짐)
                // 바텀시트가 올라올수록 헤더가 서서히 나타나게 함
                setSheetHeaderAlpha(slideOffset)
            }
        })
    }
    private fun setSheetHeaderAlpha(alpha: Float) {
        // XML ID 확인 필요 (헤더에 있는 뷰들)
        // 댓글 개수
        binding.grpMgBottomSheetNumTitleTv.alpha = alpha
        // 새로고침 버튼
        binding.grpMgBottomSheetReloadIv.alpha = alpha

        // "댓글" 이라는 고정 텍스트 뷰가 있다면 그것도 포함 (ID가 grpMgBottomSheetTitleTv 라고 가정)
        // binding.grpMgBottomSheetTitleTv.alpha = alpha

        // 만약 헤더 전체를 감싸는 레이아웃(ConstraintLayout 등)이 있다면 그것만 조절해도 됨
        // binding.grpMgBottomSheetHeaderLayout.alpha = alpha
    }

    private fun handleBackPress() {
        if (::bottomSheetBehavior.isInitialized && bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            finish()
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}