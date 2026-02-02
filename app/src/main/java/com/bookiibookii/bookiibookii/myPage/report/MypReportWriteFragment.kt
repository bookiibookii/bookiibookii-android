package com.bookiibookii.bookiibookii.myPage.report

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.MypReport
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.databinding.FragmentMypReportWriteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MypReportWriteFragment : Fragment() {
    private var _binding: FragmentMypReportWriteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyPageViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypReportWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mypReportBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 1. 그룹 선택 (+ 버튼 클릭 시 팝업)
        binding.mypReportGroupPlusIv.setOnClickListener {
            showGroupPopup(it)
        }

        // 2. 멤버 선택 (+ 버튼 클릭 시 팝업)
        binding.mypReportMemberPlusIv.setOnClickListener {
            showMemberPopup(it)
        }

        // 3. 신고 전송 버튼
        binding.mypWriteBtn.setOnClickListener {
            val group = binding.mypReportGroupEt.text.toString()
            val member = binding.mypReportMemberEt.text.toString()
            val content = binding.mypReportContentEt.text.toString()

            val typeId = binding.mypReportTypeRg.checkedRadioButtonId
            val typeText = if(typeId != -1) "신고 유형 선택됨" else ""

            if (group.isNotEmpty() && member.isNotEmpty() && content.isNotEmpty() && typeText.isNotEmpty()) {
                val today = SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault()).format(Date())

                viewModel.addReport(
                    MypReport(
                        id = System.currentTimeMillis(),
                        targetName = member,
                        title = "$group ($typeText)",
                        content = content,
                        date = today,
                        state = "답변 대기 중"
                    )
                )
                Toast.makeText(context, "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(context, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showGroupPopup(anchorView: View) {
        val popupView = layoutInflater.inflate(R.layout.fragment_myp_report_group, null)

        val density = resources.displayMetrics.density
        val width = (340 * density).toInt()
        val height = (178 * density).toInt()

        val popupWindow = PopupWindow(popupView, width, height, true)
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true


        popupWindow.showAsDropDown(binding.mypReportGroupEt, 0, 10)
    }

    // 멤버 선택 팝업
    private fun showMemberPopup(anchorView: View) {
        val popupView = layoutInflater.inflate(R.layout.fragment_myp_report_member, null)

        val density = resources.displayMetrics.density
        val width = (340 * density).toInt()
        val height = (178 * density).toInt()

        val popupWindow = PopupWindow(popupView, width, height, true)
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true

        // EditText 바로 아래에 표시
        popupWindow.showAsDropDown(binding.mypReportMemberEt, 0, 10)
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