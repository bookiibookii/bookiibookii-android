package com.bookiibookii.bookiibookii.group

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.GroupCreateRequest
import com.bookiibookii.bookiibookii.data.model.GroupTagRequest
import com.bookiibookii.bookiibookii.databinding.ActivityGrpGenerationBinding
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GroupGenerationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpGenerationBinding

    private var isEditMode = false
    private var groupType = "RELAY"

    private lateinit var searchAdapter: GrpSearchBookAdapter
    private var searchJob: Job? = null
    private var selectedIsbn: String = ""

    // 상태 변수
    private var selectedDate: String? = null
    private var selectedTradeType: String? = null
    private var selectedBookHave: Boolean? = null
    private var isItemSelectMode = false

    // ★ 칩 선택 제한을 위한 이전 상태 저장 변수
    private var previousCheckedIds: List<Int> = emptyList()

    private var isCustomTagSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrpGenerationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        groupType = intent.getStringExtra("GROUP_TYPE") ?: "RELAY"

        updateUiState()

        // 리스너 초기화
        initHashTagChipListener()
        initDirectInputListener()
        initSearchAdapter()
        checkInputs()
        initListener()
    }

    private fun updateUiState() {
        val isRelay = groupType == "RELAY"
        with(binding) {
            if (isEditMode) {
                actGrpGenMainTitleTv.text = "그룹 수정"
                actGrpGenRunBtn.text = "수정 완료"
                val bookTitle = intent.getStringExtra("BOOK_TITLE") ?: "제목 없음"
                actGrpGenEditBookTitleTv.visibility = View.VISIBLE
                actGrpGenEditBookTitleTv.text = bookTitle
            } else {
                actGrpGenMainTitleTv.text = "그룹 만들기"
                actGrpGenRunBtn.text = "그룹 만들기"
                actGrpGenEditBookTitleTv.visibility = View.GONE
                actGrpGenBookSelectDateTv.text = ""
                actGrpGenBookLimitBar.setText("")

                // (수정) EditText는 항상 보이되 칩처럼 동작하므로 visibility 조작 제거
            }

            val searchVisibility = if (!isEditMode) View.VISIBLE else View.GONE
            actGrpGenBookSerachTv.visibility = searchVisibility
            actGrpGenBookSerachStarTv.visibility = searchVisibility
            actGrpGenBookSearchBar.visibility = searchVisibility

            if (!isEditMode && isRelay) {
                actGrpGenBookPossessionGroup.visibility = View.VISIBLE
            } else {
                actGrpGenBookPossessionGroup.visibility = View.GONE
            }

            if (isRelay) {
                actGrpGenMethodContainer.visibility = View.VISIBLE
            } else {
                actGrpGenMethodContainer.visibility = View.GONE
                actGrpGenDirectInputGroup.visibility = View.GONE
            }

            if (!isRelay) {
                actGrpGenMemberCountGroup.visibility = View.VISIBLE
            } else {
                actGrpGenMemberCountGroup.visibility = View.GONE
            }
        }
    }

    // ★ 1. 칩 선택 리스너 (기존 로직 유지)
    private fun initHashTagChipListener() {
        // [필터] 공백 방지 + 8자 제한
        val spaceFilter = android.text.InputFilter { source, _, _, _, _, _ ->
            if (source.contains(" ")) "" else null
        }
        binding.actGrpGenDirectInputEt.filters = arrayOf(spaceFilter, android.text.InputFilter.LengthFilter(8))

        binding.actGrpGenChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->

            val hasCustomInput = binding.actGrpGenDirectInputEt.text.toString().isNotEmpty()
            // 칩 개수 + 직접입력(있으면 1, 없으면 0)
            val currentCount = checkedIds.size + (if (isCustomTagSelected) 1 else 0)

            // [핵심 로직] 3개 초과 선택 방지
            if (currentCount > 3) {
                // 3개 초과 시 방금 누른 칩 취소
                val newlyAddedIds = checkedIds - previousCheckedIds.toSet()
                if (newlyAddedIds.isNotEmpty()) {
                    val idToUncheck = newlyAddedIds.first()
                    binding.actGrpGenChipGroup.post {
                        group.findViewById<Chip>(idToUncheck)?.isChecked = false
                        showCustomToast("태그는 최대 3개까지만 선택 가능합니다.")
                    }
                }
                // 여기서 return 하여 previousCheckedIds 업데이트 방지
                return@setOnCheckedStateChangeListener
            }

            previousCheckedIds = checkedIds
            checkInputs()
        }
    }

    // ★ 2. 입력창 리스너 (UI Selector 적용 및 제한 로직 추가)
    private fun initDirectInputListener() {
        val et = binding.actGrpGenDirectInputEt

        // (1) TextWatcher (기존 동일)
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                if (text.isEmpty()) {
                    setCustomTagState(false)
                } else {
                    if (!isCustomTagSelected) setCustomTagState(true)
                }
                checkInputs()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // (2) TouchListener (★ 수정됨)
        et.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {

                // ★ [추가된 로직] 이미 3개가 꽉 찼고, 내가 선택된 상태가 아니라면 -> 진입 차단
                val chipCount = binding.actGrpGenChipGroup.checkedChipIds.size
                if (chipCount >= 3 && !isCustomTagSelected) {
                    et.clearFocus()
                    // 키보드 내리기 (혹시라도 떴을 경우 대비)
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)

                    showCustomToast("태그는 최대 3개까지만 선택 가능합니다.")
                    return@setOnTouchListener true // ★ true를 반환하여 터치 이벤트를 소비 -> 키보드/포커스 차단
                }

                // [기존 로직] 텍스트가 있을 때 토글 기능
                if (et.text.toString().isNotEmpty()) {
                    if (isCustomTagSelected) {
                        if (et.hasFocus()) {
                            return@setOnTouchListener false
                        } else {
                            setCustomTagState(false)
                            et.clearFocus()
                            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(v.windowToken, 0)
                            checkInputs()
                            return@setOnTouchListener true
                        }
                    } else {
                        setCustomTagState(true)
                        return@setOnTouchListener false
                    }
                }
            }
            false
        }

        // (3) FocusChangeListener (★ 보완)
        // 터치 외에 다른 방식(Next 키 등)으로 포커스가 넘어왔을 때도 쫓아내야 함
        et.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val chipCount = binding.actGrpGenChipGroup.checkedChipIds.size

                // 내가 선택되지 않았는데 이미 3개라면 -> 포커스 해제
                if (!isCustomTagSelected && chipCount >= 3) {
                    et.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(et.windowToken, 0)
                    showCustomToast("태그는 최대 3개까지만 선택 가능합니다.")
                }
            } else {
                if (et.text.toString().isEmpty()) {
                    setCustomTagState(false)
                }
            }
        }

        // (4) EditorAction (기존 동일)
        et.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                et.clearFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                return@setOnEditorActionListener true
            }
            false
        }
    }
    // ★ [헬퍼 함수] 상태 변경 및 UI 업데이트를 한 곳에서 관리
    private fun setCustomTagState(isSelected: Boolean) {
        isCustomTagSelected = isSelected
        binding.actGrpGenDirectInputEt.isSelected = isSelected // XML Selector 발동 (색상 변경)
    }

    private fun initListener() {
        with(binding) {
            actGrpGenBackIv.setOnClickListener { finish() }

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { checkInputs() }
                override fun afterTextChanged(s: Editable?) {}
            }

            actGrpGenBookLimitBar.addTextChangedListener(textWatcher)
            actGrpGenMemberCountEt.addTextChangedListener(textWatcher)
            actGrpGenIntroduceBar.addTextChangedListener(textWatcher)
            actGrpGenRegionEt.addTextChangedListener(textWatcher)
            actGrpGenPlaceEt.addTextChangedListener(textWatcher)

            actGrpGenBookDeliveryBtn.setOnClickListener {
                selectedTradeType = "DELIVERY"
                updateExchangeMethodState(false)
                checkInputs()
            }
            actGrpGenBookDirectBtn.setOnClickListener {
                selectedTradeType = "DIRECT"
                updateExchangeMethodState(true)
                checkInputs()
            }

            actGrpGenBookDateContainer.setOnClickListener {
                val today = MaterialDatePicker.todayInUtcMilliseconds()
                val tomorrow = today + (24L * 60 * 60 * 1000)
                val constraints = CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.from(tomorrow))
                    .build()
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTheme(R.style.CustomDatePickerTheme)
                    .setSelection(tomorrow)
                    .setCalendarConstraints(constraints)
                    .build()

                datePicker.addOnPositiveButtonClickListener { selection ->
                    val sdfUi = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
                    actGrpGenBookSelectDateTv.text = sdfUi.format(Date(selection))
                    actGrpGenBookSelectDateTv.setTextColor(getColor(R.color.grey_900))

                    val sdfServer = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                    selectedDate = sdfServer.format(Date(selection))
                    checkInputs()
                }
                datePicker.show(supportFragmentManager, "DATE_PICKER")
            }

            actGrpGenBookYesBtn.setOnClickListener {
                updateBookHaveState(true)
                checkInputs()
            }
            actGrpGenBookNoBtn.setOnClickListener {
                updateBookHaveState(false)
                showBuyDialog()
            }

            actGrpGenBookSearchBar.setOnEditorActionListener { v, _, _ ->
                if (v.text.isNotEmpty()) checkInputs()
                true
            }

            actGrpGenRunBtn.setOnClickListener {
                if (!it.isEnabled) return@setOnClickListener
                createGroupApi()
            }

            actGrpGenBookSearchBar.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!isItemSelectMode) selectedIsbn = ""
                    checkInputs()
                }
                override fun afterTextChanged(s: Editable?) {
                    if (isItemSelectMode) {
                        isItemSelectMode = false
                        return
                    }
                    val query = s.toString().trim()
                    searchJob?.cancel()
                    if (query.length >= 2) {
                        searchJob = lifecycleScope.launch {
                            delay(500L)
                            searchBooksFromApi(query)
                        }
                    } else {
                        actGrpGenBookSearchRv.visibility = View.GONE
                    }
                }
            })
        }
    }

    private fun checkInputs() {
        val isRelay = (groupType == "RELAY")
        val hasIsbn = selectedIsbn.isNotEmpty()
        val hasDate = !selectedDate.isNullOrEmpty()
        val hasPeriod = binding.actGrpGenBookLimitBar.text.toString().isNotEmpty()
        val commentText = binding.actGrpGenIntroduceBar.text.toString()
        val hasValidComment = commentText.length >= 10


        // ★ [수정] 칩이 하나라도 있거나 OR 에디트텍스트에 글자가 있으면 통과
        val hasValidTag = binding.actGrpGenChipGroup.checkedChipIds.isNotEmpty()

        var isValid = false

        if (isRelay) {
            val hasPossession = (selectedBookHave != null)
            val hasTradeType = (selectedTradeType != null)
            var hasDirectLocation = true
            if (selectedTradeType == "DIRECT") {
                hasDirectLocation = binding.actGrpGenRegionEt.text.toString().isNotEmpty() &&
                        binding.actGrpGenPlaceEt.text.toString().isNotEmpty()
            }
            isValid = hasIsbn && hasDate && hasPeriod && hasPossession && hasTradeType && hasValidComment && hasValidTag && hasDirectLocation
        } else {
            val hasCapacity = binding.actGrpGenMemberCountEt.text.toString().isNotEmpty()
            isValid = hasIsbn && hasDate && hasPeriod && hasCapacity && hasValidComment && hasValidTag
        }
        updateRunButtonState(isValid)
    }

    private fun updateRunButtonState(isEnabled: Boolean) {
        binding.actGrpGenRunBtn.isEnabled = isEnabled
        if (isEnabled) {
            binding.actGrpGenRunBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.grey_900)
            binding.actGrpGenRunBtn.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.actGrpGenRunBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.grey_200)
            binding.actGrpGenRunBtn.setTextColor(ContextCompat.getColor(this, R.color.grey_500))
        }
    }

    private fun showBuyDialog() {
        CommonDialog(
            context = this,
            title = "책을 먼저 구매하시겠습니까?",
            subtitle = "선택하신 도서",
            content = "그룹을 만들기 위해서는 실물 책이 필요합니다.\n구매페이지로 이동할까요?",
            confirmBtnText = "구매하러 가기",
            confirmBtnColor = R.color.grey_900,
            onConfirmClick = {
                updateBookHaveState(null)
                showCustomToast("구매 페이지로 이동 합니다.")
            },
            onCancelClick = { updateBookHaveState(null) }
        ).show()
    }

    private fun updateBookHaveState(isHave: Boolean?) {
        selectedBookHave = isHave
        val mainColor = ContextCompat.getColorStateList(this, R.color.pre_main)
        val paleColor = ContextCompat.getColorStateList(this, R.color.pre_main_pale)
        val grey200 = ContextCompat.getColorStateList(this, R.color.grey_200)
        val white = ContextCompat.getColorStateList(this, R.color.white)
        val mainColorInt = ContextCompat.getColor(this, R.color.pre_main)
        val grey900Int = ContextCompat.getColor(this, R.color.grey_900)

        with(binding) {
            when (isHave) {
                true -> {
                    actGrpGenBookYesBtn.strokeColor = mainColor
                    actGrpGenBookYesBtn.backgroundTintList = paleColor
                    actGrpGenBookYesBtn.setTextColor(mainColorInt)
                    actGrpGenBookNoBtn.strokeColor = grey200
                    actGrpGenBookNoBtn.backgroundTintList = white
                    actGrpGenBookNoBtn.setTextColor(grey900Int)
                }
                false -> {
                    actGrpGenBookNoBtn.strokeColor = mainColor
                    actGrpGenBookNoBtn.backgroundTintList = paleColor
                    actGrpGenBookNoBtn.setTextColor(mainColorInt)
                    actGrpGenBookYesBtn.strokeColor = grey200
                    actGrpGenBookYesBtn.backgroundTintList = white
                    actGrpGenBookYesBtn.setTextColor(grey900Int)
                }
                null -> {
                    actGrpGenBookYesBtn.strokeColor = grey200
                    actGrpGenBookYesBtn.backgroundTintList = white
                    actGrpGenBookYesBtn.setTextColor(grey900Int)
                    actGrpGenBookNoBtn.strokeColor = grey200
                    actGrpGenBookNoBtn.backgroundTintList = white
                    actGrpGenBookNoBtn.setTextColor(grey900Int)
                }
            }
        }
    }

    private fun updateExchangeMethodState(isDirect: Boolean?) {
        val mainColor = ContextCompat.getColorStateList(this, R.color.pre_main)
        val paleColor = ContextCompat.getColorStateList(this, R.color.pre_main_pale)
        val grey200 = ContextCompat.getColorStateList(this, R.color.grey_200)
        val white = ContextCompat.getColorStateList(this, R.color.white)
        val mainColorInt = ContextCompat.getColor(this, R.color.pre_main)
        val grey500Int = ContextCompat.getColor(this, R.color.grey_500)

        with(binding) {
            when (isDirect) {
                true -> {
                    actGrpGenBookDirectBtn.strokeColor = mainColor
                    actGrpGenBookDirectBtn.backgroundTintList = paleColor
                    actGrpGenBookDirectBtn.setTextColor(mainColorInt)
                    actGrpGenBookDeliveryBtn.strokeColor = grey200
                    actGrpGenBookDeliveryBtn.backgroundTintList = white
                    actGrpGenBookDeliveryBtn.setTextColor(grey500Int)
                    actGrpGenDirectInputGroup.visibility = View.VISIBLE
                }
                false -> {
                    actGrpGenBookDeliveryBtn.strokeColor = mainColor
                    actGrpGenBookDeliveryBtn.backgroundTintList = paleColor
                    actGrpGenBookDeliveryBtn.setTextColor(mainColorInt)
                    actGrpGenBookDirectBtn.strokeColor = grey200
                    actGrpGenBookDirectBtn.backgroundTintList = white
                    actGrpGenBookDirectBtn.setTextColor(grey500Int)
                    actGrpGenDirectInputGroup.visibility = View.GONE
                }
                null -> {
                    actGrpGenBookDirectBtn.strokeColor = grey200
                    actGrpGenBookDirectBtn.backgroundTintList = white
                    actGrpGenBookDirectBtn.setTextColor(grey500Int)
                    actGrpGenBookDeliveryBtn.strokeColor = grey200
                    actGrpGenBookDeliveryBtn.backgroundTintList = white
                    actGrpGenBookDeliveryBtn.setTextColor(grey500Int)
                    actGrpGenDirectInputGroup.visibility = View.GONE
                }
            }
        }
    }

    private fun getTagInfoFromText(uiText: String): Pair<String, String> {
        return when {
            uiText.contains("메모환영") || uiText.contains("메모") -> "MEMO" to "METHOD"
            uiText.contains("포스트잇") -> "POSTIT" to "METHOD"
            uiText.contains("깔끔") -> "CLEAN" to "METHOD"
            uiText.contains("진지함") -> "SERIOUS" to "VIBE"
            uiText.contains("재미있게") -> "LIGHT_FUN" to "VIBE"
            uiText.contains("인사이트") -> "INSIGHT" to "VIBE"
            else -> "UNKNOWN" to "SPEED"
        }
    }

    private fun searchBooksFromApi(query: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().searchBooks(query, 1, 10)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val books = response.body()?.result?.books ?: emptyList()
                    if (books.isNotEmpty()) {
                        searchAdapter.submitList(books)
                        binding.actGrpGenBookSearchRv.visibility = View.VISIBLE
                    } else {
                        binding.actGrpGenBookSearchRv.visibility = View.GONE
                    }
                } else {
                    binding.actGrpGenBookSearchRv.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("SearchError", "검색 실패", e)
                binding.actGrpGenBookSearchRv.visibility = View.GONE
            }
        }
    }

    private fun createGroupApi() {
//        if (selectedIsbn.isEmpty()) {
//            Toast.makeText(this, "도서를 검색해서 선택해주세요.", Toast.LENGTH_SHORT).show()
//            return
//        }

        val durationStr = binding.actGrpGenBookLimitBar.text.toString()
        val capacityStr = binding.actGrpGenMemberCountEt.text.toString()
        val comment = binding.actGrpGenIntroduceBar.text.toString()
        val readingPeriod = durationStr.toIntOrNull() ?: 0
        val maxCapacity = if (groupType == "TOGETHER") (capacityStr.toIntOrNull() ?: 0) else 2

        // 태그 처리
        val finalTags = mutableListOf<GroupTagRequest>()
        var customTagString = ""
        if (isCustomTagSelected) {
            val directText = binding.actGrpGenDirectInputEt.text.toString().trim()
            if (directText.isNotEmpty()) {
                customTagString = directText
            }
        }

        // 1. 일반 칩 태그 수집
        val checkedIds = binding.actGrpGenChipGroup.checkedChipIds
        val tagMap = mutableMapOf<String, MutableList<String>>()

        for (id in checkedIds) {
            val chip = binding.actGrpGenChipGroup.findViewById<Chip>(id)
            val chipText = chip.text.toString()

            val (code, type) = getTagInfoFromText(chipText)
            if (code != "UNKNOWN") {
                val list = tagMap.getOrPut(type) { mutableListOf() }
                list.add(code)
            }
        }

        for ((type, codes) in tagMap) {
            finalTags.add(GroupTagRequest(type = type, value = codes))
        }

        val request = GroupCreateRequest(
            isbn13 = selectedIsbn,
            maxCapacity = maxCapacity,
            startDate = selectedDate ?: "",
            readingPeriod = readingPeriod,
            groupComment = comment,
            customTag = customTagString,
            groupType = groupType,
            tradeType = selectedTradeType ?: "NONE",
            tags = finalTags
        )

        if (isCustomTagSelected) {
            val directText = binding.actGrpGenDirectInputEt.text.toString().trim()
            if (directText.isNotEmpty()) {
                customTagString = directText
            }
        }
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().createGroup(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@GroupGenerationActivity, "그룹이 생성되었습니다!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_ERROR", "실패: $errorBody")
                    Toast.makeText(this@GroupGenerationActivity, "생성 실패: 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API_FAIL", "통신 오류", e)
                Toast.makeText(this@GroupGenerationActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initSearchAdapter() {
        searchAdapter = GrpSearchBookAdapter { bookItem ->
            isItemSelectMode = true
            binding.actGrpGenBookSearchBar.setText(bookItem.title)
            binding.actGrpGenBookSearchBar.setSelection(bookItem.title.length)
            selectedIsbn = bookItem.isbn13
            binding.actGrpGenBookSearchRv.visibility = View.GONE

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.actGrpGenBookSearchBar.windowToken, 0)
            binding.actGrpGenBookSearchBar.clearFocus()
            checkInputs()
        }

        binding.actGrpGenBookSearchRv.apply {
            layoutManager = LinearLayoutManager(this@GroupGenerationActivity)
            adapter = searchAdapter
            visibility = View.GONE
        }
    }

    private fun showCustomToast(message: String) {
        val inflater = LayoutInflater.from(this)
        val layout = inflater.inflate(R.layout.toast_custom, null)

        val textView = layout.findViewById<TextView>(R.id.toast_message_tv)
        textView.text = message

        with(Toast(applicationContext)) {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }
}