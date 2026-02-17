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
import com.bookiibookii.bookiibookii.common.GroupTagMapper // [중요] 태그 변환기
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.GroupItemDto
import com.bookiibookii.bookiibookii.databinding.ActivityGrpJoinManagementBinding
import kotlinx.coroutines.launch

class GroupJoinManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpJoinManagementBinding
    private lateinit var groupJoinAdapter: GroupJoinAdapter

    private var currentGroupId: Long = 0L
    private var currentBookTitle = "모임 신청 관리"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrpJoinManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentGroupId = intent.getLongExtra("GROUP_ID", 0L)

        // 1. Intent 데이터 수신

        intent.getStringExtra("BOOK_TITLE")?.let {
            currentBookTitle = it
        }

        if (currentGroupId == 0L) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initView()
        initListener()
        fetchGroupInfo()
        fetchApplicationList()
    }

    private fun initView() {

        groupJoinAdapter = GroupJoinAdapter(mutableListOf()) { item, isAccept ->
            if (isAccept) showAgreeDialog(item) else showRefusalDialog(item)
        }

        binding.groupRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@GroupJoinManagementActivity)
            adapter = groupJoinAdapter
        }
    }

    private fun fetchGroupInfo() {
        lifecycleScope.launch {
            try {
                // 기존에 있던 그룹 상세 조회 API 재활용
                val response = RetrofitClient.api().getGroupDetail(currentGroupId.toInt())

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val groupData = response.body()?.result
                    if (groupData != null) {
                        currentBookTitle = groupData.bookTitle
                    }
                }
            } catch (e: Exception) {
                Log.e("JoinManage", "Title Fetch Error", e)
            }
        }
    }

    // ★ [핵심 수정] 데이터 로드 및 변환
    private fun fetchApplicationList() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getGroupApplications(currentGroupId)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    val serverList = result?.applicationList ?: emptyList()
                    val totalCount = result?.totalCount ?: 0

                    // DTO -> UI Model 변환
                    val uiList = serverList.map { serverItem ->

                        // 1. 태그 변환: [ENG_CODE] -> [#한글태그]
                        val displayTags = serverItem.tags?.map { tagCode ->
                            GroupTagMapper.toKoreanTag(tagCode)
                        } ?: emptyList()

                        // 2. 데이터 객체 생성
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
                Log.e("JoinManage", "Error", e)
                Toast.makeText(this@GroupJoinManagementActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCountText(count: Int) {
        binding.actGrpJoinMgTitleNumTv.text = "($count)"
    }

    private fun initListener() {
        binding.actGrpJoinMgBackIv.setOnClickListener { finish() }
    }

    private fun processApplication(applicationId: Int, status: String, nickname: String) {
        lifecycleScope.launch {
            try {
                val requestBody = GroupItemDto.GroupAppStatusRequest(status)
                val response = RetrofitClient.api().updateApplicationStatus(
                    applicationId.toLong(),
                    requestBody
                )

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val msg = if (status == "ACCEPTED") "$nickname 님이 게스트가 되었습니다." else "$nickname 님의 요청을 거절했습니다."
                    showCustomToast(msg)
                    //fetchApplicationList() // 목록 갱신 // 있으면 삭제됨
                } else {
                    val errorMsg = response.body()?.message ?: "처리 실패"
                    Toast.makeText(this@GroupJoinManagementActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@GroupJoinManagementActivity, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- 다이얼로그 및 토스트 ---
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
}