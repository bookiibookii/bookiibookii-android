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
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.common.GroupTagMapper
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.GroupItemDto
import com.bookiibookii.bookiibookii.databinding.ActivityGrpHostBinding // XML 파일명에 맞게 수정하세요
import com.bookiibookii.bookiibookii.databinding.DialogGroupJoinBinding
import com.bookiibookii.bookiibookii.group.generation.GroupGenerationActivity
import com.bookiibookii.bookiibookii.group.viewmodel.GroupDetailViewModel
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.roundToInt

class GroupDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpHostBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout> // 타입 변경 (View -> ConstraintLayout)

    private val memberAdapter = GroupMemberAdapter()


    // 댓글 어댑터: (부모ID, 작성자명) 콜백 -> 답글 모드 진입
    private var allCommentsList: List<GroupItemDto.CommentItem> = emptyList()

    // 어댑터 클릭 리스너 수정
    private val commentAdapter = GroupChatAdapter { commentId, writerName ->
        // 1. 전체 부모 리스트(allCommentsList)에서 클릭된 ID를 찾습니다.
        // 자식 댓글(대댓글)은 allCommentsList 안에 중첩되어 있으므로 find로 찾아지지 않습니다.
        val isParentItem = allCommentsList.any { it.id == commentId }

        if (isParentItem) {
            // 부모 댓글인 경우에만 답글 모드 실행
            enterReplyMode(commentId, writerName)
        } else {
            // 자식 댓글을 클릭했을 때는 아무 동작도 하지 않음 (필요 시 토스트 메시지)
          }
    }

    private val viewModel: GroupDetailViewModel by viewModels()
    private val userViewModel: MyPageViewModel by viewModels()
    private var currentGroupId: Int = 0
    private var targetParentId: Long? = null

    private var isReplyMode = false

    private var isSecretMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrpHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentGroupId = intent.getIntExtra("GROUP_ID", 0)
        if (currentGroupId == 0) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initView()
        initBottomSheet()
        setupObserver()

        initSecretLock()
        initInputListener()

        userViewModel.fetchMypageData()
        viewModel.fetchGroupDetail(currentGroupId)
        viewModel.fetchComments(currentGroupId.toLong())


        binding.grpMgBottomSheetBackIv.setOnClickListener {
            exitReplyMode() // 전체 목록으로 복귀
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 1. 답글 모드라면 -> 답글 모드 해제
                if (isReplyMode) {
                    exitReplyMode()
                    return
                }
                // 2. 바텀시트가 열려있다면 -> 닫기
                if (::bottomSheetBehavior.isInitialized && bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    return
                }
                // 3. 그 외 -> 액티비티 종료
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        })

    }

    // ========================================================================
    // ★ [핵심] 답글 모드 진입 (Activity가 UI 상태를 변경)
    // ========================================================================
    private fun enterReplyMode(parentId: Long, writerName: String) {
        isReplyMode = true
        targetParentId = parentId // 답글 작성 시 보낼 부모 ID 저장

        // 1. 헤더 UI 변경 ("댓글 3" -> "답글")
        binding.grpMgBottomSheetBackIv.visibility = View.VISIBLE
        binding.grpMgBottomSheetTitleTv.text = "답글"
        binding.grpMgBottomSheetNumTitleTv.visibility = View.GONE // 개수 숨김 (또는 필요시 답글 수 표시)

        // 2. 리스트 필터링 (선택한 댓글과 그 자식들만 필터링해서 어댑터에 전달)
        // (서버 구조에 따라 parentId가 자기 자신이거나, parentId 필드가 일치하는 것 추출)
        val targetThread = allCommentsList.filter {
            it.id == parentId || (it.parentId == parentId && it.parentId != 0L)
        }
        commentAdapter.setComments(targetThread)

        // 3. 입력창 힌트 변경 & 포커스
        binding.grpMgBottomSheetInputEt.hint = "$writerName 님에게 답글 작성..."
        showKeyboard()

        // 4. 바텀시트가 닫혀있다면 열기
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun showKeyboard() {
        binding.grpMgBottomSheetInputEt.requestFocus()
        // 약간의 딜레이를 주어야 바텀시트가 펼쳐진 후 키보드가 안정적으로 올라옵니다.
        binding.grpMgBottomSheetInputEt.postDelayed({
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.grpMgBottomSheetInputEt, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    // ========================================================================
    // ★ [핵심] 답글 모드 해제 (원상 복구)
    // ========================================================================
    private fun exitReplyMode() {
        isReplyMode = false
        targetParentId = null // 부모 ID 초기화

        // 1. 헤더 UI 복구
        binding.grpMgBottomSheetBackIv.visibility = View.GONE
        binding.grpMgBottomSheetTitleTv.text = "댓글"
        binding.grpMgBottomSheetNumTitleTv.visibility = View.VISIBLE
        binding.grpMgBottomSheetNumTitleTv.text = "${allCommentsList.size}"

        // 2. 리스트 전체 복구
        commentAdapter.setComments(allCommentsList)

        // 3. 입력창 초기화
        binding.grpMgBottomSheetInputEt.hint = "텍스트 입력 전" // 기본 힌트로 복귀
        binding.grpMgBottomSheetInputEt.clearFocus()
        hideKeyboard()
    }

    private fun initView() {
        // 상단 뒤로가기
        binding.actGrpHoBackIv.setOnClickListener { handleBackPress() }

        // 멤버 리스트
        binding.actGrpHoMemberRv.adapter = memberAdapter

        // 댓글 리스트 설정
        binding.grpMgBottomSheetInfoRv.apply {
            layoutManager = LinearLayoutManager(this@GroupDetailActivity)
            adapter = commentAdapter
            itemAnimator = null // 깜빡임 방지
            addItemDecoration(GroupChatAdapter.VerticalSpaceItemDecoration(12))

            // 빈 공간 터치 시 키보드 내리기
            setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                    hideKeyboard()
                }
                false
            }
        }

        // 입력창 & 전송 버튼
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

        // 전송 버튼 클릭
        sendBtn.setOnClickListener {
            val content = inputEt.text.toString().trim()
            if (content.isNotEmpty()) {
                // ★ viewModel에 현재 상태의 parentId를 함께 전달 (일반 댓글이면 null, 답글이면 값 존재)
                viewModel.postComment(
                    groupId = currentGroupId.toLong(),
                    content = content,
                    parentId = targetParentId,
                    secret = isSecretMode
                )
            }
        }

        // 새로고침
        binding.grpMgBottomSheetReloadIv.setOnClickListener {
            viewModel.fetchComments(currentGroupId.toLong())
        }

    }

    private fun initBottomSheet() {
        // XML에서 정의한 ID 연결
        val bottomSheetLayout = binding.persistentBottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)

        // 1. PeekHeight 설정 (접혔을 때 보이는 높이)
        // XML에서 app:behavior_peekHeight="170dp"로 설정했으므로 코드 생략 가능하나, 명시적으로 넣어도 됨
        // bottomSheetBehavior.peekHeight = dpToPx(170)

        // 2. 초기 상태: 접힘
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // 3. 콜백 설정 (드래그 시 동작)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // 필요 시 상태 변화에 따른 로직 추가 (예: 뒤로가기 버튼 모양 변경 등)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // slideOffset: 0.0 (접힘) -> 1.0 (펼쳐짐)
                // 새로고침 아이콘 투명도 조절 (펼칠수록 잘 보이게)
                binding.grpMgBottomSheetReloadIv.alpha = slideOffset
            }
        })
    }

    private fun deactivateReplyMode() {
        targetParentId = null
        binding.grpMgBottomSheetInputEt.hint = "텍스트 입력 전" // 원래 힌트로 복구
        binding.grpMgBottomSheetInputEt.clearFocus()
        // 키보드 내리기는 상황에 따라 결정
    }
    private fun initInputListener() {
        binding.grpMgBottomSheetSendIv.setOnClickListener {
            val content = binding.grpMgBottomSheetInputEt.text.toString().trim()
            if (content.isNotEmpty()) {
                viewModel.postComment(
                    groupId = currentGroupId.toLong(),
                    content = content,
                    parentId = targetParentId,
                    secret = isSecretMode // ★ 이 값을 서버 리퀘스트에 담아 보냅니다.
                )
            }
        }

        // (선택) 빈 공간 터치 시 답글 모드 취소하고 싶다면
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
                Toast.makeText(this, "비밀댓글 모드 활성", Toast.LENGTH_SHORT).show()
            } else {
                binding.ivLock.setImageResource(R.drawable.ic_lock)
            }
        }
    }
    private fun setupObserver() {

        // 1. 내 정보(마이페이지) 관찰 -> 내 닉네임 확보
        userViewModel.profileData.observe(this) { profile ->
            if (profile != null) {
                val myNickname = profile.nickname
                // 현재 멤버 리스트가 있다면 내 닉네임을 반영해서 ME 뱃지 갱신
                val currentSlots = viewModel.groupDetail.value?.participantSlots
                memberAdapter.submitList(currentSlots, myNickname)
                val detail = viewModel.groupDetail.value
                updateMemberAdapter(detail?.participantSlots, detail?.hostProfileImageUrl)
            }
        }

        // 2. 그룹 상세 정보 관찰 (하나로 통합)
        viewModel.groupDetail.observe(this) { data ->
            if (data != null) {
                bindUi(data)
                handleButtonStatus(data)
                setupMoreMenu(data.isHost)

                // 상세 데이터 로드 시점에 내 닉네임이 로드되어 있다면 같이 전달
                val myNickname = userViewModel.profileData.value?.nickname
                memberAdapter.submitList(data.participantSlots, myNickname)

                updateMemberAdapter(data.participantSlots, data.hostProfileImageUrl)
            }
        }

        // 3. 댓글 리스트 관찰
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

        // 4. 댓글 작성 성공 및 에러 처리 (기존과 동일)
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

        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrBlank()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
    private fun handleBackPress() {
        // 바텀시트가 열려있으면 먼저 닫기
        if (::bottomSheetBehavior.isInitialized && bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            finish()
        }
    }

//    override fun onBackPressed() {
//        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
//            if (isReplyMode) {
//                exitReplyMode() // 답글 모드 -> 전체 목록
//                return
//            }
//            // 전체 목록이면 바텀시트 닫기 (기존 로직)
//        }
//        super.onBackPressed()
//    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.grpMgBottomSheetInputEt.windowToken, 0)
    }




    private fun bindUi(data: GroupItemDto.GroupDetailResult) {

        // 1. 직접 교환 여부 확인
        val isDirectExchange = !data.meetPlace.isNullOrBlank()

        if (isDirectExchange) {
            // 직접거래일 때: 레이아웃을 보여주고 실제 장소명을 세팅
            binding.actGrpHoRegionLayout.visibility = View.VISIBLE
            binding.actGrpHoIntroRealRegionTv.text = data.meetPlace
        } else {
            // 택배일 때 (meetPlace가 null인 경우): 레이아웃 자체를 숨김
            binding.actGrpHoRegionLayout.visibility = View.GONE
        }

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
            Glide.with(this@GroupDetailActivity).load(data.hostProfileImageUrl).placeholder(R.drawable.ic_profile).circleCrop().into(grpItemProfileIv)

            grpItemHotCp.visibility = if (data.isHot) View.VISIBLE else View.GONE

            // 모든 칩 리스트 (XML에 5개 이상 넉넉히 있다고 가정)
            with(binding.actGrpHoIncludedItem.grpItemChipGroup) {
                removeAllViews() // 초기화

                val displayTags = ArrayList<String>()
                if (!data.customTag.isNullOrBlank()) displayTags.add("#${data.customTag}")
                data.groupTags?.forEach { displayTags.add(GroupTagMapper.toKoreanTag(it)) }

                // 상세 페이지는 개수 제한 없이 루프 실행
                displayTags.forEach { tagText ->
                    val chip = layoutInflater.inflate(R.layout.item_chip_tag, this, false) as Chip
                    chip.text = tagText
                    this.addView(chip)
                }
            }

            grpItemBottomBtnLayout.visibility = View.VISIBLE

        }

        binding.actGrpHoIntroContTv.text = data.groupComment
        binding.actGrpHoIntroRealRegionTv.text = data.preferRegion
        binding.actGrpHoMainTitleTv.text = data.title
        binding.actGrpHoMemberStatus1Tv.text = "${data.matchedCount}"
        binding.actGrpHoMemberStatus3Tv.text = "${data.maxCapacity}"
