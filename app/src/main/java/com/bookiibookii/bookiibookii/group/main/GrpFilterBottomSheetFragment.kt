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

// 필터 종류 구분용 Enum
enum class FilterType {
    GROUP_TYPE,
    CATEGORY
}

class FilterBottomSheetFragment(
    private val filterType: FilterType,
    private val preSelectedList: List<String>,
    private val onConfirm: (List<String>) -> Unit,
    private val onDismissAction: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentGrpBottomSheetBinding? = null
    private val binding get() = _binding!!

    // 현재 선택된 필터들을 담을 리스트
    private val selectedList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrpBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        setupUI()
        initListener()
    }

    private fun initData() {
        selectedList.clear()
        // 이전 선택값이 없거나 '전체'가 포함되어 있으면 전체 선택
        if (preSelectedList.isEmpty() || preSelectedList.contains("전체")) {
            binding.grpBottomSheetEntiretyCp.isChecked = true
        } else {
            selectedList.addAll(preSelectedList)
            binding.grpBottomSheetEntiretyCp.isChecked = false
        }
    }

    private fun setupUI() {
        val inflater = LayoutInflater.from(requireContext())

        // 타입에 따라 제목과 칩 데이터 리스트 결정
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

        // 칩 동적 생성 및 추가
        for (dataText in chipDataList) {
            val chip = inflater.inflate(R.layout.item_filter_chip, binding.grpBottomSheetChipGroup, false) as Chip
            chip.text = dataText

            // 이전에 선택된 항목이라면 체크 상태로 표시
            chip.isChecked = selectedList.contains(dataText)

            // 칩 클릭 리스너
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedList.add(dataText)
                    // 개별 칩을 선택하면 '전체' 칩은 해제
                    binding.grpBottomSheetEntiretyCp.isChecked = false
                } else {
                    selectedList.remove(dataText)
                    // 모든 칩이 해제되면 자동으로 '전체' 선택
                    if (selectedList.isEmpty()) {
                        binding.grpBottomSheetEntiretyCp.isChecked = true
                    }
                }
                updateSummaryText() // 상단 요약 텍스트 갱신
            }

            // 칩 추가
            binding.grpBottomSheetChipGroup.addView(chip)


            if (filterType == FilterType.GROUP_TYPE && dataText == "함께 읽기") {
                addDynamicDivider()
            }
        }

        // 초기 상태에 맞춰 요약 텍스트 업데이트
        updateSummaryText()
    }

    // 동적으로 구분선(ImageView)을 생성하여 추가하는 함수
    private fun addDynamicDivider() {
        val divider = ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_vector_86) // 구분선 이미지
            scaleType = ImageView.ScaleType.FIT_XY

            // XML의 layout_height="32dp"와 동일하게 설정
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dpToPx(38)
            )
        }
        binding.grpBottomSheetChipGroup.addView(divider)
    }

    // ★ dp를 px로 변환하는 헬퍼 함수
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun initListener() {
        with(binding) {
            // '전체' 칩 클릭 로직
            grpBottomSheetEntiretyCp.setOnClickListener {
                if (grpBottomSheetEntiretyCp.isChecked) {
                    // 전체가 선택되면 나머지 칩 모두 해제 및 리스트 비우기
                    selectedList.clear()
                    clearAllChipsExcludeEntirety()
                    updateSummaryText()
                } else {
                    // 전체를 끄려고 할 때, 다른 선택된 게 없다면 못 끄게 막음 (강제 체크)
                    if (selectedList.isEmpty()) grpBottomSheetEntiretyCp.isChecked = true
                }
            }

            // 취소 버튼
            grpBottomSheetCancelBtn.setOnClickListener { dismiss() }

            // 확인 버튼
            grpBottomSheetEnterBtn.setOnClickListener {
                val result = if (selectedList.isEmpty()) listOf("전체") else selectedList
                onConfirm(result)
                dismiss()
            }
        }
    }

    // '전체' 칩과 '구분선'을 제외한 나머지 동적 칩들의 체크 해제
    private fun clearAllChipsExcludeEntirety() {
        val count = binding.grpBottomSheetChipGroup.childCount
        for (i in 0 until count) {
            val view = binding.grpBottomSheetChipGroup.getChildAt(i)
            // Chip 객체이면서 ID가 '전체'가 아닌 것만 체크 해제
            if (view is Chip && view.id != R.id.grp_bottom_sheet_entirety_Cp) {
                view.isChecked = false
            }
        }
    }

    // 상단 요약 텍스트 (과학/IT · 자기계발 외 3개) 업데이트 로직
    private fun updateSummaryText() {
        with(binding) {
            // 모든 텍스트 뷰 일단 숨김 (초기화)
            grpBottomSheetSelect1Tv.visibility = View.GONE
            grpBottomSheetDot1Tv.visibility = View.GONE
            grpBottomSheetSelect2Tv.visibility = View.GONE
            grpBottomSheetDot2Tv.visibility = View.GONE
            grpBottomSheetSelect3Tv.visibility = View.GONE
            grpBottomSheetExceptTitleTv.visibility = View.GONE
            grpBottomSheetExceptNumTv.visibility = View.GONE
            grpBottomSheetExceptCountTv.visibility = View.GONE

            // '전체'인 경우
            if (selectedList.isEmpty() || grpBottomSheetEntiretyCp.isChecked) {
                grpBottomSheetSelect1Tv.text = "전체"
                grpBottomSheetSelect1Tv.visibility = View.VISIBLE
                return
            }

            // 선택된 개수에 따라 Visibility 켜기
            // 첫 번째 아이템
            if (selectedList.isNotEmpty()) {
                grpBottomSheetSelect1Tv.text = selectedList[0]
                grpBottomSheetSelect1Tv.visibility = View.VISIBLE
            }

            // 두 번째 아이템
            if (selectedList.size >= 2) {
                grpBottomSheetDot1Tv.visibility = View.VISIBLE
                grpBottomSheetSelect2Tv.text = selectedList[1]
                grpBottomSheetSelect2Tv.visibility = View.VISIBLE
            }

            // 세 번째 아이템
            if (selectedList.size >= 3) {
                grpBottomSheetDot2Tv.visibility = View.VISIBLE
                grpBottomSheetSelect3Tv.text = selectedList[2]
                grpBottomSheetSelect3Tv.visibility = View.VISIBLE
            }

            // 4개 이상일 때 ("외 N개")
            if (selectedList.size > 3) {
                grpBottomSheetExceptTitleTv.visibility = View.VISIBLE
                grpBottomSheetExceptNumTv.text = "${selectedList.size - 3}"
                grpBottomSheetExceptNumTv.visibility = View.VISIBLE
                grpBottomSheetExceptCountTv.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissAction()
    }
}