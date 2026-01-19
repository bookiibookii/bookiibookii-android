package com.bookiibookii.bookiibookii.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentGrpBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip

class FilterBottomSheetFragment(val itemClick: (String) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentGrpBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGrpBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 카테고리 목록 데이터
        val categories = listOf(
            "경제/경영", "기술/과학", "소설",
            "시", "에세이", "예술/문화",
            "인문학", "자기계발", "정치/사회"
        )

        // 2. 칩 동적 생성 및 추가
        categories.forEach { category ->
            val chip = Chip(context).apply {
                text = category
                isCheckable = true

                // 스타일 적용 (아까 만든 컬러 파일 적용)
                chipBackgroundColor = resources.getColorStateList(R.color.selector_chip_category_bg, null)
                chipStrokeColor = resources.getColorStateList(R.color.selector_chip_category_stroke, null)
                chipStrokeWidth = dpToPx(1).toFloat()
                setTextColor(resources.getColorStateList(R.color.selector_chip_category_text, null))

                // 크기 및 폰트 설정
                textSize = 14f
                setEnsureMinTouchTargetSize(false) // 칩 위아래 여백 최소화
                chipCornerRadius = dpToPx(30).toFloat()

                // 칩 클릭 시 동작
                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        // 선택된 값을 GroupFragment로 전달하고 창 닫기
                        itemClick(category)
                        dismiss()
                    }
                }
            }
//            binding.chipGroupCategory.addView(chip)
        }
    }

    // dp -> px 변환 함수
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}