//        memberAdapter.submitList(data.participantSlots)

        val processedSlots = data.participantSlots?.map { slot ->
            if (slot.role == "HOST" && slot.profileImage.isNullOrBlank()) {
                // 호스트인데 이미지가 없다면, 상단 호스트 프로필 URL을 복사해서 넣어줌
                slot.copy(profileImage = data.hostProfileImageUrl)
            } else {
                slot
            }
        }

        updateMemberAdapter(data.participantSlots, data.hostProfileImageUrl)
    }

    private fun updateMemberAdapter(slots: List<GroupItemDto.ParticipantSlot>?, hostProfile: String?) {
        val myNickname = userViewModel.profileData.value?.nickname

        val processed = slots?.map { slot ->
            if (slot.role == "HOST") {
                // 호스트라면 상단 프로필 이미지(hostProfile)를 강제로 꽂아줌
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
                    viewModel.fetchGroupDetail(currentGroupId)
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
                    viewModel.fetchGroupDetail(currentGroupId)
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
                            intent.putExtra("GROUP_ID", currentGroupId)
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
        if (currentGroupId != 0) {
            viewModel.fetchGroupDetail(currentGroupId)
            // 화면 돌아올 때마다 댓글도 새로고침하려면 여기 추가
            viewModel.fetchComments(currentGroupId.toLong())
        }
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

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}