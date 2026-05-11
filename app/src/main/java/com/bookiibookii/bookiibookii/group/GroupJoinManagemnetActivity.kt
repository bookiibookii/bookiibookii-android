package com.bookiibookii.bookiibookii.group

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.BaseActivity
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.common.GroupTagMapper
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.group.GroupAppStatusRequest
import com.bookiibookii.bookiibookii.databinding.ActivityGrpJoinManagementBinding
import kotlinx.coroutines.launch

class GroupJoinManagementActivity : BaseActivity<ActivityGrpJoinManagementBinding>() {

    override fun getViewBinding(): ActivityGrpJoinManagementBinding {
        return ActivityGrpJoinManagementBinding.inflate(layoutInflater)
    }
    private lateinit var groupJoinAdapter: GroupJoinAdapter

    private var currentGroupId: Long = 0L
    private var currentBookTitle = "모임 신청 관리"
    private var currentGroupType: String = "TOGETHER" // ★ 그룹 유형 저장 변수 추가

    // ★ 로컬 리스트 관리를 위한 멤버 변수
    private val applicationList = mutableListOf<GroupJoinData>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentGroupId = intent.getLongExtra("GROUP_ID", 0L)
        intent.getStringExtra("BOOK_TITLE")?.let { currentBookTitle = it }

        if (currentGroupId == 0L) {
            showCustomToast("잘못된 접근입니다.",false)
            finish()
            return
        }

        initView()
        initListener()
        fetchGroupInfo() // 여기서 그룹 타입(TOGETHER/RELAY)을 확인합니다.
        fetchApplicationList()
    }

    private fun initView() {
        groupJoinAdapter = GroupJoinAdapter(mutableListOf()) { item, isAccept ->
            if (isAccept) showAgreeDialog(item) else showRefusalDialog(item)
        }

        binding.groupRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@GroupJoinManagementActivity)
            adapter = groupJoinAdapter
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        }
    }

    private fun fetchGroupInfo() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.grpApi().getGroupDetail(currentGroupId.toInt())
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val groupData = response.body()?.result
                    if (groupData != null) {
                        currentBookTitle = groupData.bookTitle
                        currentGroupType = intent.getStringExtra("GROUP_TYPE") ?: "TOGETHER"
                    }
                }
            } catch (e: Exception) {
                Log.e("JoinManage", "Title Fetch Error", e)
            }
        }
    }

    private fun fetchApplicationList() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.grpApi().getGroupApplications(currentGroupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    val serverList = result?.applicationList ?: emptyList()

                    val uiList = serverList.map { serverItem ->
                        val displayTags = serverItem.tags?.map { GroupTagMapper.toKoreanTag(it) } ?: emptyList()
                        GroupJoinData(
                            id = serverItem.applicationId.toInt(),
                            profileImgUrl = serverItem.profileImageUrl,
                            nickname = serverItem.name,
                            date = serverItem.createdAt,
                            intro = serverItem.applyMsg,
                            tags = displayTags
                        )
                    }

                    // ★ 리스트 갱신
                    applicationList.clear()
                    applicationList.addAll(uiList)
                    groupJoinAdapter.updateData(applicationList)
                    updateCountText(applicationList.size)

                } else {
                    showCustomToast("목록 로드 실패", false)
                }
            } catch (e: Exception) {
                showCustomToast("네트워크 오류", false)
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
                val requestBody = GroupAppStatusRequest(status)
                val response = RetrofitClient.grpApi().updateApplicationStatus(applicationId.toLong(), requestBody)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val msg = if (status == "ACCEPTED") "$nickname 님이 게스트가 되었습니다." else "$nickname 님의 요청을 거절했습니다."
                    showCustomToast(msg, true)

                    // ★ 즉시 삭제 로직
                    val targetItem = applicationList.find { it.id == applicationId }
                    if (targetItem != null) {
                        applicationList.remove(targetItem)
                        groupJoinAdapter.updateData(applicationList)
                        updateCountText(applicationList.size)
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "처리 실패"
                    showCustomToast(errorMsg, false)
                }
            } catch (e: Exception) {
                showCustomToast("네트워크 오류 발생", false)
            }
        }
    }

    // --- 다이얼로그 및 토스트 ---
    private fun showAgreeDialog(item: GroupJoinData) {
        // ★ [핵심] 그룹 유형에 따른 문구 분기 처리
        val contentMsg = if (currentGroupType == "RELAY") {
            "${item.nickname} 님의 그룹 참여 요청을 수락하시겠습니까? 수락 즉시 그룹이 시작됩니다."
        } else {
            "${item.nickname} 님의 그룹 참여 요청을 수락하시겠습니까? 인원이 다 차면 그룹이 시작됩니다."
        }

        CommonDialog(
            context = this,
            title = "참여 요청 수락",
            subtitle = currentBookTitle,
            content = contentMsg,
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
            content = "${item.nickname} 님의 그룹 참여 요청을 거절하시겠습니까? 상대방에게 거절 알림이 발송됩니다.",
            confirmBtnText = "거절",
            confirmBtnColor = R.color.ui_point_red,
            onConfirmClick = { processApplication(item.id, "REJECTED", item.nickname) }
        ).show()
    }

}