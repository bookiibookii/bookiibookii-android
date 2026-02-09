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
import com.bookiibookii.bookiibookii.common.GroupTagMapper // 태그 변환기 (없으면 아래 주석 참고)
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.GroupItemDto
import com.bookiibookii.bookiibookii.databinding.ActivityGrpJoinManagementBinding
import kotlinx.coroutines.launch

class GroupJoinManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpJoinManagementBinding
    private lateinit var groupJoinAdapter: GroupJoinAdapter

    private var currentGroupId: Long = 0L
    private var currentBookTitle = "" // 이전 화면에서 받아오거나 API로 조회

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrpJoinManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Intent 데이터 수신
        val groupIdInt = intent.getIntExtra("GROUP_ID", 0)
        currentGroupId = groupIdInt.toLong()

        // 책 제목도 이전 화면에서 넘겨주면 좋습니다. (없으면 기본값)
        currentBookTitle = intent.getStringExtra("BOOK_TITLE") ?: "모임 신청 관리"

        if (currentGroupId == 0L) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initView()
        initListener()

        // 2. API 호출
        fetchApplicationList()
    }

    private fun initView() {
        // 어댑터 초기화 (처음엔 빈 리스트)
        groupJoinAdapter = GroupJoinAdapter(mutableListOf()) { item, isAccept ->
            if (isAccept) showAgreeDialog(item) else showRefusalDialog(item)
        }

        binding.groupRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@GroupJoinManagementActivity)
            adapter = groupJoinAdapter
        }

        // 책 제목 세팅 (XML에 해당 뷰가 있다면)
        // binding.bookTitleTv.text = currentBookTitle
    }

    // ★ 서버에서 신청 목록 가져오기
    private fun fetchApplicationList() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getGroupApplications(currentGroupId)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    val serverList = result?.applicationList ?: emptyList()
                    val totalCount = result?.totalCount ?: 0

                    // DTO -> GroupJoinData 변환
                    val uiList = serverList.map { serverItem ->

                        // 태그 변환: 서버 코드(ENGLISH) -> 화면용(#한글)
                        // GroupTagMapper가 없다면: serverItem.tags?.map { "#$it" } ?: emptyList() 로 대체
                        val displayTags = serverItem.tags?.map { tagCode ->
                            GroupTagMapper.toKoreanTag(tagCode)
                        } ?: emptyList()

                        GroupJoinData(
                            id = serverItem.applicationId.toInt(), // ID
                            profileResId = null, // API에 이미지 URL이 없으므로 null (Adapter가 기본 이미지 처리)
                            nickname = serverItem.name,
                            date = serverItem.createdAt,
                            intro = serverItem.applyMsg,
                            tags = displayTags
                        )
                    }.toMutableList()

                    // ★ 어댑터 데이터 갱신
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
                // 서버로 보낼 Body 생성
                val requestBody = GroupItemDto.GroupAppStatusRequest(status)

                // API 호출 (Int -> Long 변환 주의)
                val response = RetrofitClient.api().updateApplicationStatus(
                    applicationId.toLong(), // ★ 여기서 item.id를 넘겨야 함 (groupId 아님!)
                    requestBody
                )

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    // 성공 시 메시지 출력
                    val msg = if (status == "ACCEPTED") {
                        "$nickname 님이 게스트가 되었습니다."
                    } else {
                        "$nickname 님의 요청을 거절했습니다."
                    }
                    showCustomToast(msg)

                    // ★ 목록 새로고침 (중요)
                    fetchApplicationList()

                } else {
                    // 실패 시 에러 메시지
                    val errorMsg = response.body()?.message ?: "처리 실패"
                    Toast.makeText(this@GroupJoinManagementActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@GroupJoinManagementActivity, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- 다이얼로그 및 토스트 로직 ---

    private fun showAgreeDialog(item: GroupJoinData) {
        CommonDialog(
            context = this,
            title = "참여 요청 수락",
            subtitle = currentBookTitle,
            content = "${item.nickname} 님의 그룹 참여 요청을 수락하시겠습니까?",
            confirmBtnText = "수락",
            confirmBtnColor = R.color.grey_900,
            onConfirmClick = {
                // 수락 API 호출 ("ACCEPTED")
                processApplication(item.id, "ACCEPTED", item.nickname)
            }
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
            onConfirmClick = {
                // 거절 API 호출 ("REJECTED")
                processApplication(item.id, "REJECTED", item.nickname)
            }
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