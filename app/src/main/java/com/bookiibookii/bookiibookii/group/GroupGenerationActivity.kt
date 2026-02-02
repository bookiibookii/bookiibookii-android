package com.bookiibookii.bookiibookii.group

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.databinding.ActivityGrpGenerationBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GroupGenerationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpGenerationBinding

    private var isEditMode = false
    private var groupType = "RELAY" // "RELAY"(이어읽기) or "TOGETHER"(함께읽기)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGrpGenerationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 데이터 수신
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        groupType = intent.getStringExtra("GROUP_TYPE") ?: "RELAY"

        // 2. 화면 초기화 (통합 함수 호출)
        updateUiState()

        // 3. 리스너 초기화
        initListener()
    }

    // ★ [핵심] 모드(생성/수정)와 타입(함께/이어)에 따라 UI 상태を一괄 설정
    private fun updateUiState() {
        val isRelay = groupType == "RELAY"

        with(binding) {
            // ==========================================
            // 1. 텍스트 및 기본 정보 설정 (수정/생성)
            // ==========================================
            if (isEditMode) {
                actGrpGenMainTitleTv.text = "그룹 수정"
                actGrpGenRunBtn.text = "수정 완료"

                // 수정 모드: 상단 도서 제목 표시
                val bookTitle = intent.getStringExtra("BOOK_TITLE") ?: "제목 없음"
                actGrpGenEditBookTitleTv.visibility = View.VISIBLE
                actGrpGenEditBookTitleTv.text = bookTitle

                // 수정 모드 데이터 바인딩 예시
                val startDate = intent.getStringExtra("START_DATE")
                val duration = intent.getStringExtra("DURATION")
                if (startDate != null) {
                    actGrpGenBookSelectDateTv.text = startDate
                    actGrpGenBookSelectDateTv.setTextColor(getColor(R.color.grey_900))
                }
                if (duration != null) {
                    actGrpGenBookLimitBar.setText(duration)
                }

            } else {
                actGrpGenMainTitleTv.text = "그룹 만들기"
                actGrpGenRunBtn.text = "그룹 만들기"

                // 생성 모드: 상단 도서 제목 숨김 (검색해서 넣어야 하니까)
                actGrpGenEditBookTitleTv.visibility = View.GONE

                actGrpGenBookSelectDateTv.text = ""
                actGrpGenBookLimitBar.setText("")
            }

            // 가시성(Visibility) 상세 로직

            // A. [도서 검색창] : 오직 '생성 모드'일 때만 보임
            val searchVisibility = if (!isEditMode) View.VISIBLE else View.GONE
            actGrpGenBookSerachTv.visibility = searchVisibility
            actGrpGenBookSerachStarTv.visibility = searchVisibility
            actGrpGenBookSearchBar.visibility = searchVisibility

            // B. [책 소유 여부] : '생성 모드'이면서 && '이어읽기'일 때만 보임
            // (함께읽기는 각자 사니까 불필요, 수정 모드는 이미 책이 정해졌으니 불필요)
            if (!isEditMode && isRelay) {
                actGrpGenBookPossessionGroup.visibility = View.VISIBLE
            } else {
                actGrpGenBookPossessionGroup.visibility = View.GONE
            }

            // C. [교환 방법] : '이어읽기'일 때만 보임 (수정/생성 모두)
            if (isRelay) {
                actGrpGenMethodContainer.visibility = View.VISIBLE
                // 수정 모드라면 기존 저장된 값에 따라 배송/직접 버튼 세팅하는 로직 필요
            } else {
                actGrpGenMethodContainer.visibility = View.GONE
                actGrpGenDirectInputGroup.visibility = View.GONE // 세부 입력창도 확실히 닫기
            }

            // D. [최대 인원] : '함께읽기'일 때만 보임 (수정/생성 모두)
            if (!isRelay) { // TOGETHER
                actGrpGenMemberCountGroup.visibility = View.VISIBLE
            } else { // RELAY
                actGrpGenMemberCountGroup.visibility = View.GONE
            }
        }
    }

    private fun initListener() {
        with(binding) {
            // 뒤로가기
            actGrpGenBackIv.setOnClickListener { finish() }

            // 교환 방법 (이어읽기 전용)
            actGrpGenBookDeliveryBtn.setOnClickListener { updateExchangeMethodState(false) }
            actGrpGenBookDirectBtn.setOnClickListener { updateExchangeMethodState(true) }

            // 시작 날짜
            actGrpGenBookDateContainer.setOnClickListener {
                val today = MaterialDatePicker.todayInUtcMilliseconds()
                val tomorrow = today + (24L * 60 * 60 * 1000)

                val constraintsBuilder = CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.from(tomorrow))

                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("시작 날짜 선택")
                    .setSelection(tomorrow)
                    .setCalendarConstraints(constraintsBuilder.build())
                    .setTheme(R.style.CustomDatePickerTheme)
                    .build()

                datePicker.addOnPositiveButtonClickListener { selection ->
                    val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
                    actGrpGenBookSelectDateTv.text = sdf.format(Date(selection))
                    actGrpGenBookSelectDateTv.setTextColor(getColor(R.color.grey_900))
                }
                datePicker.show(supportFragmentManager, "DATE_PICKER")
            }

            // 책 소유 여부 (생성 + 이어읽기 전용)
            actGrpGenBookYesBtn.setOnClickListener { updateBookHaveState(true) }
            actGrpGenBookNoBtn.setOnClickListener {
                updateBookHaveState(false)
                showBuyDialog()
            }

            // 완료 버튼
            actGrpGenRunBtn.setOnClickListener {
                // TODO: 유효성 검사 (함께읽기면 인원수 체크, 이어읽기면 주소 체크 등)
                if (isEditMode) {
                    Toast.makeText(this@GroupGenerationActivity, "수정이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    val typeMsg = if (groupType == "TOGETHER") "함께읽기" else "이어읽기"
                    Toast.makeText(this@GroupGenerationActivity, "$typeMsg 그룹 생성 완료!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showBuyDialog() {
        CommonDialog(
            context = this,
            title = "책을 먼저 구매하시겠습니까?",
            subtitle = "선택하신 도서",
            content = "그룹을 만들기 위해서는 실물 책이 필요합니다.\n구매페이지로 이동할까요?",
            confirmBtnText = "구매하러 가기",
            confirmBtnColor = R.color.grey_900,
            onConfirmClick = {
                Toast.makeText(this, "구매 페이지로 이동합니다.", Toast.LENGTH_SHORT).show()
                updateBookHaveState(null)
            },
            onCancelClick = { updateBookHaveState(null) }
        ).show()
    }

    // 버튼 상태 업데이트 (책 소유)
    private fun updateBookHaveState(isHave: Boolean?) {
        val mainColor = ContextCompat.getColorStateList(this, R.color.pre_main)
        val paleColor = ContextCompat.getColorStateList(this, R.color.pre_main_pale)
        val grey200 = ContextCompat.getColorStateList(this, R.color.grey_200)
        val white = ContextCompat.getColorStateList(this, R.color.white)
        val mainColorInt = ContextCompat.getColor(this, R.color.pre_main)
        val grey900Int = ContextCompat.getColor(this, R.color.grey_900)

        with(binding) {
            when (isHave) {
                true -> {
                    actGrpGenBookYesBtn.strokeColor = mainColor
                    actGrpGenBookYesBtn.backgroundTintList = paleColor
                    actGrpGenBookYesBtn.setTextColor(mainColorInt)
                    actGrpGenBookNoBtn.strokeColor = grey200
                    actGrpGenBookNoBtn.backgroundTintList = white
                    actGrpGenBookNoBtn.setTextColor(grey900Int)
                }
                false -> {
                    actGrpGenBookNoBtn.strokeColor = mainColor
                    actGrpGenBookNoBtn.backgroundTintList = paleColor
                    actGrpGenBookNoBtn.setTextColor(mainColorInt)
                    actGrpGenBookYesBtn.strokeColor = grey200
                    actGrpGenBookYesBtn.backgroundTintList = white
                    actGrpGenBookYesBtn.setTextColor(grey900Int)
                }
                null -> {
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

    // 버튼 상태 업데이트 (교환 방법)
    private fun updateExchangeMethodState(isDirect: Boolean?) {
        val mainColor = ContextCompat.getColorStateList(this, R.color.pre_main)
        val paleColor = ContextCompat.getColorStateList(this, R.color.pre_main_pale)
        val grey200 = ContextCompat.getColorStateList(this, R.color.grey_200)
        val white = ContextCompat.getColorStateList(this, R.color.white)
        val mainColorInt = ContextCompat.getColor(this, R.color.pre_main)
        val grey500Int = ContextCompat.getColor(this, R.color.grey_500)

        with(binding) {
            when (isDirect) {
                true -> { // 직접 교환
                    actGrpGenBookDirectBtn.strokeColor = mainColor
                    actGrpGenBookDirectBtn.backgroundTintList = paleColor
                    actGrpGenBookDirectBtn.setTextColor(mainColorInt)
                    actGrpGenBookDeliveryBtn.strokeColor = grey200
                    actGrpGenBookDeliveryBtn.backgroundTintList = white
                    actGrpGenBookDeliveryBtn.setTextColor(grey500Int)
                    actGrpGenDirectInputGroup.visibility = View.VISIBLE
                }
                false -> { // 택배 교환
                    actGrpGenBookDeliveryBtn.strokeColor = mainColor
                    actGrpGenBookDeliveryBtn.backgroundTintList = paleColor
                    actGrpGenBookDeliveryBtn.setTextColor(mainColorInt)
                    actGrpGenBookDirectBtn.strokeColor = grey200
                    actGrpGenBookDirectBtn.backgroundTintList = white
                    actGrpGenBookDirectBtn.setTextColor(grey500Int)
                    actGrpGenDirectInputGroup.visibility = View.GONE
                }
                null -> { // 초기 상태
                    actGrpGenBookDirectBtn.strokeColor = grey200
                    actGrpGenBookDirectBtn.backgroundTintList = white
                    actGrpGenBookDirectBtn.setTextColor(grey500Int)
                    actGrpGenBookDeliveryBtn.strokeColor = grey200
                    actGrpGenBookDeliveryBtn.backgroundTintList = white
                    actGrpGenBookDeliveryBtn.setTextColor(grey500Int)
                    actGrpGenDirectInputGroup.visibility = View.GONE
                }
            }
        }
    }
}