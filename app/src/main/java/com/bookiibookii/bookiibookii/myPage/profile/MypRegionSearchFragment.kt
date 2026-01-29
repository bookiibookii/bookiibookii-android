package com.bookiibookii.bookiibookii.myPage.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.bookData.Data.City
import com.bookiibookii.bookiibookii.databinding.FragmentMypRegionSearchBinding

class MypRegionSearchFragment : Fragment() {

    private var _binding: FragmentMypRegionSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypRegionSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 데이터 준비 (이전과 동일)
        val mockData = listOf(
            City("서울", listOf("강남구", "강동구", "강북구", "강서구", "관악구")),
            City("경기", listOf("수원시", "성남시", "의정부시", "안양시", "부천시")),
            City("인천", listOf("전체", "계양구", "남동구", "동구", "미추홀구", "부평구", "서구", "연수구", "옹진군", "중구")),
            City("강원", listOf("춘천시", "원주시", "강릉시"))
            
        )

        // 2. 어댑터 생성
        val rightAdapter = MypDistrictAdapter()
        val leftAdapter = MypCityAdapter(mockData) { selectedCity ->
            rightAdapter.submitList(selectedCity.districts)
        }

        // 3. RecyclerView 설정
        binding.rvLeftCity.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = leftAdapter
        }

        binding.rvRightDistrict.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = rightAdapter
        }

        // 4. 초기값 설정
        if (mockData.isNotEmpty()) {
            rightAdapter.submitList(mockData[0].districts)
        }

        // 5. 버튼 이벤트 처리
        binding.mypSearchCloseIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.mypSearchSearchBtn.setOnClickListener {
            // 완료 로직
            Toast.makeText(requireContext(), "설정 완료", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}