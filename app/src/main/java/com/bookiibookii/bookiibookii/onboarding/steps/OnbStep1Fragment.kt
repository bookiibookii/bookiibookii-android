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

    private val vm: OnbViewModel by activityViewModels()

    private lateinit var chipArea: ConstraintLayout
    private lateinit var flow: Flow

    // ReadingPreference -> Chip View
    private val chipViewMap = linkedMapOf<ReadingPreference, TextView>()

    // 표시 순서 고정
    private val chipItems = listOf(
        ReadingPreference.ECONOMY,
        ReadingPreference.SCIENCE_IT,
        ReadingPreference.NOVEL_GENRE,
        ReadingPreference.POEM_ESSAY,
        ReadingPreference.HOME_HOBBY,
        ReadingPreference.ART_CULTURE,
        ReadingPreference.HUMANITIES_HISTORY,
        ReadingPreference.SELF_HELP,
        ReadingPreference.POLITICS_SOCIETY,
        ReadingPreference.ETC
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindQuestionCard(view)

        chipArea = view.findViewById(R.id.chipArea)
        flow = view.findViewById(R.id.flowChips)

        buildChips()

        // 상태 관찰 → 선택 UI + 다음 버튼 활성화 갱신
        vm.state.observe(viewLifecycleOwner) { state ->
            chipViewMap.forEach { (pref, chipView) ->
                chipView.isSelected = state.readingPreferences.contains(pref)
            }
        }
    }

    private fun bindQuestionCard(root: View) {
        val card = root.findViewById<View>(R.id.includeQuestionCard)

        val tvTitle = card.findViewById<TextView>(R.id.tvQuestionTitle)
        val tvDesc = card.findViewById<TextView>(R.id.tvQuestionDesc)

        tvTitle.text = "어떤 책을 펼칠 때\n가장 설레나요?"
        tvDesc.text = "좋아하는 분야를 3가지까지 골라주세요."
    }

    private fun buildChips() {
        val chipIds = ArrayList<Int>()

        chipItems.forEach { pref ->
            val chip = createChip(pref)
            chipArea.addView(chip)

            chipViewMap[pref] = chip
            chipIds.add(chip.id)
        }

        flow.referencedIds = chipIds.toIntArray()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun createChip(pref: ReadingPreference): TextView {
        return TextView(requireContext()).apply {
            id = View.generateViewId()
            text = pref.displayName
            tag = pref

            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                dp(44)
            )

            gravity = Gravity.CENTER
            setPadding(dp(22), 0, dp(22), 0)

            textSize = 14f
            setTextColor(requireContext().getColorStateList(R.color.selector_onb_step1_chip))
            background = requireContext().getDrawable(R.drawable.bg_onb_chip_selector)

            isClickable = true
            isFocusable = true

            setOnClickListener {
                vm.togglePreference(tag as ReadingPreference)
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}