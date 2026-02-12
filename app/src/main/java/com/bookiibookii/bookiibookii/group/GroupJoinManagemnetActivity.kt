package com.bookiibookii.bookiibookii.group

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.common.GroupTagMapper
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.GroupItemDto
import com.bookiibookii.bookiibookii.databinding.ActivityGrpJoinManagementBinding
import kotlinx.coroutines.launch

class GroupJoinManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpJoinManagementBinding
    private lateinit var groupJoinAdapter: GroupJoinAdapter

    private var currentGroupId: Long = 0L
    private var currentBookTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrpJoinManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Intent 데이터 수신 및 유효성 검사
        currentGroupId = intent.getLongExtra("GROUP_ID", 0L)
        currentBookTitle = intent.getStringExtra("BOOK_TITLE") ?: "모임 신청 관리"

        if (currentGroupId == 0L) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initView()
        initListener()
        fetchApplicationList()
    }

    private fun initView() {
        // 어댑터 초기화: 아이템 클릭 시 수락/거절 다이얼로그 노출
        groupJoinAdapter = GroupJoinAdapter(mutableListOf()) { item, isAccept ->
            if (isAccept) showAgreeDialog(item) else showRefusalDialog(item)
        }

        binding.groupRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@GroupJoinManagementActivity)
            adapter = groupJoinAdapter
        }
    }

    private fun initListener() {
        binding.actGrpJoinMgBackIv.setOnClickListener { finish() }
    }

    /**
     * 서버로부터 신청자 목록을 가져와 UI 데이터로 변환 후 리스트 갱신
     */
    private fun fetchApplicationList() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getGroupApplications(currentGroupId)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    val serverList = result?.applicationList ?: emptyList()
                    val totalCount = result?.totalCount ?: 0

                    // [핵심] DTO(서버 데이터) -> UI 전용 Model 변환
                    val uiList = serverList.map { serverItem ->
                        // 태그 변환: [ENG_CODE] -> [#한글태그]
                        val displayTags = serverItem.tags?.map { tagCode ->
                            GroupTagMapper.toKoreanTag(tagCode)
                        } ?: emptyList()

                        GroupJoinData(
                            id = serverItem.applicationId.toInt(),
                            profileImgUrl = serverItem.profileImageUrl,
                            nickname = serverItem.name,
                            date = serverItem.createdAt,
                            intro = serverItem.applyMsg,
                            tags = displayTags
                        )
                    }.toMutableList()

                    groupJoinAdapter.updateData(uiList)
                    updateCountText(totalCount)

                } else {
                    Toast.makeText(this@GroupJoinManagementActivity, "목록 로드 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("JoinManage", "Fetch Error", e)
                Toast.makeText(this@GroupJoinManagementActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCountText(count: Int) {
        binding.actGrpJoinMgTitleNumTv.text = "($count)"
    }

    /**
     * 신청 수락/거절 상태 변경 처리
     */
    private fun processApplication(applicationId: Int, status: String, nickname: String) {
        lifecycleScope.launch {
            try {
                val requestBody = GroupItemDto.GroupAppStatusRequest(status)
                val response = RetrofitClient.api().updateApplicationStatus(
                    applicationId.toLong(),
                    requestBody
                )

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val msg = if (status == "ACCEPTED") {
                        "$nickname 님이 게스트가 되었습니다."
                    } else {
                        "$nickname 님의 요청을 거절했습니다."
                    }
                    showCustomToast(msg)

                    // 처리 성공 후 리스트 재로딩 (성공한 아이템 제외 목적)
                    fetchApplicationList()
                } else {
                    val errorMsg = response.body()?.message ?: "처리 실패"
                    Toast.makeText(this@GroupJoinManagementActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("JoinManage", "Update Status Error", e)
                Toast.makeText(this@GroupJoinManagementActivity, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // region [다이얼로그 및 토스트 UI]

    private fun showAgreeDialog(item: GroupJoinData) {
        CommonDialog(
            context = this,
            title = "참여 요청 수락",
            subtitle = currentBookTitle,
            content = "${item.nickname} 님의 그룹 참여 요청을 수락하시겠습니까?",
            confirmBtnText = "수락",
            confirmBtnColor = R.color.grey_900,
            onConfirmClick = { processApplication(item.id, "ACCEPTED", item.nickname) }
        ).show()
    }

    private fun showRefusalDialog(item: GroupJoinData) {
        CommonDialog(
            context = this,
            title = "참여 요청 거절",
            subtitle = currentBookTitle,
            content = "${item.nickname} 님의 그룹 참여 요청을 거절하시겠습니까?",
            confirmBtnText = "거절",
            confirmBtnColor = R.color.ui_point_red,
            onConfirmClick = { processApplication(item.id, "REJECTED", item.nickname) }
        ).show()
    }


     // 디자인 가이드에 맞춘 커스텀 토스트 메시지
    private fun showCustomToast(message: String) {
        val inflater = LayoutInflater.from(this)
        val layout = inflater.inflate(R.layout.toast_custom, null)
        val textView = layout.findViewById<TextView>(R.id.toast_message_tv)
        textView.text = message

        with(Toast(applicationContext)) {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    // endregion
}