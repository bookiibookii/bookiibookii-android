package com.bookiibookii.bookiibookii.myPage.report

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.ReportRequest
import com.bookiibookii.bookiibookii.databinding.FragmentMypReportWriteBinding // XML 이름 확인 필요
import kotlinx.coroutines.launch

class MypReportWriteFragment : Fragment() {
    private var _binding: FragmentMypReportWriteBinding? = null // 제공된 XML 파일 이름에 맞게 수정
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // XML 이름이 fragment_myp_report_write.xml 이라면 -> FragmentMypReportWriteBinding
        // 제공해주신 XML 파일 내용을 바탕으로 추론했습니다.
        _binding = FragmentMypReportWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mypReportBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.mypWriteBtn.setOnClickListener {
            val groupName = binding.mypReportGroupEt.text.toString().trim()
            val memberName = binding.mypReportMemberEt.text.toString().trim()
            val content = binding.mypReportContentEt.text.toString().trim()

            // 라디오 버튼 선택 확인 및 타입 변환
            val reportType = getSelectedReportType()

            if (groupName.isEmpty() || memberName.isEmpty() || content.isEmpty()) {
                Toast.makeText(context, "모든 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (reportType == null) {
                Toast.makeText(context, "신고 유형을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendReport(groupName, memberName, reportType, content)
        }
    }

    private fun getSelectedReportType(): String? {
        return when (binding.mypReportTypeRg.checkedRadioButtonId) {
            binding.mypReportType1Rb.id -> "ABUSE"  // 욕설/비방
            binding.mypReportType2Rb.id -> "SPAM"   // 스팸/광고
            binding.mypReportType3Rb.id -> "NOSHOW" // 미발송/노쇼
            binding.mypReportType4Rb.id -> "DAMAGE" // 파손/낙서
            binding.mypReportType5Rb.id -> "OTHER"  // 기타(텍스트)
            else -> null
        }
    }

    private fun sendReport(group: String, member: String, type: String, content: String) {
        lifecycleScope.launch {
            try {
                val request = ReportRequest(group, member, type, content)
                val response = RetrofitClient.getInstance(requireContext()).postReport(request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(context, "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Log.e("ReportWrite", "전송 실패: ${response.code()}")
                    Toast.makeText(context, "신고 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ReportWrite", "네트워크 오류", e)
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}