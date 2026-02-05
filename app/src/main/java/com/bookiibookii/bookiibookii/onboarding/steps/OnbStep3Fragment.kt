package com.bookiibookii.bookiibookii.onboarding.steps

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.onboarding.steps.data.ReadingPace

class OnbStep3Fragment : Fragment(R.layout.fragment_onb_step3) {

    // 온보딩 전체 상태를 공유하는 ViewModel
    private val viewModel: OnbViewModel by activityViewModels()

    // ReadingPace ↔ optionView 매핑 (선택 상태 UI 반영용)
    private val optionViews = LinkedHashMap<ReadingPace, View>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 질문 카드(타이틀/설명) 텍스트 바인딩
        bindQuestionCard(view)

        val paceList = view.findViewById<ViewGroup>(R.id.paceList)

        // 재진입/재바인딩 시 중복 생성 방지
        paceList.removeAllViews()
        optionViews.clear()

        // 독서 페이스 옵션을 화면에 동적으로 생성
        addChipOption(paceList, ReadingPace.FAST, topMarginDp = 0)
        addChipOption(paceList, ReadingPace.WEEKLY, topMarginDp = 10)
        addChipOption(paceList, ReadingPace.MONTHLY, topMarginDp = 10)
        addTextOnlyOption(paceList, ReadingPace.UNKNOWN, topMarginDp = 10)

        // 상태 변화 감지 → 선택된 페이스에 따라 UI 자동 갱신
        viewModel.state.observe(viewLifecycleOwner) { state ->
            updateSelectionUi(state.readingPace)
        }
    }

    // Step3 질문 카드 문구 설정
    private fun bindQuestionCard(root: View) {
        val card = root.findViewById<View>(R.id.includeQuestionCard)

        val tvTitle = card.findViewById<TextView>(R.id.tvQuestionTitle)
        val tvDesc = card.findViewById<TextView>(R.id.tvQuestionDesc)

        tvTitle.setText(R.string.onb_step3_title)
        tvDesc.setText(R.string.onb_step3_desc)
    }

    // 배지(약 3일/약 1주/약 1개월)가 있는 옵션 아이템 생성
    private fun addChipOption(parent: ViewGroup, pace: ReadingPace, topMarginDp: Int) {
        val item = layoutInflater.inflate(R.layout.item_onb_step3_option_chip, parent, false)

        item.findViewById<TextView>(R.id.tv_chip).text = pace.badge
        item.findViewById<TextView>(R.id.tv_text).text = pace.displayName

        if (topMarginDp > 0) setTopMargin(item, dpToPx(topMarginDp))

        // 같은 항목 재클릭 시 해제까지 지원
        bindToggleClick(item, pace)

        parent.addView(item)
        optionViews[pace] = item
    }

    // 텍스트만 있는 옵션(아직 모르겠어요) 아이템 생성
    private fun addTextOnlyOption(parent: ViewGroup, pace: ReadingPace, topMarginDp: Int) {
        val item = layoutInflater.inflate(R.layout.item_onb_step3_option_text, parent, false)

        item.findViewById<TextView>(R.id.tv_text).text = pace.displayName

        if (topMarginDp > 0) setTopMargin(item, dpToPx(topMarginDp))

        // 같은 항목 재클릭 시 해제까지 지원
        bindToggleClick(item, pace)

        parent.addView(item)
        optionViews[pace] = item
    }

    // 단일 선택 + 재클릭 해제 토글 처리
    private fun bindToggleClick(view: View, pace: ReadingPace) {
        view.setOnClickListener {
            val current = viewModel.state.value?.readingPace

            // 동일 항목 재클릭 → 선택 해제
            if (current == pace) {
                viewModel.clearReadingPace()
            } else {
                // 다른 항목 클릭 → 단일 선택 갱신
                viewModel.selectReadingPace(pace)
            }
        }
    }

    // ViewModel 상태를 기준으로 선택 UI를 일괄 반영
    private fun updateSelectionUi(selected: ReadingPace?) {
        optionViews.forEach { (pace, view) ->
            view.isSelected = (pace == selected)
        }
    }

    // 옵션 간 간격(상단 margin)을 dp 단위로 통일 적용
    private fun setTopMargin(view: View, px: Int) {
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = px }
    }

    // dp → px 변환 유틸
    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
}