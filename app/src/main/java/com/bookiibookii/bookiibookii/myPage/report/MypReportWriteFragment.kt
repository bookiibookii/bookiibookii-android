package com.bookiibookii.bookiibookii.myPage.report

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.GroupSummary
import com.bookiibookii.bookiibookii.data.model.ReportRequest
import com.bookiibookii.bookiibookii.databinding.FragmentMypReportWriteBinding
import kotlinx.coroutines.launch

class MypReportWriteFragment : Fragment() {
    private var _binding: FragmentMypReportWriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog

    private var myGroups: List<GroupSummary> = emptyList()
    private var selectedGroupId: Int? = null
    private var selectedTargetId: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypReportWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        // 1. 기본적으로 EditText 입력 불가하게 설정 (클릭만 가능)
        binding.mypReportGroupEt.isFocusable = false
        binding.mypReportGroupEt.isFocusableInTouchMode = false
        binding.mypReportMemberEt.isFocusable = false
        binding.mypReportMemberEt.isFocusableInTouchMode = false

        initListeners()
        initReportTypeRadioGroup()
        initValidation()
    }

    private fun initListeners() {
        binding.mypReportBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        // 그룹 선택
        val groupClickListener = View.OnClickListener {
            fetchMyGroupsAndShowPopup()
        }
        binding.mypReportGroupEt.setOnClickListener(groupClickListener)
        binding.mypReportGroupPlusIv.setOnClickListener(groupClickListener)

        // 멤버 선택
        val memberClickListener = View.OnClickListener {
            // 5. 그룹 선택 안 된 상태로 멤버 누르면 토스트 메시지
            if (selectedGroupId == null) {
                Toast.makeText(context, "먼저 신고할 그룹을 선택해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                fetchGroupMembersAndShowPopup(selectedGroupId!!)
            }
        }
        binding.mypReportMemberEt.setOnClickListener(memberClickListener)
        binding.mypReportMemberPlusIv.setOnClickListener(memberClickListener)

        // 6. 전송 버튼 (유효성 검사 후 전송)
        binding.mypWriteBtn.setOnClickListener {
            val safeContext = context ?: return@setOnClickListener

            if (selectedGroupId == null) {
                Toast.makeText(safeContext, "신고 그룹을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedTargetId == null) {
                Toast.makeText(safeContext, "신고 대상을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.mypReportTypeRg.checkedRadioButtonId == -1) {
                Toast.makeText(safeContext, "신고 유형을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.mypReportContentEt.text.toString().trim().isEmpty()) {
                Toast.makeText(safeContext, "신고 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendReport()
        }
    }

    // --- 그룹 드롭다운 로직 ---
    private fun fetchMyGroupsAndShowPopup() {
        lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().getMyGroups()
                val safeContext = context ?: return@launch

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    myGroups = response.body()?.result ?: emptyList()

                    // 3. 내 그룹 / 참여 그룹 분류 로직
                    val myGroupsList = myGroups.filter { it.isHost }
                    val otherGroupsList = myGroups.filter { !it.isHost }

                    val popupItems = mutableListOf<Any>()
                    if (myGroupsList.isNotEmpty()) {
                        popupItems.add("내 그룹") // 헤더
                        popupItems.addAll(myGroupsList)
                    }
                    if (otherGroupsList.isNotEmpty()) {
                        popupItems.add("참여 그룹") // 헤더
                        popupItems.addAll(otherGroupsList)
                    }

                    // 2. 드롭다운 열릴 때 아이콘 주황색으로 변경
                    binding.mypReportGroupPlusIv.setImageResource(R.drawable.ic_plus_orange)

                    showCustomDropdown(
                        anchorView = binding.mypReportGroupEt,
                        items = popupItems,
                        safeContext = safeContext,
                        dropdownHeight = dpToPx(200), // 3. 높이 200 주고 스크롤
                        isGroup = true
                    ) { selectedItem ->
                        val group = selectedItem as GroupSummary
                        val displayText = if (group.isHost) group.groupName else "[${group.groupHostNickname}] ${group.groupName}"

                        binding.mypReportGroupEt.setText(displayText)
                        selectedGroupId = group.groupId

                        // 그룹 바뀌면 멤버 초기화
                        binding.mypReportMemberEt.setText("")
                        selectedTargetId = null
                        binding.mypReportMemberPlusIv.setImageResource(R.drawable.ic_plus)
                        binding.mypReportMemberPlusIv.visibility = View.VISIBLE

                        // 2. 선택 완료 시 + 버튼 숨기기
                        binding.mypReportGroupPlusIv.visibility = View.GONE
                        checkValidation()
                    }
                } else {
                    Toast.makeText(safeContext, "그룹 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ReportWrite", "그룹 조회 에러", e)
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    // --- 멤버 드롭다운 로직 ---
    private fun fetchGroupMembersAndShowPopup(groupId: Int) {
        lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().getGroupMembers(groupId)
                val safeContext = context ?: return@launch

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val members = response.body()?.result ?: emptyList()

                    // 4. 멤버 드롭다운 헤더 추가
                    val selectedGroup = myGroups.find { it.groupId == groupId }
                    val headerText = if (selectedGroup != null) {
                        "[${selectedGroup.groupHostNickname}] ${selectedGroup.groupName}"
                    } else "신고 대상 선택"

                    val popupItems = mutableListOf<Any>()
                    popupItems.add(headerText) // 헤더
                    popupItems.addAll(members)

                    // 2. 오픈 시 주황색 변경
                    binding.mypReportMemberPlusIv.setImageResource(R.drawable.ic_plus_orange)

                    showCustomDropdown(
                        anchorView = binding.mypReportMemberEt,
                        items = popupItems,
                        safeContext = safeContext,
                        dropdownHeight = dpToPx(120), // 4. 높이 120 주고 스크롤
                        isGroup = false
                    ) { selectedItem ->
                        // ※ 주의: API 응답의 실제 모델 이름(MemberSummary)에 맞춰서 캐스팅
                        // 임시로 Any로 받아 nickname을 추출하도록 작성했습니다.
                        // member 객체에 nickname, userId 필드가 있다고 가정합니다.
                        try {
                            val member = selectedItem
                            val nickname = member.javaClass.getMethod("getNickname").invoke(member) as String
                            val userId = member.javaClass.getMethod("getUserId").invoke(member) as Int

                            binding.mypReportMemberEt.setText(nickname)
                            selectedTargetId = userId

                            // 2. 선택 완료 시 + 버튼 숨기기
                            binding.mypReportMemberPlusIv.visibility = View.GONE
                            checkValidation()
                        } catch (e: Exception) {
                            Log.e("ReportWrite", "멤버 캐스팅 에러", e)
                        }
                    }
                } else {
                    Toast.makeText(safeContext, "멤버 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ReportWrite", "멤버 조회 에러", e)
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    // --- 통합 커스텀 드롭다운 띄우기 ---
    private fun showCustomDropdown(
        anchorView: View,
        items: List<Any>,
        safeContext: Context,
        dropdownHeight: Int,
        isGroup: Boolean,
        onSelected: (Any) -> Unit
    ) {
        val popupView = RecyclerView(safeContext).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            background = ContextCompat.getDrawable(safeContext, R.drawable.bg_round_10dp_gray300)
            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            layoutManager = LinearLayoutManager(safeContext)
            setPadding(0, dpToPx(8), 0, dpToPx(8))
        }

        val popupWidth = anchorView.width.takeIf { it > 0 } ?: 800
        val popupWindow = PopupWindow(popupView, popupWidth, dropdownHeight, true)
        popupWindow.elevation = 10f
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 창이 닫힐 때 + 아이콘 상태 복구 로직
        popupWindow.setOnDismissListener {
            if (isGroup && selectedGroupId == null) {
                binding.mypReportGroupPlusIv.setImageResource(R.drawable.ic_plus) // 기본 아이콘으로 복구 (XML 확인)
            } else if (!isGroup && selectedTargetId == null) {
                binding.mypReportMemberPlusIv.setImageResource(R.drawable.ic_plus) // 기본 아이콘으로 복구
            }
        }

        // 인라인 커스텀 어댑터 생성 (헤더와 아이템 분리)
        popupView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            val TYPE_HEADER = 0
            val TYPE_ITEM = 1

            override fun getItemViewType(position: Int): Int {
                return if (items[position] is String) TYPE_HEADER else TYPE_ITEM
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                if (viewType == TYPE_HEADER) {
                    val tv = TextView(parent.context).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(4))
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                        setTextColor(ContextCompat.getColor(context, R.color.grey_500))
                        setTypeface(null, Typeface.BOLD)
                    }
                    return object : RecyclerView.ViewHolder(tv) {}
                } else {
                    val tv = TextView(parent.context).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                        setTextColor(ContextCompat.getColor(context, R.color.grey_900))
                    }
                    return object : RecyclerView.ViewHolder(tv) {}
                }
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val item = items[position]
                val tv = holder.itemView as TextView

                if (getItemViewType(position) == TYPE_HEADER) {
                    tv.text = item as String
                } else {
                    if (isGroup) {
                        val group = item as GroupSummary
                        // 3. 내 그룹은 책 제목만, 참여 그룹은 [호스트] 책 제목
                        tv.text = if (group.isHost) group.groupName else "[${group.groupHostNickname}] ${group.groupName}"
                    } else {
                        // 멤버 이름 표시
                        try {
                            tv.text = item.javaClass.getMethod("getNickname").invoke(item) as String
                        } catch (e: Exception) {}
                    }

                    tv.setOnClickListener {
                        onSelected(item)
                        popupWindow.dismiss()
                    }
                }
            }
            override fun getItemCount() = items.size
        }

        popupWindow.showAsDropDown(anchorView, 0, dpToPx(4))
    }

    // --- 라디오 버튼 스타일 변경 로직 ---
    private fun initReportTypeRadioGroup() {
        val radioGroup = binding.mypReportTypeRg
        val count = radioGroup.childCount

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until count) {
                val view = group.getChildAt(i)
                if (view is RadioButton) {
                    if (view.id == checkedId) applySelectedStyle(view)
                    else applyUnselectedStyle(view)
                }
            }
            checkValidation()
        }
    }

    private fun applySelectedStyle(rb: RadioButton) {
        val safeContext = context ?: return
        rb.setBackgroundResource(R.drawable.bg_round_20dp_orange_stroke)
        rb.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_orange, 0, 0, 0)
        rb.setTextColor(ContextCompat.getColor(safeContext, R.color.pre_main))
    }

    private fun applyUnselectedStyle(rb: RadioButton) {
        val safeContext = context ?: return
        rb.setBackgroundResource(R.drawable.bg_input_round_20dp_white)
        rb.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_gray, 0, 0, 0)
        rb.setTextColor(ContextCompat.getColor(safeContext, R.color.grey_900))
    }

    // --- 버튼 시각적 활성화 처리 (실제 기능 제한은 clickListener에서 Toast로 방어) ---
    private fun initValidation() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { checkValidation() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.mypReportGroupEt.addTextChangedListener(watcher)
        binding.mypReportMemberEt.addTextChangedListener(watcher)
        binding.mypReportContentEt.addTextChangedListener(watcher)
    }

    private fun checkValidation() {
        val isGroupFilled = selectedGroupId != null
        val isMemberFilled = selectedTargetId != null
        val isTypeSelected = binding.mypReportTypeRg.checkedRadioButtonId != -1
        val isContentFilled = binding.mypReportContentEt.text.toString().trim().isNotEmpty()

        val isValid = isGroupFilled && isMemberFilled && isTypeSelected && isContentFilled
        updateButtonVisualState(isValid)
    }

    private fun updateButtonVisualState(isValid: Boolean) {
        val safeContext = context ?: return
        // 주의: 버튼은 항상 활성화 상태여야 빈칸일 때 Toast를 띄울 수 있음.
        if (isValid) {
            binding.mypWriteBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(safeContext, R.color.grey_900))
            binding.mypWriteBtn.setTextColor(ContextCompat.getColor(safeContext, R.color.white))
        } else {
            binding.mypWriteBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(safeContext, R.color.grey_200))
            binding.mypWriteBtn.setTextColor(ContextCompat.getColor(safeContext, R.color.grey_500))
        }
    }

    // --- API 전송 로직 ---
    private fun sendReport() {
        val content = binding.mypReportContentEt.text.toString()
        val type = getSelectedReportType() ?: return
        val groupId = selectedGroupId ?: return
        val targetId = selectedTargetId ?: return

        lifecycleScope.launch {
            loadingDialog.show()
            try {
                val safeContext = context ?: return@launch
                val request = ReportRequest(
                    groupId = groupId,
                    targetId = targetId,
                    reportType = type,
                    content = content
                )

                val response = RetrofitClient.api().postReport(request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(safeContext, "신고가 정상적으로 접수되었습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Toast.makeText(safeContext, "전송 실패: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ReportWrite", "API 오류", e)
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    private fun getSelectedReportType(): String? {
        return when (binding.mypReportTypeRg.checkedRadioButtonId) {
            binding.mypReportType1Rb.id -> "ABUSE"
            binding.mypReportType2Rb.id -> "SPAM"
            binding.mypReportType3Rb.id -> "NO_SHOW"
            binding.mypReportType4Rb.id -> "DAMAGED_BOOK"
            binding.mypReportType5Rb.id -> "OTHER"
            else -> null
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}