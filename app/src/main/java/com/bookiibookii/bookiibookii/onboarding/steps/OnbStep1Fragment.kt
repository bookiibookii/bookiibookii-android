package com.bookiibookii.bookiibookii.onboarding.steps

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.onboarding.steps.data.ReadingPreference

class OnbStep1Fragment : Fragment(R.layout.fragment_onb_step1) {

    // 온보딩 전체 상태를 공유하는 ViewModel
    private val vm: OnbViewModel by activityViewModels()

    // Chip들을 담는 부모 레이아웃
    private lateinit var chipArea: ConstraintLayout
    private lateinit var flow: Flow

    // ReadingPreference ↔ Chip View 매핑 (선택 상태 반영용)
    private val chipViewMap = linkedMapOf<ReadingPreference, TextView>()

    // 칩 표시 순서를 고정하기 위한 리스트
    private val chipItems = listOf(
        ReadingPreference.ECON_BIZ,
        ReadingPreference.SCI_IT,
        ReadingPreference.NOVEL_GENRE,
        ReadingPreference.POEM_ESSAY,
        ReadingPreference.HOME_HOBBY,
        ReadingPreference.ART_CULTURE,
        ReadingPreference.HUMAN_HISTORY,
        ReadingPreference.SELF_DEV,
        ReadingPreference.POL_SOC,
        ReadingPreference.ESC
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 질문 카드(타이틀/설명) 텍스트 바인딩
        bindQuestionCard(view)

        // Chip 영역 및 Flow 초기화
        chipArea = view.findViewById(R.id.chipArea)
        flow = view.findViewById(R.id.flowChips)

        // ReadingPreference 기반으로 Chip 동적 생성
        buildChips()

        // 상태 변화 감지 → 선택된 취향에 따라 Chip UI 갱신
        vm.state.observe(viewLifecycleOwner) { state ->
            chipViewMap.forEach { (pref, chipView) ->
                chipView.isSelected = state.readingPreferences.contains(pref)
            }
        }
    }

    // Step1 질문 카드 문구 설정
    private fun bindQuestionCard(root: View) {
        val card = root.findViewById<View>(R.id.includeQuestionCard)

        val tvTitle = card.findViewById<TextView>(R.id.tvQuestionTitle)
        val tvDesc = card.findViewById<TextView>(R.id.tvQuestionDesc)

        tvTitle.setText(R.string.onb_step1_title)
        tvDesc.setText(R.string.onb_step1_desc)
    }

    // Chip 목록 생성 및 Flow에 연결
    private fun buildChips() {
        val chipIds = ArrayList<Int>()

        chipItems.forEach { pref ->
            val chip = createChip(pref)
            chipArea.addView(chip)

            chipViewMap[pref] = chip
            chipIds.add(chip.id)
        }

        // Flow가 관리할 Chip id 목록 설정
        flow.referencedIds = chipIds.toIntArray()
    }

    // 개별 ReadingPreference에 대응하는 Chip 생성
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun createChip(pref: ReadingPreference): TextView {
        return TextView(requireContext()).apply {
            id = View.generateViewId()
            text = pref.displayName
            tag = pref

            // 높이를 고정해 Chip 형태 유지
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                dp(44)
            )

            gravity = Gravity.CENTER
            setPadding(dp(22), 0, dp(22), 0)

            textSize = 14f
            setTextColor(requireContext().getColorStateList(R.color.selector_onb_step_text))
            background = requireContext().getDrawable(R.drawable.bg_onb_chip_selector)

            // 선택 가능한 UI 요소로 동작하도록 설정
            isClickable = true
            isFocusable = true

            // 클릭 시 ViewModel에 선택/해제 요청
            setOnClickListener {
                vm.togglePreference(tag as ReadingPreference)
            }
        }
    }

    // dp → px 변환 유틸
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}