package com.bookiibookii.bookiibookii.group

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.databinding.ActivityGrpJoinManagementBinding

class GroupJoinManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpJoinManagementBinding
    private lateinit var groupJoinAdapter: GroupJoinAdapter

    // 책 제목을 변수로 관리 (토스트에서도 쓰기 위해)
    private val currentBookTitle = "살인자의 기억법"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrpJoinManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initListener()
    }

    private fun initView() {
        val mockData = mutableListOf(
            GroupJoinData(1, null, "독서광", "2025.12.05", "저 진짜 열심히 할게요!", listOf("#메모환영", "#인사이트", "#열정")),
            GroupJoinData(2, null, "심심이", "2025.12.06", "안녕하세요", listOf("#하나")),
            GroupJoinData(3, null, "투머치토커", "2025.12.07", "태그 부자입니다.", listOf("#일", "#이", "#삼", "#사", "#오", "#육"))
        )

        groupJoinAdapter = GroupJoinAdapter(mockData) { item, isAccept ->
            if (isAccept) showAgreeDialog(item) else showRefusalDialog(item)
        }

        binding.groupRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@GroupJoinManagementActivity)
            adapter = groupJoinAdapter
        }
        updateCountText(mockData.size)
    }

    private fun updateCountText(count: Int) {
        binding.actGrpJoinMgTitleNumTv.text = "($count)"
    }

    private fun initListener() {
        binding.actGrpJoinMgBackIv.setOnClickListener { finish() }
    }

    // 커스텀 토스트 띄우기 함수
    private fun showCustomToast(message: String) {
        val inflater = LayoutInflater.from(this)
        val layout = inflater.inflate(R.layout.toast_custom, null)

        // 텍스트 설정
        val textView = layout.findViewById<TextView>(R.id.toast_message_tv)
        textView.text = message

        // 토스트 생성 및 설정
        with(Toast(applicationContext)) {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100) // 위치 조정 (하단에서 100만큼 위로)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    // 수락 다이얼로그
    private fun showAgreeDialog(item: GroupJoinData) {
        CommonDialog(
            context = this,
            title = "참여 요청 수락",
            subtitle = currentBookTitle,
            content = "${item.nickname} 님의 그룹 참여 요청을 수락하시겠습니까? 수락 즉시 그룹이 시작됩니다.",
            confirmBtnText = "수락",
            confirmBtnColor = R.color.grey_900,
            onConfirmClick = {
                // ★ 커스텀 토스트 호출 (수락 메시지)
                val msg = "${item.nickname} 님이 ${currentBookTitle}의 게스트가 되었습니다."
                showCustomToast(msg)

                groupJoinAdapter.removeItem(item)
                updateCountText(groupJoinAdapter.itemCount)
            }
        ).show()
    }

    // 거절 다이얼로그
    private fun showRefusalDialog(item: GroupJoinData) {
        CommonDialog(
            context = this,
            title = "참여 요청 거절",
            subtitle = currentBookTitle,
            content = "${item.nickname} 님의 그룹 참여 요청을 거절하시겠습니까? 상대방에게 거절 알림이 발송됩니다.",
            confirmBtnText = "거절",
            confirmBtnColor = R.color.ui_point_red,
            onConfirmClick = {
                // ★ 커스텀 토스트 호출 (거절 메시지)
                val msg = "${item.nickname} 님의 $currentBookTitle 그룹 요청을 거절했습니다."
                showCustomToast(msg)

                groupJoinAdapter.removeItem(item)
                updateCountText(groupJoinAdapter.itemCount)
            }
        ).show()
    }
}