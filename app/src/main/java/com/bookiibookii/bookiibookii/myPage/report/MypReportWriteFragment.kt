package com.bookiibookii.bookiibookii.myPage.report

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.GroupSummary
import com.bookiibookii.bookiibookii.data.model.ReportRequest
import com.bookiibookii.bookiibookii.databinding.FragmentMypReportWriteBinding
import com.bookiibookii.bookiibookii.util.PopupAdapter
import kotlinx.coroutines.launch

class MypReportWriteFragment : Fragment() {
    private var _binding: FragmentMypReportWriteBinding? = null
    private val binding get() = _binding!!

    // 데이터 저장용
    private var myGroups: List<GroupSummary> = emptyList()
    private var selectedGroupId: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypReportWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
        initReportTypeRadioGroup() // 라디오 버튼 스타일 로직
        initValidation()           // 필수 입력값 체크 로직
    }

    private fun initListeners() {
        binding.mypReportBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 1. 그룹 선택 (+ 버튼)
        binding.mypReportGroupPlusIv.setOnClickListener {
            fetchMyGroupsAndShowPopup()
        }

        // 2. 멤버 선택 (+ 버튼)
        binding.mypReportMemberPlusIv.setOnClickListener {
            if (selectedGroupId == null) {
                Toast.makeText(context, "먼저 신고할 그룹을 선택해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                fetchGroupMembersAndShowPopup(selectedGroupId!!)
            }
        }

        // 3. 전송 버튼
        binding.mypWriteBtn.setOnClickListener {
            // 버튼이 활성화된 상태(clickable=true)에서만 동작
            sendReport()
        }
    }

    // --- 드롭다운 (PopupWindow) 로직 ---

    private fun fetchMyGroupsAndShowPopup() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getMyGroups()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    myGroups = response.body()!!.result
                    val groupNames = myGroups.map { it.name }
                    showDropdown(binding.mypReportGroupPlusIv, groupNames) { name, index ->
                        // 그룹 선택 시 처리
                        binding.mypReportGroupEt.setText(name)
                        selectedGroupId = myGroups[index].groupId

                        // 그룹이 바뀌면 멤버 초기화
                        binding.mypReportMemberEt.setText("")
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportWrite", "그룹 조회 실패", e)
            }
        }
    }

    private fun fetchGroupMembersAndShowPopup(groupId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getGroupMembers(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val members = response.body()!!.result
                    val memberNames = members.map { it.nickname }
                    showDropdown(binding.mypReportMemberPlusIv, memberNames) { name, _ ->
                        // 멤버 선택 시 처리
                        binding.mypReportMemberEt.setText(name)
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportWrite", "멤버 조회 실패", e)
            }
        }
    }

    private fun showDropdown(anchorView: View, items: List<String>, onSelected: (String, Int) -> Unit) {
        if (items.isEmpty()) {
            Toast.makeText(context, "목록이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val inflater = LayoutInflater.from(context)
        // 팝업용 레이아웃을 코드로 생성하거나 별도 XML 사용 가능. 여기선 간단히 RecyclerView만 있는 뷰 생성
        val popupView = RecyclerView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            background = ContextCompat.getDrawable(context, R.drawable.bg_round_10dp_gray300) // 배경 설정 (XML 필요)
            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            layoutManager = LinearLayoutManager(context)
            setPadding(0, 10, 0, 10)
        }

        val popupWindow = PopupWindow(popupView, 500, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명 처리해야 둥근 모서리 보임

        popupView.adapter = PopupAdapter(items) { name, index ->
            onSelected(name, index)
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchorView, -400, 0) // 위치 조정
    }

    // --- 라디오 버튼 스타일 변경 로직 ---

    private fun initReportTypeRadioGroup() {
        val radioGroup = binding.mypReportTypeRg
        val count = radioGroup.childCount

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until count) {
                val view = group.getChildAt(i)
                if (view is RadioButton) {
                    if (view.id == checkedId) {
                        // 선택된 버튼 스타일 적용
                        applySelectedStyle(view)
                    } else {
                        // 선택 해제된 버튼 스타일 적용
                        applyUnselectedStyle(view)
                    }
                }
            }
            checkValidation() // 라디오 버튼 변경 시 유효성 검사
        }
    }

    private fun applySelectedStyle(rb: RadioButton) {
        rb.setBackgroundResource(R.drawable.bg_round_20dp_orange_stroke) // 오렌지 테두리
        rb.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_orange, 0, 0, 0) // 오렌지 체크 아이콘
        rb.setTextColor(ContextCompat.getColor(requireContext(), R.color.pre_main)) // 메인 컬러 텍스트
    }

    private fun applyUnselectedStyle(rb: RadioButton) {
        rb.setBackgroundResource(R.drawable.bg_input_round_20dp_white) // 흰색 배경 (기본)
        rb.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_gray, 0, 0, 0) // 회색 체크 아이콘
        rb.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_900)) // 검정 텍스트
    }


    // --- 필수값 체크 및 버튼 활성화 로직 ---

    private fun initValidation() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { checkValidation() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.mypReportGroupEt.addTextChangedListener(watcher)
        // 멤버는 필수는 아니지만, 로직상 필요하다면 추가
        binding.mypReportContentEt.addTextChangedListener(watcher)
    }

    private fun checkValidation() {
        val isGroupFilled = binding.mypReportGroupEt.text.isNotEmpty()
        val isTypeSelected = binding.mypReportTypeRg.checkedRadioButtonId != -1
        val isContentFilled = binding.mypReportContentEt.text.isNotEmpty()

        // 필수 항목: 그룹, 유형, 내용 (멤버는 UI상 별표가 없어서 제외했습니다. 필요시 추가하세요)
        val isValid = isGroupFilled && isTypeSelected && isContentFilled

        updateButtonState(isValid)
    }

    private fun updateButtonState(isEnabled: Boolean) {
        if (isEnabled) {
            binding.mypWriteBtn.isEnabled = true
            binding.mypWriteBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey_900))
            binding.mypWriteBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            binding.mypWriteBtn.isEnabled = false
            binding.mypWriteBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey_200))
            binding.mypWriteBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_500))
        }
    }

    // --- API 전송 로직 ---
    private fun sendReport() {
        val groupName = binding.mypReportGroupEt.text.toString()
        val memberName = binding.mypReportMemberEt.text.toString()
        val content = binding.mypReportContentEt.text.toString()
        val type = getSelectedReportType() ?: return

        lifecycleScope.launch {
            try {
                val request = ReportRequest(groupName, memberName, type, content)
                val response = RetrofitClient.api().postReport(request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(context, "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(context, "전송 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSelectedReportType(): String? {
        return when (binding.mypReportTypeRg.checkedRadioButtonId) {
            binding.mypReportType1Rb.id -> "ABUSE"
            binding.mypReportType2Rb.id -> "SPAM"
            binding.mypReportType3Rb.id -> "NOSHOW"
            binding.mypReportType4Rb.id -> "DAMAGE"
            binding.mypReportType5Rb.id -> "OTHER"
            else -> null
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