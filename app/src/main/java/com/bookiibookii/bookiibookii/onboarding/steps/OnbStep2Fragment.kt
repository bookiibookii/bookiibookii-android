package com.bookiibookii.bookiibookii.onboarding.steps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.onboarding.steps.data.RecordMethod

class OnbStep2Fragment : Fragment(R.layout.fragment_onb_step2) {

    // 온보딩 전체 상태를 공유하는 ViewModel
    private val vm: OnbViewModel by activityViewModels()

    // 상단 4개 기록 방식이 들어가는 리스트 (divider 포함)
    private lateinit var methodList: LinearLayout

    // 하단 카드 내부 컨테이너 (전체 선택 / 모름)
    private lateinit var containerAll: ViewGroup
    private lateinit var containerUnknown: ViewGroup

    // RecordMethod ↔ itemView 매핑 (선택 상태 UI 반영용)
    private val methodViewMap = linkedMapOf<RecordMethod, View>()

    // 하단 카드에 들어가는 실제 itemView
    private lateinit var viewAll: View
    private lateinit var viewUnknown: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 질문 카드(타이틀/설명) 텍스트 바인딩
        bindQuestionCard(view)

        // 필요한 View 참조 바인딩
        bindViews(view)

        // 상단 기록 방식 아이템 + divider 생성
        buildMethodItemsWithDividers()

        // 하단 카드 내부 옵션 아이템 생성
        buildOptionItemsInCards()

        // 상태 변화 감지 → 선택 상태에 따라 UI 자동 갱신
        vm.state.observe(viewLifecycleOwner) { state ->
            // 상단 4개 기록 방식 선택 상태 반영
            methodViewMap.forEach { (method, itemView) ->
                val selected = state.recordMethods.contains(method)

                itemView.isSelected = selected
                itemView.findViewById<ImageView>(R.id.ivIcon).isSelected = selected
                itemView.findViewById<TextView>(R.id.tvTitle).isSelected = selected
            }

            // "모든 방식을 환영해요" 선택 상태 반영
            val allSelected = vm.isStep2AllSelected()
            viewAll.isSelected = allSelected
            viewAll.findViewById<ImageView>(R.id.ivIcon).isSelected = allSelected
            viewAll.findViewById<TextView>(R.id.tvTitle).isSelected = allSelected

            // "아직 잘 모르겠어요" 선택 상태 반영
            val unknownSelected = vm.isStep2Unknown()
            viewUnknown.isSelected = unknownSelected
            viewUnknown.findViewById<ImageView>(R.id.ivIcon).isSelected = unknownSelected
            viewUnknown.findViewById<TextView>(R.id.tvTitle).isSelected = unknownSelected
        }
    }

    // Fragment 내부에서 사용하는 View 참조 바인딩
    private fun bindViews(root: View) {
        methodList = root.findViewById(R.id.methodList)
        containerAll = root.findViewById(R.id.containerAll)
        containerUnknown = root.findViewById(R.id.containerUnknown)
    }

    // Step2 질문 카드 문구 설정
    private fun bindQuestionCard(root: View) {
        val card = root.findViewById<View>(R.id.includeQuestionCard)

        val tvTitle = card.findViewById<TextView>(R.id.tvQuestionTitle)
        val tvDesc = card.findViewById<TextView>(R.id.tvQuestionDesc)

        tvTitle.setText(R.string.onb_step2_title)
        tvDesc.setText(R.string.onb_step2_desc)
    }

    // 상단 기록 방식 아이템 + divider를 순서대로 생성
    private fun buildMethodItemsWithDividers() {
        val inflater = LayoutInflater.from(requireContext())

        // 재진입 시 중복 생성 방지
        methodList.removeAllViews()
        methodViewMap.clear()

        val methods = RecordMethod.entries

        methods.forEachIndexed { index, method ->
            val item = inflater.inflate(R.layout.item_onb_step2, methodList, false)

            // 아이템 내용 바인딩
            bindStep2Item(item, method.titleResId, method.iconRes)

            // 클릭 시 해당 기록 방식 토글
            item.setOnClickListener { vm.toggleRecordMethod(method) }

            methodViewMap[method] = item
            methodList.addView(item)

            // 마지막 아이템을 제외하고 divider 추가
            if (index != methods.lastIndex) {
                methodList.addView(createDivider())
            }
        }
    }

    // 하단 카드 영역에 단일 아이템을 넣는 구조 생성
    private fun buildOptionItemsInCards() {
        val inflater = LayoutInflater.from(requireContext())

        // "모든 방식을 환영해요" 카드
        containerAll.removeAllViews()
        viewAll = inflater.inflate(R.layout.item_onb_step2, containerAll, false).apply {
            bindStep2Item(this, R.string.onb_step2_record_all, R.drawable.ic_onb_record_all)
            setOnClickListener { vm.toggleAllRecordMethods() }
        }
        containerAll.addView(viewAll)

        // "아직 잘 모르겠어요" 카드
        containerUnknown.removeAllViews()
        viewUnknown = inflater.inflate(R.layout.item_onb_step2, containerUnknown, false).apply {
            bindStep2Item(this, R.string.onb_step2_record_unknown, R.drawable.ic_onb_record_unknown)
            setOnClickListener { vm.toggleStep2Unknown() }
        }
        containerUnknown.addView(viewUnknown)
    }

    // Step2 아이템 공통 바인딩 함수
    private fun bindStep2Item(
        itemView: View,
        @StringRes titleResId: Int,
        @DrawableRes iconRes: Int
    ) {
        itemView.findViewById<TextView>(R.id.tvTitle).setText(titleResId)
        itemView.findViewById<ImageView>(R.id.ivIcon).setImageResource(iconRes)
    }

    // 상단 아이템 사이를 구분하기 위한 divider View 생성
    private fun createDivider(): View {
        return View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            ).apply {
                // 아이템과 너무 붙어 보이지 않도록 여백 추가
                topMargin = dp(8)
                bottomMargin = dp(8)
            }
            setBackgroundResource(R.color.grey_200)
        }
    }

    // dp → px 변환 유틸
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}