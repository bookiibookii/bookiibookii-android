package com.bookiibookii.bookiibookii.group

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.databinding.ActivityGrpJoinManagementBinding

class GroupJoinManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpJoinManagementBinding
    private lateinit var groupJoinAdapter: GroupJoinAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrpJoinManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initListener()
    }

    private fun initView() {
        //더미
        val mockData = mutableListOf(
            GroupJoinData(1, null, "독서광", "2025.12.05", "저 진짜 열심히 할게요!",
                listOf("#메모환영", "#인사이트", "#열정")),

            GroupJoinData(2, null, "심심이", "2025.12.06", "안녕하세요",
                listOf("#하나")),

            GroupJoinData(3, null, "투머치토커", "2025.12.07", "태그 부자입니다.",
                listOf("#일", "#이", "#삼", "#사", "#오", "#육"))

        )

        groupJoinAdapter = GroupJoinAdapter(mockData) { item, isAccept ->
            if (isAccept) {
                // 수락 버튼 클릭 시 -> 수락 다이얼로그 띄우기 (데이터 넘김)
                showAgreeDialog(item)
            } else {
                // 거절 버튼 클릭 시 -> 거절 다이얼로그 띄우기 (데이터 넘김)
                showRefusalDialog(item)
            }

        }

        // 리사이클러뷰 연결
        binding.groupRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@GroupJoinManagementActivity)
            adapter = groupJoinAdapter
        }

        // 초기 개수 세팅
        updateCountText(mockData.size)
    }

    private fun updateCountText(count: Int) {
        binding.actGrpJoinMgTitleNumTv.text = "($count)"
    }

    private fun initListener() {
        with(binding) {
            // 뒤로가기 버튼 기능
            actGrpJoinMgBackIv.setOnClickListener {
                finish()
            }
        }
    }
    // 수락 다이얼로그
    private fun showAgreeDialog(item: GroupJoinData) {
        val dialog = CommonDialog(
            context = this,
            title = "참여 요청 수락",
            subtitle = "살인자의 기억법",
            content = "${item.nickname} 님의 그룹 참여 요청을 수락하시겠습니까? 수락 즉시 그룹이 시작됩니다.",
            confirmBtnText = "수락",
            confirmBtnColor = R.color.grey_900,
            onConfirmClick = {
                // [확인 눌렀을 때 실행되는 진짜 로직]
                Toast.makeText(this, "${item.nickname}님이 그룹에 추가되었습니다.", Toast.LENGTH_SHORT).show()

                // 리스트에서 제거 및 개수 갱신
                groupJoinAdapter.removeItem(item)
                updateCountText(groupJoinAdapter.itemCount)
            }
        )
        dialog.show()
    }

    // 거절 다이얼로그
    private fun showRefusalDialog(item: GroupJoinData) {
        val dialog = CommonDialog(
            context = this,
            title = "참여 요청 거절",
            subtitle = "살인자의 기억법",
            content = "${item.nickname} 님의 그룹 참여 요청을 거절하시겠습니까? 상대방에게 거절 알림이 발송됩니다.",
            confirmBtnText = "거절",
            confirmBtnColor = R.color.ui_point_red,
            onConfirmClick = {
                Toast.makeText(this, "요청을 거절했습니다.", Toast.LENGTH_SHORT).show()

                // 리스트에서 제거 및 개수 갱신
                groupJoinAdapter.removeItem(item)
                updateCountText(groupJoinAdapter.itemCount)
            }
        )
        dialog.show()
    }
}
