package com.bookiibookii.bookiibookii.onboarding.steps

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R

class OnbStep1Fragment : Fragment(R.layout.fragment_onb_step1) {

    // 최대 선택 가능 개수
    private val maxSelect = 3

    // 현재 선택된 칩 관리 (중복 방지 + 순서 유지)
    private val selectedChips = linkedSetOf<TextView>()

    // Step1 카테고리 목록
    private val chipItems = listOf(
        "경제/경영", "과학/IT", "소설/장르", "시/에세이",
        "가정/취미", "예술/문화", "인문/역사",
        "자기계발", "정치/사회", "기타"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 질문 카드 문구 세팅
        bindQuestionCard(view)

        // 카테고리 칩 생성 및 Flow 연결
        setupChips(view)

        // 최초 진입 시 다음 버튼 비활성
        (activity as? OnbStepHost)?.setNextEnabled(false)
    }

    // 질문 카드 텍스트 설정
    private fun bindQuestionCard(root: View) {
        val card = root.findViewById<View>(R.id.includeQuestionCard)

        val tvTitle = card.findViewById<TextView>(R.id.tvQuestionTitle)
        val tvDesc = card.findViewById<TextView>(R.id.tvQuestionDesc)

        tvTitle.text = "어떤 책을 펼칠 때\n가장 설레나요?"
        tvDesc.text = "좋아하는 분야를 3가지까지 골라주세요."
    }

    // 카테고리 칩을 동적으로 생성하고 Flow에 연결
    private fun setupChips(root: View) {
        val chipArea = root.findViewById<ConstraintLayout>(R.id.chipArea)
        val flow = root.findViewById<androidx.constraintlayout.helper.widget.Flow>(R.id.flowChips)

        val chipIds = ArrayList<Int>()

        chipItems.forEach { label ->
            val chip = createChip(label)
            chipArea.addView(chip)
            chipIds.add(chip.id)
        }

        flow.referencedIds = chipIds.toIntArray()
    }

    // 카테고리 선택용 Chip View 생성
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun createChip(label: String): TextView {
        return TextView(requireContext()).apply {
            id = View.generateViewId()
            text = label

            // Flow 기준 크기 (높이 고정, 가로는 내용 기준)
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                dp(44)
            )

            gravity = Gravity.CENTER
            setPadding(dp(22), 0, dp(22), 0)

            textSize = 14f
            setTextColor(requireContext().getColorStateList(R.color.selector_onb_chip))
            background = requireContext().getDrawable(R.drawable.bg_onb_chip_selector)

            isClickable = true
            isFocusable = true
            isSelected = false

            setOnClickListener { handleChipClick(this) }
        }
    }

    // 칩 선택/해제 처리 및 최대 선택 수 제한
    private fun handleChipClick(chip: TextView) {
        if (chip.isSelected) {
            chip.isSelected = false
            selectedChips.remove(chip)
        } else {
            if (selectedChips.size >= maxSelect) return
            chip.isSelected = true
            selectedChips.add(chip)
        }

        // 선택된 칩이 하나라도 있으면 다음 버튼 활성
        (activity as? OnbStepHost)?.setNextEnabled(selectedChips.isNotEmpty())
    }

    // dp → px 변환 유틸
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    // 선택된 카테고리 텍스트 목록 반환 (서버 전달용)
    fun getSelectedCategories(): List<String> {
        return selectedChips.map { it.text.toString() }
    }
}