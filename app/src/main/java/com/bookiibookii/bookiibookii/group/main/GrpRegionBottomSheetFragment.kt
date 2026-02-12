package com.bookiibookii.bookiibookii.group.main

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.City
import com.bookiibookii.bookiibookii.databinding.FragmentGrpBottomSheetRegionBinding
import com.bookiibookii.bookiibookii.myPage.profile.MypCityAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip

class GrpRegionBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentGrpBottomSheetRegionBinding? = null
    private val binding get() = _binding!!

    // [Properties] 데이터 관리
    private var currentCity = "서울"
    private var selectedDistricts = mutableListOf<String>()

    private val allCityData = getMockCityData()
    private lateinit var leftAdapter: MypCityAdapter
    // endregion

    companion object {
        const val ARG_PRE_SELECTED = "pre_selected_region"

        fun newInstance(preSelected: String): GrpRegionBottomSheetFragment {
            return GrpRegionBottomSheetFragment().apply {
                arguments = bundleOf(ARG_PRE_SELECTED to preSelected)
            }
        }
    }

    // [Lifecycle & Dialog Setup]
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGrpBottomSheetRegionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                // 바텀시트 높이를 전체 화면의 40%로 고정
                sheet.layoutParams.height = (resources.displayMetrics.heightPixels * 0.4).toInt()
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true // 절반 접힘 방지
            }
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parseArguments()      // 기존 선택값 복구
        initLeftCityList()    // 좌측 시/도 리스트 초기화
        initRightDistricts()  // 우측 구/군 칩 초기화
        initListeners()       // 버튼 리스너
        updateSummaryUI()     // 상단 요약 바 업데이트

        // UX: 칩 그룹 내부 스크롤 시 바텀시트가 닫히지 않도록 부모의 터치 간섭을 막음
        binding.grpBottomSheetRegionChipGroup.setOnTouchListener { v, _ ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }


    // 데이터 파싱 및 초기화

    private fun parseArguments() {
        val preSelected = arguments?.getString(ARG_PRE_SELECTED) ?: "전체"
        if (preSelected == "전체" || preSelected.isEmpty()) return

        val split = preSelected.split(" ")
        if (split.isNotEmpty()) {
            currentCity = split[0]
            if (split.size > 1 && split[1] != "전체") {
                val districts = split[1].split("/")
                districts.forEach { if (it != "전체") selectedDistricts.add(it) }
            }
        }
    }

    private fun initLeftCityList() {
        val initialIndex = allCityData.indexOfFirst { it.name == currentCity }.coerceAtLeast(0)

        leftAdapter = MypCityAdapter(allCityData) { city ->
            if (currentCity != city.name) {
                currentCity = city.name
                selectedDistricts.clear() // 도시 변경 시 선택된 구 초기화
                updateSummaryUI()
                updateDistrictChips(city.districts)
                binding.grpBottomSheetScrollView.scrollTo(0, 0)
            }
        }
        leftAdapter.selectedPosition = initialIndex

        binding.grpBottomSheetRegionCityRv.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                scrollToPositionWithOffset(initialIndex, 0)
            }
            adapter = leftAdapter
        }
    }

    private fun initRightDistricts() {
        val targetCityData = allCityData.find { it.name == currentCity } ?: allCityData[0]
        updateDistrictChips(targetCityData.districts)
    }

    // 구/군 칩 생성 및 상태 제어

    private fun updateDistrictChips(districts: List<String>) {
        binding.grpBottomSheetRegionChipGroup.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        districts.forEach { districtName ->
            val chip = inflater.inflate(R.layout.item_filter_chip, binding.grpBottomSheetRegionChipGroup, false) as Chip
            chip.text = districtName

            // 초기 체크 상태 설정
            chip.isChecked = if (districtName == "전체") selectedDistricts.isEmpty()
            else selectedDistricts.contains(districtName)

            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!buttonView.isPressed) return@setOnCheckedChangeListener // 프로그래밍적 변경은 무시

                if (districtName == "전체") {
                    handleEntireChipClick(isChecked, buttonView as Chip)
                } else {
                    handleDistrictChipClick(isChecked, districtName, buttonView as Chip)
                }
                updateSummaryUI()
            }
            binding.grpBottomSheetRegionChipGroup.addView(chip)
        }
    }

    private fun handleEntireChipClick(isChecked: Boolean, chip: Chip) {
        if (isChecked) {
            selectedDistricts.clear()
            val group = binding.grpBottomSheetRegionChipGroup
            for (i in 0 until group.childCount) {
                val child = group.getChildAt(i) as? Chip
                if (child != chip) child?.isChecked = false
            }
        } else if (selectedDistricts.isEmpty()) {
            chip.isChecked = true // 최소 하나(전체)는 선택되어야 함
        }
    }

    private fun handleDistrictChipClick(isChecked: Boolean, name: String, chip: Chip) {
        if (isChecked) {
            if (selectedDistricts.size >= 3) {
                chip.isChecked = false
                Toast.makeText(requireContext(), "최대 3개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                selectedDistricts.add(name)
                toggleEntireChip(false)
            }
        } else {
            selectedDistricts.remove(name)
            if (selectedDistricts.isEmpty()) toggleEntireChip(true)
        }
    }

    private fun toggleEntireChip(isCheck: Boolean) {
        val group = binding.grpBottomSheetRegionChipGroup
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i) as? Chip
            if (child?.text == "전체") {
                child.isChecked = isCheck
                break
            }
        }
    }

    private fun updateSummaryUI() {
        with(binding) {
            val views = listOf(grpBottomSheetRegionSelect1Tv, grpBottomSheetRegionDot1Tv,
                grpBottomSheetRegionSelect2Tv, grpBottomSheetRegionDot2Tv,
                grpBottomSheetRegionSelect3Tv)
            views.forEach { it.visibility = View.GONE }

            if (selectedDistricts.isEmpty()) {
                grpBottomSheetRegionSelect1Tv.apply { text = "전체"; visibility = View.VISIBLE }
                return
            }

            selectedDistricts.forEachIndexed { index, name ->
                when (index) {
                    0 -> { grpBottomSheetRegionSelect1Tv.text = name; grpBottomSheetRegionSelect1Tv.visibility = View.VISIBLE }
                    1 -> { grpBottomSheetRegionDot1Tv.visibility = View.VISIBLE; grpBottomSheetRegionSelect2Tv.text = name; grpBottomSheetRegionSelect2Tv.visibility = View.VISIBLE }
                    2 -> { grpBottomSheetRegionDot2Tv.visibility = View.VISIBLE; grpBottomSheetRegionSelect3Tv.text = name; grpBottomSheetRegionSelect3Tv.visibility = View.VISIBLE }
                }
            }
        }
    }


    private fun initListeners() {
        binding.grpBottomSheetCancelBtn.setOnClickListener { dismiss() }

        binding.grpBottomSheetEnterBtn.setOnClickListener {
            val resultString = if (selectedDistricts.isNotEmpty()) {
                "$currentCity ${selectedDistricts.joinToString("/")}"
            } else {
                "$currentCity 전체"
            }
            setFragmentResult("requestKeyRegion", bundleOf("regionResult" to resultString))
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // [Mock Data] 전국 시/도 데이터
    private fun getMockCityData(): List<City> {
        return listOf(
            City("서울", listOf("전체", "강남구", "강동구", "강북구", "강서구", "관악구", "광진구", "구로구", "금천구", "노원구", "도봉구", "동대문구", "동작구", "마포구", "서대문구", "서초구", "성동구", "성북구", "송파구", "양천구", "영등포구", "용산구", "은평구", "종로구", "중구", "중랑구")),
            City("경기", listOf("전체", "수원시", "성남시", "의정부시", "안양시", "부천시", "광명시", "평택시", "동두천시", "안산시", "고양시", "과천시", "구리시", "남양주시", "오산시", "시흥시", "군포시", "의왕시", "하남시", "용인시", "파주시", "이천시", "안성시", "김포시", "화성시", "광주시", "양주시", "포천시", "여주시", "연천군", "가평군", "양평군")),
            City("인천", listOf("전체","계양구", "미추홀구", "남동구", "동구", "부평구", "서구", "연수구", "중구", "강화군·옹진군")),
            City("대전", listOf("전체","대덕구", "동구", "서구", "유성구", "중구")),
            City("대구", listOf("전체","남구", "달서구", "동구", "북구", "서구", "수성구", "중구", "달성군", "군위군")),
            City("광주", listOf("전체","광산구", "남구", "동구", "북구", "서구")),
            City("울산", listOf("전체","남구", "동구", "북구", "중구", "울주군")),
            City("부산", listOf("전체","강서구", "금정구", "남구", "동구", "동래구", "부산진구", "북구", "사상구", "사하구", "서구", "수영구", "연제구", "영도구", "중구", "해운대구", "기장군")),
            City("세종", listOf("전체","세종특별자치시")),
            City("강원", listOf("전체","춘천시", "원주시", "강릉시", "동해시", "태백시", "속초시", "삼척시", "홍천군", "횡성군", "영월군", "평창군", "정선군", "철원군", "화천군", "양구군", "인제군", "고성군", "양양군")),
            City("충북", listOf("전체","청주시", "충주시", "제천시", "보은군", "옥천군", "영동군", "증평군", "진천군", "괴산군", "음성군", "단양군")),
            City("충남", listOf("전체","천안시", "공주시", "보령시", "아산시", "서산시", "논산시", "계룡시", "당진시", "금산군", "부여군", "서천군", "청양군", "홍성군", "예산군", "태안군")),
            City("전북", listOf("전체","전주시", "군산시", "익산시", "정읍시", "남원시", "김제시", "완주군", "진안군", "무주군", "장수군", "임실군", "순창군", "고창군", "부안군")),
            City("전남", listOf("전체","목포시", "여수시", "순천시", "나주시", "광양시", "담양군", "곡성군", "구례군", "고흥군", "보성군", "화순군", "장흥군", "강진군", "해남군", "영암군", "무안군", "함평군", "영광군", "장성군", "완도군", "진도군", "신안군")),
            City("경북", listOf("전체","포항시", "경주시", "김천시", "안동시", "구미시", "영주시", "영천시", "상주시", "문경시", "경산시", "의성군", "청송군", "영양군", "영덕군", "청도군", "고령군", "성주군", "칠곡군", "예천군", "봉화군", "울진군", "울릉군")),
            City("경남", listOf("전체","창원시", "진주시", "통영시", "사천시", "김해시", "밀양시", "거제시", "양산시", "의령군", "함안군", "창녕군", "고성군", "남해군", "하동군", "산청군", "함양군", "거창군", "합천군")),
            City("제주", listOf("전체","제주시", "서귀포시"))
        )
    }
}