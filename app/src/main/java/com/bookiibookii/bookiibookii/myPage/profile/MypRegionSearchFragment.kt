package com.bookiibookii.bookiibookii.myPage.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.bookData.Data.City
import com.bookiibookii.bookiibookii.databinding.FragmentMypRegionSearchBinding

class MypRegionSearchFragment : Fragment() {
    // ... (기본 코드 동일) ...
    private var _binding: FragmentMypRegionSearchBinding? = null
    private val binding get() = _binding!!

    // 현재 선택된 정보 저장
    private var currentCity = ""
    private var currentDistrict = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypRegionSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mockData = listOf(
            City("서울", listOf("강남구", "강동구", "강북구", "강서구")),
            City("경기", listOf("수원시", "성남시", "의정부시"))
            // ... 데이터 추가
        )

        // 오른쪽 어댑터 (구/군)
        val rightAdapter = MypDistrictAdapter { selectedDistrict ->
            currentDistrict = selectedDistrict
            // 선택 시 UI 효과(배경색 등)는 어댑터 내부에서 처리 필요
        }

        // 왼쪽 어댑터 (시/도)
        val leftAdapter = MypCityAdapter(mockData) { city ->
            currentCity = city.name
            currentDistrict = "" // 시가 바뀌면 구 초기화
            rightAdapter.submitList(city.districts)
        }

        binding.rvLeftCity.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLeftCity.adapter = leftAdapter

        binding.rvRightDistrict.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvRightDistrict.adapter = rightAdapter

        // 초기값
        if (mockData.isNotEmpty()) {
            currentCity = mockData[0].name
            rightAdapter.submitList(mockData[0].districts)
        }

        binding.mypSearchCloseIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // [완료 버튼] 선택 결과 반환
        binding.mypSearchSearchBtn.setOnClickListener {
            if(currentDistrict.isNotEmpty()) {
                val result = "$currentCity $currentDistrict"
                // 결과 전달
                setFragmentResult("requestKeyRegion", bundleOf("regionResult" to result))
                parentFragmentManager.popBackStack()
            } else {
                // 구/군을 선택하지 않았을 때 처리
            }
        }
    }
    // ... onDestroyView, onResume(하단바 숨김) ...
}