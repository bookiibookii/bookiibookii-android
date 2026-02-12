package com.bookiibookii.bookiibookii.group.main

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentGrpBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip


//필터 종류를 구분하기 위한 Enum 클래스

enum class FilterType {
    GROUP_TYPE, // 그룹 유형 (함께 읽기, 교환 등)
    CATEGORY    // 도서 분야 (경제, 소설 등)
}

class FilterBottomSheetFragment(
    private val filterType: FilterType,
    private val preSelectedList: List<String>,    // 기존에 선택되어 있던 값
    private val onConfirm: (List<String>) -> Unit, // 확인 버튼 클릭 콜백
    private val onDismissAction: () -> Unit        // 닫힐 때 실행할 액션
) : BottomSheetDialogFragment() {

    private var _binding: FragmentGrpBottomSheetBinding? = null
    private val binding get() = _binding!!

    // 현재 바텀시트 내에서 실시간으로 선택된 필터 리스트
    private val selectedList = mutableListOf<String>()

    // [Lifecycle] 생명주기
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrpBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()      // 전달받은 데이터 세팅
        setupUI()       // 칩 동적 생성 및 타이틀 설정
        initListener()  // 버튼 및 전체 선택 리스너 설정
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissAction() // 칩의 UI 상태 복구 등을 위해 콜백 호출
    }

    // [Initialization] 초기 데이터 및 UI 설정

    private fun initData() {
        selectedList.clear()
        if (preSelectedList.isEmpty() || preSelectedList.contains("전체")) {
            binding.grpBottomSheetEntiretyCp.isChecked = true
        } else {
            selectedList.addAll(preSelectedList)
            binding.grpBottomSheetEntiretyCp.isChecked = false
        }
    }

    private fun setupUI() {
        val inflater = LayoutInflater.from(requireContext())

        // 1. 타입별 타이틀 및 데이터 정의 (필터 추가 시 여기를 수정)
        val chipDataList = when (filterType) {
            FilterType.GROUP_TYPE -> {
                binding.grpBottomSheetTitleTv.text = "그룹 유형"
                listOf("함께 읽기", "택배 교환", "직접 교환")
            }
            FilterType.CATEGORY -> {
                binding.grpBottomSheetTitleTv.text = "분야별"
                listOf(
                    "경제/경영", "과학/IT", "소설/장르", "시/에세이",
                    "가정/취미", "예술/문화", "인문/역사", "자기계발", "정치/사회"
                )
            }
        }

        // 2. 정의된 리스트를 바탕으로 칩 생성
        for (dataText in chipDataList) {
            val chip = inflater.inflate(R.layout.item_filter_chip, binding.grpBottomSheetChipGroup, false) as Chip
            chip.text = dataText
            chip.isChecked = selectedList.contains(dataText)

            // 개별 칩 인터랙션: 선택 시 '전체' 해제 / 미선택 시 리스트에서 제거
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedList.add(dataText)
                    binding.grpBottomSheetEntiretyCp.isChecked = false
                } else {
                    selectedList.remove(dataText)
                    if (selectedList.isEmpty()) {
                        binding.grpBottomSheetEntiretyCp.isChecked = true
                    }
                }
                updateSummaryText() // 실시간 상단 텍스트 갱신
            }

            binding.grpBottomSheetChipGroup.addView(chip)

            // '함께 읽기' 뒤에 구분선이 필요한 디자인 대응
            if (filterType == FilterType.GROUP_TYPE && dataText == "함께 읽기") {
                addDynamicDivider()
            }
        }

        updateSummaryText()
    }

    // [Interaction] 버튼 및 칩 리스너
    private fun initListener() {
        with(binding) {
            // '전체' 칩 클릭 로직
            grpBottomSheetEntiretyCp.setOnClickListener {
                if (grpBottomSheetEntiretyCp.isChecked) {
                    selectedList.clear()
                    clearAllChipsExcludeEntirety()
                    updateSummaryText()
                } else {
                    // 아무것도 선택 안 된 상태에서 '전체'를 끌 수 없도록 강제 유지
                    if (selectedList.isEmpty()) grpBottomSheetEntiretyCp.isChecked = true
                }
            }

            grpBottomSheetCancelBtn.setOnClickListener { dismiss() }

            grpBottomSheetEnterBtn.setOnClickListener {
                val result = if (selectedList.isEmpty()) listOf("전체") else selectedList
                onConfirm(result)
                dismiss()
            }
        }
    }

    private fun clearAllChipsExcludeEntirety() {
        val group = binding.grpBottomSheetChipGroup
        for (i in 0 until group.childCount) {
            val view = group.getChildAt(i)
            if (view is Chip && view.id != R.id.grp_bottom_sheet_entirety_Cp) {
                view.isChecked = false
            }
        }
    }


    //  [UI Update] 요약 텍스트 제어 -> 상단에 현재 선택된 항목들을 요약해서 보여줍니다. (예: 경제/경영 · 과학/IT 외 2개)

    private fun updateSummaryText() {
        with(binding) {
            // 모든 텍스트 초기화
            val summaryViews = listOf(
                grpBottomSheetSelect1Tv, grpBottomSheetDot1Tv,
                grpBottomSheetSelect2Tv, grpBottomSheetDot2Tv,
                grpBottomSheetSelect3Tv, grpBottomSheetExceptTitleTv,
                grpBottomSheetExceptNumTv, grpBottomSheetExceptCountTv
            )
            summaryViews.forEach { it.visibility = View.GONE }

            // '전체' 선택 시 처리
            if (selectedList.isEmpty() || grpBottomSheetEntiretyCp.isChecked) {
                grpBottomSheetSelect1Tv.apply {
                    text = "전체"
                    visibility = View.VISIBLE
                }
                return
            }

            // 선택 개수에 따른 순차 노출 (최대 3개 노출 후 '외 N개' 처리)
            if (selectedList.size >= 1) {
                grpBottomSheetSelect1Tv.text = selectedList[0]
                grpBottomSheetSelect1Tv.visibility = View.VISIBLE
            }
            if (selectedList.size >= 2) {
                grpBottomSheetDot1Tv.visibility = View.VISIBLE
                grpBottomSheetSelect2Tv.text = selectedList[1]
                grpBottomSheetSelect2Tv.visibility = View.VISIBLE
            }
            if (selectedList.size >= 3) {
                grpBottomSheetDot2Tv.visibility = View.VISIBLE
                grpBottomSheetSelect3Tv.text = selectedList[2]
                grpBottomSheetSelect3Tv.visibility = View.VISIBLE
            }
            if (selectedList.size > 3) {
                grpBottomSheetExceptTitleTv.visibility = View.VISIBLE
                grpBottomSheetExceptNumTv.text = "${selectedList.size - 3}"
                grpBottomSheetExceptNumTv.visibility = View.VISIBLE
                grpBottomSheetExceptCountTv.visibility = View.VISIBLE
            }
        }
    }


    //  [Helpers] 유틸리티

    private fun addDynamicDivider() {
        val divider = ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_vector_86)
            scaleType = ImageView.ScaleType.FIT_XY
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dpToPx(38)
            )
        }
        binding.grpBottomSheetChipGroup.addView(divider)
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

}