package com.bookiibookii.bookiibookii.group

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.GroupTagMapper
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.GroupItemDto
import com.bookiibookii.bookiibookii.databinding.ActivityGrpHostBinding
import com.bookiibookii.bookiibookii.databinding.DialogGroupJoinBinding
import com.bookiibookii.bookiibookii.group.viewmodel.GroupDetailViewModel
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import org.json.JSONObject

class GroupDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpHostBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val memberAdapter = GroupMemberAdapter()
    private val viewModel: GroupDetailViewModel by viewModels()
    private var currentGroupId: Int = 0

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

        // 화면 진입 시 데이터 로드
        viewModel.fetchGroupDetail(currentGroupId)
    }

    private fun initView() {
        binding.actGrpHoBackIv.setOnClickListener { handleBackPress() }
        binding.actGrpHoMemberRv.adapter = memberAdapter

        // 바텀시트 내부 버튼들
        binding.grpMgBottomSheetSendIv.setOnClickListener {
            val text = binding.grpMgBottomSheetInputEt.text.toString()
            if (text.isNotBlank()) {
                Toast.makeText(this, "전송: $text", Toast.LENGTH_SHORT).show()
                binding.grpMgBottomSheetInputEt.text.clear()
            }
        }
        binding.grpMgBottomSheetReloadIv.setOnClickListener {
            viewModel.fetchGroupDetail(currentGroupId)
        }
    }

    private fun setupObserver() {
        viewModel.groupDetail.observe(this) { data ->
            if (data != null) {
                bindUi(data)
                handleButtonStatus(data) // 상태에 따라 버튼(참여/취소/관리) 분기
                setupMoreMenu(data.isHost)
            }
        }
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

            // 태그 처리
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

    // ★ 버튼 상태 관리 (여기가 중요!)
    private fun handleButtonStatus(data: GroupItemDto.GroupDetailResult) {
        val itemBinding = binding.actGrpHoIncludedItem
        val btnLayout = itemBinding.grpItemManageBtn
        val btnTitle = itemBinding.grpItemBtnTitleTv
        val btnCount = itemBinding.grpItemBtnNumTv

        // 초기화
        btnLayout.setOnClickListener(null)
        btnCount.visibility = View.GONE
        btnLayout.isEnabled = true
        // 배경색 등 스타일 초기화 필요 시 추가

        when (data.buttonStatus) {
            "MANAGE" -> { // 방장
                btnTitle.text = "참여 요청 관리"
                btnCount.visibility = View.VISIBLE
                btnCount.text = "(${data.waitingCount})"
                btnLayout.setOnClickListener {
                    val intent = Intent(this, GroupJoinManagementActivity::class.java)
                    intent.putExtra("GROUP_ID", currentGroupId)
                    startActivity(intent)
                }
            }
            "APPLY" -> { // 참여 가능 -> 다이얼로그 띄우기
                btnTitle.text = "참여 신청하기"
                btnLayout.setOnClickListener {
                    // ★ showJoinDialog 호출 시 데이터 전달
                    showJoinDialog(
                        groupId = currentGroupId.toLong(),
                        hostNickName = data.hostNickname,
                        bookTitle = data.bookTitle
                    )
                }
            }
            "CANCEL" -> { // 이미 신청함 -> 취소 로직
                btnTitle.text = "신청 취소하기"
                btnLayout.setOnClickListener {
                    requestCancelGroup(currentGroupId.toLong())
                }
            }
            "FULL" -> { // 모집 마감
                btnTitle.text = "모집 완료"
                btnLayout.isEnabled = false
            }
            "TRACKER" -> { // 활동 중
                btnTitle.text = "활동/배송 현황"
                btnLayout.setOnClickListener {
                    // 트래커 이동
                }
            }
        }
    }

    // ★ 참여 신청 다이얼로그
    private fun showJoinDialog(groupId: Long, hostNickName: String, bookTitle: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogGroupJoinBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 1. 정보 세팅
        dialogBinding.dialogJoinHostTv.text = "[$hostNickName]"
        dialogBinding.dialogJoinInfoTv.text = bookTitle
        dialogBinding.dialogJoinCountTv.text = "0/200"

        // 2. 글자 수 체크
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

        // 3. 확인 버튼 -> API 호출
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

    // ★ API 호출 및 화면 갱신
    private fun requestJoinGroup(groupId: Long, message: String, dialog: Dialog) {
        lifecycleScope.launch {
            try {
                val request = GroupItemDto.GroupApplyRequest(applyMsg = message)
                val response = RetrofitClient.api().applyGroup(groupId, request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(this@GroupDetailActivity, "신청되었습니다!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()

                    //성공했으니 화면을 새로고침 -> 버튼이 "신청 취소하기"로 바뀜
                    viewModel.fetchGroupDetail(currentGroupId)
                } else {
                    val errorString = response.errorBody()?.string()
                    val msg = try {
                        JSONObject(errorString ?: "{}").getString("message")
                    } catch (e: Exception) {
                        "신청에 실패했습니다."
                    }
                    Toast.makeText(this@GroupDetailActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@GroupDetailActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestCancelGroup(groupId: Long) {
        lifecycleScope.launch {
            try {
                // API 호출
                val response = RetrofitClient.api().cancelGroupApplication(groupId)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(this@GroupDetailActivity, "신청이 취소되었습니다.", Toast.LENGTH_SHORT).show()

                    // ★ 핵심: 성공 후 화면 새로고침! -> 버튼이 다시 "참여 신청하기"로 바뀜
                    viewModel.fetchGroupDetail(currentGroupId)

                } else {
                    // 실패 메시지 파싱
                    val errorString = response.errorBody()?.string()
                    val msg = try {
                        JSONObject(errorString ?: "{}").getString("message")
                    } catch (e: Exception) {
                        "취소에 실패했습니다."
                    }
                    Toast.makeText(this@GroupDetailActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@GroupDetailActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupMoreMenu(isHost: Boolean) { /* 구현 생략 */ }

    private fun initBottomSheet() {
        val bottomSheetLayout = binding.persistentBottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetBehavior.peekHeight = dpToPx(120) // 필요에 따라 조정
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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