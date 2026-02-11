package com.bookiibookii.bookiibookii.myPage.report

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
    private var selectedTargetId: Int? = null // ★ [추가됨] 선택된 타겟(멤버)의 ID 저장용

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypReportWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
        initReportTypeRadioGroup()
        initValidation()
    }

    private fun initListeners() {
        binding.mypReportBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

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
                        selectedTargetId = null // ★ 멤버 ID도 같이 초기화
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
                    showDropdown(binding.mypReportMemberPlusIv, memberNames) { name, index ->
                        // 멤버 선택 시 처리
                        binding.mypReportMemberEt.setText(name)

                        // ★ [추가됨] 선택한 멤버의 실제 고유 ID를 저장합니다.
                        // 주의: members[index].userId 부분은 실제 모델의 ID 필드명(예: memberId, id 등)에 맞게 수정해 주세요!
                        selectedTargetId = members[index].userId
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

        val popupView = RecyclerView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            background = ContextCompat.getDrawable(context, R.drawable.bg_round_10dp_gray300)
            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            layoutManager = LinearLayoutManager(context)
            setPadding(0, 10, 0, 10)
        }

        val popupWindow = PopupWindow(popupView, 500, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        popupView.adapter = PopupAdapter(items) { name, index ->
            onSelected(name, index)
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchorView, -400, 0)
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
                        applySelectedStyle(view)
                    } else {
                        applyUnselectedStyle(view)
                    }
                }
            }
            checkValidation()
        }
    }

    private fun applySelectedStyle(rb: RadioButton) {
        rb.setBackgroundResource(R.drawable.bg_round_20dp_orange_stroke)
        rb.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_orange, 0, 0, 0)
        rb.setTextColor(ContextCompat.getColor(requireContext(), R.color.pre_main))
    }

    private fun applyUnselectedStyle(rb: RadioButton) {
        rb.setBackgroundResource(R.drawable.bg_input_round_20dp_white)
        rb.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_gray, 0, 0, 0)
        rb.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_900))
    }

    // --- 필수값 체크 및 버튼 활성화 로직 ---

    private fun initValidation() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { checkValidation() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.mypReportGroupEt.addTextChangedListener(watcher)
        binding.mypReportMemberEt.addTextChangedListener(watcher) // 타겟 ID를 위해 멤버도 필수 체크 연결
        binding.mypReportContentEt.addTextChangedListener(watcher)
    }

    private fun checkValidation() {
        val isGroupFilled = binding.mypReportGroupEt.text.isNotEmpty()
        val isMemberFilled = binding.mypReportMemberEt.text.isNotEmpty()
        val isTypeSelected = binding.mypReportTypeRg.checkedRadioButtonId != -1
        val isContentFilled = binding.mypReportContentEt.text.isNotEmpty()

        val isValid = isGroupFilled && isMemberFilled && isTypeSelected && isContentFilled

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
        val content = binding.mypReportContentEt.text.toString()
        val type = getSelectedReportType() ?: return

        // ★ [수정됨] 이름이 아닌 ID 값을 가져옵니다.
        val groupId = selectedGroupId
        val targetId = selectedTargetId

        if (groupId == null || targetId == null) {
            Toast.makeText(context, "그룹과 신고 대상을 정확히 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // ★ [수정됨] API 명세서에 맞춰 groupId와 targetId를 파라미터로 넘깁니다.
                val request = ReportRequest(
                    groupId = groupId,
                    targetId = targetId,
                    reportType = type,
                    content = content
                )

                val response = RetrofitClient.api().postReport(request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(context, "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Toast.makeText(context, "전송 실패: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ReportWrite", "API 오류", e)
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSelectedReportType(): String? {
        // ★ [수정됨] API 명세서에 맞게 String 값을 완벽히 맞춥니다.
        return when (binding.mypReportTypeRg.checkedRadioButtonId) {
            binding.mypReportType1Rb.id -> "ABUSE"
            binding.mypReportType2Rb.id -> "SPAM"
            binding.mypReportType3Rb.id -> "NO_SHOW"
            binding.mypReportType4Rb.id -> "DAMAGED_BOOK"
            binding.mypReportType5Rb.id -> "OTHER" // *만약 서버 명세서에 OTHER가 없다면 다른 값으로 대체하거나 서버쪽에 추가를 요청해야 합니다.
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