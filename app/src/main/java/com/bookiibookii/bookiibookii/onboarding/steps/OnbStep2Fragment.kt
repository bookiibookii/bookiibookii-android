package com.bookiibookii.bookiibookii.onboarding.steps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.onboarding.steps.data.RecordMethod

class OnbStep2Fragment : Fragment(R.layout.fragment_onb_step2) {

    private val vm: OnbViewModel by activityViewModels()

    // 상단 4개가 들어갈 리스트(구분선 포함)
    private lateinit var methodList: LinearLayout

    // 하단 2개 카드 내부 컨테이너
    private lateinit var containerAll: ViewGroup
    private lateinit var containerUnknown: ViewGroup

    // RecordMethod -> itemView 매핑 (선택 상태 반영용)
    private val methodViewMap = linkedMapOf<RecordMethod, View>()

    private lateinit var viewAll: View
    private lateinit var viewUnknown: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindQuestionCard(view)
        bindViews(view)

        buildMethodItemsWithDividers()
        buildOptionItemsInCards()

        // 상태 관찰 → 선택 UI 자동 갱신
        vm.state.observe(viewLifecycleOwner) { state ->
            methodViewMap.forEach { (method, itemView) ->
                val selected = state.recordMethods.contains(method)

                itemView.isSelected = selected
                itemView.findViewById<ImageView>(R.id.ivIcon).isSelected = selected
                itemView.findViewById<TextView>(R.id.tvTitle).isSelected = selected
            }

            val allSelected = vm.isStep2AllSelected()
            viewAll.isSelected = allSelected
            viewAll.findViewById<ImageView>(R.id.ivIcon).isSelected = allSelected
            viewAll.findViewById<TextView>(R.id.tvTitle).isSelected = allSelected

            val unknownSelected = vm.isStep2Unknown()
            viewUnknown.isSelected = unknownSelected
            viewUnknown.findViewById<ImageView>(R.id.ivIcon).isSelected = unknownSelected
            viewUnknown.findViewById<TextView>(R.id.tvTitle).isSelected = unknownSelected
        }
    }

    private fun bindViews(root: View) {
        methodList = root.findViewById(R.id.methodList)
        containerAll = root.findViewById(R.id.containerAll)
        containerUnknown = root.findViewById(R.id.containerUnknown)
    }

    private fun bindQuestionCard(root: View) {
        val card = root.findViewById<View>(R.id.includeQuestionCard)

        val tvTitle = card.findViewById<TextView>(R.id.tvQuestionTitle)
        val tvDesc = card.findViewById<TextView>(R.id.tvQuestionDesc)

        tvTitle.text = "마음에 콕! 박히는\n문장을 만났을 때"
        tvDesc.text = "나의 평소 독서 습관은 어떤가요?\n(중복 선택 가능)"
    }

    // 상단 4개 + divider 생성
    private fun buildMethodItemsWithDividers() {
        val inflater = LayoutInflater.from(requireContext())

        methodList.removeAllViews()
        methodViewMap.clear()

        val methods = RecordMethod.entries

        methods.forEachIndexed { index, method ->
            val item = inflater.inflate(R.layout.item_onb_step2, methodList, false)
            bindStep2Item(item, method.displayName, method.iconRes)

            item.setOnClickListener { vm.toggleRecordMethod(method) }

            methodViewMap[method] = item
            methodList.addView(item)

            // 마지막 아이템 제외하고 divider 추가
            if (index != methods.lastIndex) {
                methodList.addView(createDivider())
            }
        }
    }

    // 하단 2개를 "카드 배경 안에 item 1개"로 넣기
    private fun buildOptionItemsInCards() {
        val inflater = LayoutInflater.from(requireContext())

        containerAll.removeAllViews()
        viewAll = inflater.inflate(R.layout.item_onb_step2, containerAll, false).apply {
            bindStep2Item(this, "모든 방식을 환영해요", R.drawable.ic_onb_record_all)
            setOnClickListener { vm.toggleAllRecordMethods() }
        }
        containerAll.addView(viewAll)

        containerUnknown.removeAllViews()
        viewUnknown = inflater.inflate(R.layout.item_onb_step2, containerUnknown, false).apply {
            bindStep2Item(this, "아직 잘 모르겠어요", R.drawable.ic_onb_record_unknown)
            setOnClickListener { vm.toggleStep2Unknown() }
        }
        containerUnknown.addView(viewUnknown)
    }

    private fun bindStep2Item(itemView: View, title: String, iconRes: Int) {
        itemView.findViewById<TextView>(R.id.tvTitle).text = title
        itemView.findViewById<ImageView>(R.id.ivIcon).setImageResource(iconRes)
    }

    // divider (1dp 라인)
    private fun createDivider(): View {
        return View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            ).apply {
                // ✅ divider 위/아래 살짝 여백 주면 더 안 붙어 보임
                topMargin = dp(8)
                bottomMargin = dp(8)
            }
            setBackgroundResource(R.color.grey_200) // 너 프로젝트 색 토큰으로 맞추기
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}