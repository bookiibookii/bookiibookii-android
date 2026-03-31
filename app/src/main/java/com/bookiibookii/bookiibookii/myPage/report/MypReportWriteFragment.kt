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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.GroupSummary
import com.bookiibookii.bookiibookii.data.model.ReportRequest
import com.bookiibookii.bookiibookii.databinding.FragmentMypReportWriteBinding
import kotlinx.coroutines.launch

class MypReportWriteFragment : BaseDetailFragment<FragmentMypReportWriteBinding>() {

    private lateinit var loadingDialog: LoadingDialog

    private var myGroups: List<GroupSummary> = emptyList()
    private var selectedGroupId: Int? = null
    private var selectedTargetId: Int? = null

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMypReportWriteBinding {
        return FragmentMypReportWriteBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getInt("FIXED_ID", -1)?.takeIf { it != -1 }?.let { id ->
            selectedGroupId = id
            binding.mypReportGroupEt.setText(arguments?.getString("FIXED_NAME"))
            binding.mypReportGroupEt.isEnabled = false
            binding.mypReportGroupPlusIv.visibility = View.GONE
        }

        loadingDialog = LoadingDialog(requireContext())

        binding.mypReportGroupEt.isFocusable = false
        binding.mypReportGroupEt.isFocusableInTouchMode = false
        binding.mypReportMemberEt.isFocusable = false
        binding.mypReportMemberEt.isFocusableInTouchMode = false

        initListeners()
        initReportTypeRadioGroup()
        initValidation()
        setupKeyboardAutoScroll() // ★ 자동 스크롤 적용
    }

    // ★ 텍스트 박스 터치 시 키보드 위로 스크롤을 끌어올리는 함수
    private fun setupKeyboardAutoScroll() {
        // 신고 그룹과 멤버 선택창은 키보드가 안 올라오므로, 신고 내용(Content)에만 적용
        val scrollAction = {
            binding.mypReportContentEt.postDelayed({
                binding.mypReportContentEt.requestRectangleOnScreen(
                    android.graphics.Rect(0, binding.mypReportContentEt.height, binding.mypReportContentEt.width, binding.mypReportContentEt.height), true
                )
            }, 300)
        }
        binding.mypReportContentEt.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) scrollAction() }
        binding.mypReportContentEt.setOnClickListener { scrollAction() }
    }

    private fun initListeners() {
        binding.mypReportBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        val groupClickListener = View.OnClickListener {
            fetchMyGroupsAndShowPopup()
        }
        binding.mypReportGroupEt.setOnClickListener(groupClickListener)
        binding.mypReportGroupPlusIv.setOnClickListener(groupClickListener)

        val memberClickListener = View.OnClickListener {
            if (selectedGroupId == null) {
                Toast.makeText(context, "먼저 신고할 그룹을 선택해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                fetchGroupMembersAndShowPopup(selectedGroupId!!)
            }
        }
        binding.mypReportMemberEt.setOnClickListener(memberClickListener)
        binding.mypReportMemberPlusIv.setOnClickListener(memberClickListener)

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

    private fun fetchMyGroupsAndShowPopup() {
        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().getMyGroups()
                val safeContext = context ?: return@launch

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    myGroups = response.body()?.result ?: emptyList()

                    val myGroupsList = myGroups.filter { it.isHost }
                    val otherGroupsList = myGroups.filter { !it.isHost }

                    val popupItems = mutableListOf<Any>()
                    if (myGroupsList.isNotEmpty()) {
                        popupItems.add("내 그룹")
                        popupItems.addAll(myGroupsList)
                    }
                    if (otherGroupsList.isNotEmpty()) {
                        popupItems.add("참여 그룹")
                        popupItems.addAll(otherGroupsList)
                    }

                    binding.mypReportGroupPlusIv.setImageResource(R.drawable.ic_plus_orange)

                    showCustomDropdown(
                        anchorView = binding.mypReportGroupEt,
                        items = popupItems,
                        safeContext = safeContext,
                        dropdownHeight = dpToPx(200),
                        isGroup = true
                    ) { selectedItem ->
                        val group = selectedItem as GroupSummary
                        val displayText = if (group.isHost) group.groupName else "[${group.groupHostNickname}] ${group.groupName}"

                        binding.mypReportGroupEt.setText(displayText)
                        selectedGroupId = group.groupId

                        binding.mypReportMemberEt.setText("")
                        selectedTargetId = null
                        binding.mypReportMemberPlusIv.setImageResource(R.drawable.ic_plus)
                        binding.mypReportMemberPlusIv.visibility = View.VISIBLE

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

    private fun fetchGroupMembersAndShowPopup(groupId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().getGroupMembers(groupId)
                val safeContext = context ?: return@launch

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val members = response.body()?.result ?: emptyList()

                    val selectedGroup = myGroups.find { it.groupId == groupId }
                    val headerText = if (selectedGroup != null) {
                        "[${selectedGroup.groupHostNickname}] ${selectedGroup.groupName}"
                    } else "신고 대상 선택"

                    val popupItems = mutableListOf<Any>()
                    popupItems.add(headerText)
                    popupItems.addAll(members)

                    binding.mypReportMemberPlusIv.setImageResource(R.drawable.ic_plus_orange)

                    showCustomDropdown(
                        anchorView = binding.mypReportMemberEt,
                        items = popupItems,
                        safeContext = safeContext,
                        dropdownHeight = dpToPx(120),
                        isGroup = false
                    ) { selectedItem ->
                        try {
                            val member = selectedItem
                            val nickname = member.javaClass.getMethod("getNickname").invoke(member) as String
                            val userId = member.javaClass.getMethod("getUserId").invoke(member) as Int

                            binding.mypReportMemberEt.setText(nickname)
                            selectedTargetId = userId

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

        popupWindow.setOnDismissListener {
            if (isGroup && selectedGroupId == null) {
                binding.mypReportGroupPlusIv.setImageResource(R.drawable.ic_plus)
            } else if (!isGroup && selectedTargetId == null) {
                binding.mypReportMemberPlusIv.setImageResource(R.drawable.ic_plus)
            }
        }

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
                        tv.text = if (group.isHost) group.groupName else "[${group.groupHostNickname}] ${group.groupName}"
                    } else {
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
        if (isValid) {
            binding.mypWriteBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(safeContext, R.color.grey_900))
            binding.mypWriteBtn.setTextColor(ContextCompat.getColor(safeContext, R.color.white))
        } else {
            binding.mypWriteBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(safeContext, R.color.grey_200))
            binding.mypWriteBtn.setTextColor(ContextCompat.getColor(safeContext, R.color.grey_500))
        }
    }

    private fun sendReport() {
        val content = binding.mypReportContentEt.text.toString()
        val type = getSelectedReportType() ?: return
        val groupId = selectedGroupId ?: return
        val targetId = selectedTargetId ?: return

        viewLifecycleOwner.lifecycleScope.launch {
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
}