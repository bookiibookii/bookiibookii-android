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
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.databinding.ActivityGrpHostBinding
import com.bookiibookii.bookiibookii.databinding.DialogGroupJoinBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class GroupDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpHostBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    // 호스트 여부를 저장할 변수
    private var isHost: Boolean = false

    // 현재 접속자 ID와 방장 ID
    private val currentUserId = "user123"
    private val groupHostId = "user123" // 나중엔 DB에서 받아온 값

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrpHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 권한 체크 (DB 데이터 로딩 후 실행)
        checkUserAuthority()

        initView()
        initBottomSheet()

        val itemBinding = binding.actGrpHoIncludedItem

        if (isHost) {
            // [호스트 모드]
            // 텍스트 설정: "참여 요청 관리"
            itemBinding.grpItemMgBtnTitleTv.text = "참여 요청 관리"

            // 숫자 보이기 & 데이터 설정
            itemBinding.grpItemMgBtnNumTv.visibility = View.VISIBLE
            itemBinding.grpItemMgBtnNumTv.text = "(5)" // 실제 데이터 넣기

            // 버튼 클릭 시 관리 페이지로 이동
            itemBinding.grpItemMgManageBtn.setOnClickListener {
                val intent = Intent(this, GroupJoinManagementActivity::class.java)

                startActivity(intent)
            }

        } else {
            // [게스트 모드 - 기본]
            // 텍스트 설정: "참여 신청하기"
            itemBinding.grpItemMgBtnTitleTv.text = "참여 신청하기"

            // 숫자 숨기기
            itemBinding.grpItemMgBtnNumTv.visibility = View.GONE

            // 버튼 클릭 시 참여 신청 로직
            itemBinding.grpItemMgManageBtn.setOnClickListener {
            showJoinDialog()
            }
        }
    }

    private fun initView() {
        // 뒤로가기 버튼
        binding.actGrpHoBackIv.setOnClickListener {
            handleBackPress()
        }

        // 전송 버튼
        binding.grpMgBottomSheetSendIv.setOnClickListener {
            val text = binding.grpMgBottomSheetInputEt.text.toString()
            if(text.isNotBlank()) {
                Toast.makeText(this, "전송: $text", Toast.LENGTH_SHORT).show()
                binding.grpMgBottomSheetInputEt.text.clear()
            }
        }

        // 새로고침 버튼
        binding.grpMgBottomSheetReloadIv.setOnClickListener {
            Toast.makeText(this, "새로고침 클릭", Toast.LENGTH_SHORT).show()
        }

    }
    private fun checkUserAuthority() {
        isHost = (currentUserId == groupHostId)

        if (isHost) {
            // 호스트일 때만 보여줄 것들 세팅
            setupHostMode()
        } else {
            // 게스트일 때 세팅
            setupGuestMode()
        }
    }

    private fun setupHostMode() {
        // 호스트는 '더보기' 눌렀을 때 [수정, 삭제] 가 뜸
        binding.actGrpHoMoreIv.setOnClickListener {
            val bottomSheet = GroupMoreBottomSheet(isHost = true) { action ->
                when (action) {
                    "EDIT" -> Toast.makeText(this, "수정 화면으로 이동", Toast.LENGTH_SHORT).show()
                    "DELETE" -> showDeleteDialog()
                }
            }
            bottomSheet.show(supportFragmentManager, "GroupMoreBottomSheet")
        }
    }

    private fun setupGuestMode() {
        // 게스트는 '더보기' 눌렀을 때 [신고, 나가기] 가 뜸
        binding.actGrpHoMoreIv.setOnClickListener {
            val bottomSheet = GroupMoreBottomSheet(isHost = false) { action ->
                when (action) {
                    "REPORT" -> Toast.makeText(this, "신고하기 화면으로 이동", Toast.LENGTH_SHORT).show()
                }
            }
            bottomSheet.show(supportFragmentManager, "GroupMoreBottomSheet")
        }
    }


    private fun initBottomSheet() {
        val bottomSheetLayout = binding.persistentBottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)

        // 기본 바텀시트 표시영역 설정
        bottomSheetBehavior.peekHeight = dpToPx(170)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.isHideable = false

        //바텀시트 움직임 감지
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // slideOffset: 접힘(0.0) -> 펼침(1.0)
                // 새로고침 아이콘이 서서히 나타나게 함
                binding.grpMgBottomSheetReloadIv.alpha = slideOffset
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }
        })

        // 뒤로가기 처리
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })
    }

    private fun handleBackPress() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            finish()
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }


    private fun showJoinDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogGroupJoinBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        // 바인딩 변수로 뷰 접근
        dialogBinding.dialogJoinInputEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                dialogBinding.dialogJoinCountTv.text = "$length/200"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        dialogBinding.dialogJoinCloseIv.setOnClickListener { dialog.dismiss() }
        dialogBinding.dialogJoinCancelBtn.setOnClickListener { dialog.dismiss() }

        dialogBinding.dialogJoinEnterBtn.setOnClickListener {
            val message = dialogBinding.dialogJoinInputEt.text.toString()
            Toast.makeText(this, "신청 내용: $message", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteDialog() {
        val dialog = CommonDialog(
            context = this,
            title = "그룹 삭제",
            subtitle = "괴테는 모든 것을 말했다",
            content = "그룹을 정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.",
            confirmBtnText = "삭제",
            confirmBtnColor = R.color.ui_point_red,
            onConfirmClick = {
                // 삭제 API 호출 로직
                Toast.makeText(this, "그룹이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
        )
        dialog.show()
    }
}