package com.bookiibookii.bookiibookii.myPage.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentMypReportDetailBinding

class MypReportDetailFragment : Fragment() {

    private var _binding: FragmentMypReportDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 전달받은 데이터 꺼내기
        val groupName = arguments?.getString("groupName") ?: ""
        val reportType = arguments?.getString("reportType") ?: ""
        val content = arguments?.getString("content") ?: ""

        // 2. 데이터 세팅
        binding.mypReportGroupEt.setText(groupName)
        binding.mypReportContentEt.setText(content)

        // 3. 디테일 화면이므로 사용자가 수정할 수 없도록 EditText 잠금 처리
        binding.mypReportGroupEt.isFocusable = false
        binding.mypReportGroupEt.isFocusableInTouchMode = false
        binding.mypReportContentEt.isFocusable = false
        binding.mypReportContentEt.isFocusableInTouchMode = false

        // 4. 신고 유형(라디오 버튼) UI 업데이트
        updateRadioButtons(reportType)

        // 5. 뒤로 가기
        binding.mypReportDetailBackIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    // 서버에서 받은 타입에 맞춰서 선택된 라디오 버튼에만 주황색 테두리를 칠해주는 함수
    private fun updateRadioButtons(type: String) {
        val rbs = listOf(
            binding.mypReportType1Rb, binding.mypReportType2Rb,
            binding.mypReportType3Rb, binding.mypReportType4Rb, binding.mypReportType5Rb
        )

        // 전부 기본 회색(선택 해제) 상태로 초기화 및 클릭 방지
        rbs.forEach {
            it.setBackgroundResource(R.drawable.bg_input_round_20dp_white)
            it.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_gray, 0, 0, 0)
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_500))
            it.isClickable = false // 클릭 방지
        }

        // 타입에 맞는 버튼 하나만 주황색으로 활성화
        val selectedRb = when (type) {
            "ABUSE" -> binding.mypReportType1Rb
            "SPAM" -> binding.mypReportType2Rb
            "NOSHOW" -> binding.mypReportType3Rb
            "DAMAGE" -> binding.mypReportType4Rb
            else -> binding.mypReportType5Rb // OTHER (기타 텍스트)
        }

        selectedRb.setBackgroundResource(R.drawable.bg_round_20dp_orange_stroke)
        selectedRb.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_orange, 0, 0, 0)
        selectedRb.setTextColor(ContextCompat.getColor(requireContext(), R.color.pre_main))
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.VISIBLE
        _binding = null
    }
}