package com.bookiibookii.bookiibookii.group

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.databinding.ActivityGrpGenerationBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GroupGenerationActivity : AppCompatActivity() {

    // 뷰 바인딩 선언
    private lateinit var binding: ActivityGrpGenerationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 바인딩 초기화 및 뷰 설정
        binding = ActivityGrpGenerationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    private fun initListener() {
        with(binding) {
            // 뒤로가기 버튼
            actGrpGenBackIv.setOnClickListener {
                finish()
            }

            // 시작 날짜 선택 (MaterialDatePicker)
            actGrpGenBookDateContainer.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("시작 날짜 선택")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()

                datePicker.addOnPositiveButtonClickListener { selection ->
                    val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
                    val dateString = sdf.format(Date(selection))

                    // 선택된 날짜 텍스트뷰에 반영
                    actGrpGenBookSelectDateTv.text = dateString
                    // 텍스트 색상을 검은색으로 변경 (혹시 초기값이 회색이었다면)
                    actGrpGenBookSelectDateTv.setTextColor(getColor(R.color.grey_900))
                }
                datePicker.show(supportFragmentManager, "DATE_PICKER")
            }

            // 실물 책 소지 여부 (네/아니오 버튼 토글)
            actGrpGenBookYesBtn.setOnClickListener {
                updateBookHaveState(true)
            }

            actGrpGenBookNoBtn.setOnClickListener {
                updateBookHaveState(false)
                showBuyDialog()
            }

            //  그룹 만들기 완료 버튼
            actGrpGenRunBtn.setOnClickListener {
                // TODO: 입력된 데이터 수집 및 서버 전송 로직
            }
        }
    }
    // 도서 구매 다이얼로그 띄우기
//    private fun showBuyDialog() {
//        val dialog = Dialog(this)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//
//        val dialogBinding = DialogGroupBuyBinding.inflate(layoutInflater)
//        dialog.setContentView(dialogBinding.root)
//
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.window?.setLayout(
//            WindowManager.LayoutParams.WRAP_CONTENT,
//            WindowManager.LayoutParams.WRAP_CONTENT
//        )
//
//        // [취소 버튼] -> 다이얼로그 닫고, 버튼 선택 상태 초기화
//        dialogBinding.dialogBuyCancelBtn.setOnClickListener {
//            dialog.dismiss()
//            updateBookHaveState(null) // 여기서 버튼 상태를 Reset
//        }
//
//        // [닫기 아이콘] -> 취소와 동일하게 처리
//        dialogBinding.dialogBuyCloseIv.setOnClickListener {
//            dialog.dismiss()
//            updateBookHaveState(null)
//        }
//
//        // [구매하러 가기 버튼] -> 토스트 메시지 후 다이얼로그 닫기
//        dialogBinding.dialogBuyEnterBtn.setOnClickListener {
//            Toast.makeText(this, "구매 사이트로 이동합니다 (추후 구현)", Toast.LENGTH_SHORT).show()
//            dialog.dismiss()
//            updateBookHaveState(null)
//        }
//
//        dialog.show()
//    }
    private fun showBuyDialog() {
        val dialog = CommonDialog(
            context = this,
            title = "책을 먼저 구매하시겠습니까?",
            subtitle = "괴테는 모든 것을 말했다",
            content = "그룹을 만들기 위해서는 실물 책이 필요합니다.구매페이지로 이동할까요?",
            confirmBtnText = "구매하러 가기",
            confirmBtnColor = R.color.grey_900,
            onConfirmClick = {
                // 구매 페이지 이동 로직
                Toast.makeText(this, "구매 페이지로 이동합니다.", Toast.LENGTH_SHORT).show()
                updateBookHaveState(null)

            },
            onCancelClick = {
                // 취소 시 (Reset) 로직
                updateBookHaveState(null)
            }
        )
        dialog.show()
    }

    private fun updateBookHaveState(isHave: Boolean?) {
        val mainColor = ContextCompat.getColorStateList(this, R.color.pre_main)
        val paleColor = ContextCompat.getColorStateList(this, R.color.pre_main_pale)
        val grey200 = ContextCompat.getColorStateList(this, R.color.grey_200)
        val white = ContextCompat.getColorStateList(this, R.color.white)

        val mainColorInt = ContextCompat.getColor(this, R.color.pre_main)
        val grey900Int = ContextCompat.getColor(this, R.color.grey_900)

        with(binding) {
            when (isHave) {
                true -> { // [네] 선택 시
                    actGrpGenBookYesBtn.strokeColor = mainColor
                    actGrpGenBookYesBtn.backgroundTintList = paleColor
                    actGrpGenBookYesBtn.setTextColor(mainColorInt)

                    actGrpGenBookNoBtn.strokeColor = grey200
                    actGrpGenBookNoBtn.backgroundTintList = white
                    actGrpGenBookNoBtn.setTextColor(grey900Int)
                }

                false -> { // [아니오] 선택 시
                    actGrpGenBookNoBtn.strokeColor = mainColor
                    actGrpGenBookNoBtn.backgroundTintList = paleColor
                    actGrpGenBookNoBtn.setTextColor(mainColorInt)

                    actGrpGenBookYesBtn.strokeColor = grey200
                    actGrpGenBookYesBtn.backgroundTintList = white
                    actGrpGenBookYesBtn.setTextColor(grey900Int)
                }

                null -> { // 취소 시 둘 다 비활성화
                    actGrpGenBookYesBtn.strokeColor = grey200
                    actGrpGenBookYesBtn.backgroundTintList = white
                    actGrpGenBookYesBtn.setTextColor(grey900Int)

                    actGrpGenBookNoBtn.strokeColor = grey200
                    actGrpGenBookNoBtn.backgroundTintList = white
                    actGrpGenBookNoBtn.setTextColor(grey900Int)
                }
            }
        }
    }
}