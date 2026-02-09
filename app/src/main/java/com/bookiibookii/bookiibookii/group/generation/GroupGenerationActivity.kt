package com.bookiibookii.bookiibookii.group.generation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
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
// import com.bookiibookii.bookiibookii.group.generation.GrpGenAladinSearchAdapter // 같은 패키지면 import 생략 가능
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.iterator

class GroupGenerationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpGenerationBinding

    private var isEditMode = false
    private var groupType = "RELAY"

    private lateinit var searchAdapter: GrpGenAladinSearchAdapter
    private var searchJob: Job? = null
    private var selectedIsbn: String = ""
    private var selectedBookLink: String = ""

    // 상태 변수
    private var selectedDate: String? = null
    private var selectedTradeType: String? = null
    private var selectedBookHave: Boolean? = null
    private var isItemSelectMode = false

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

        // ★ [추가] 그룹 생성 모드일 때, 사용자 정보(지역/장소) 미리 채우기
        if (!isEditMode && groupType == "RELAY") {
            fetchMyPageDataAndPreFill()
        }
    }

    // ★ [추가] 마이페이지 정보 불러와서 EditText에 채워넣는 함수
    private fun fetchMyPageDataAndPreFill() {
        lifecycleScope.launch {
            try {
                // 마이페이지 조회 API 호출 (Endpoint 이름은 실제 API에 맞춰주세요. 예: getMyPage())
                val response = RetrofitClient.api().getMypage()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val profile = response.body()?.result

                    profile?.let { data ->
                        // 1. 활동 지역 (address 혹은 region 필드 사용)
                        // data.address가 "서울시 강남구 ..." 형태라면 그대로 넣거나 가공해서 넣음
                        val userRegion = data.region
                        if (!userRegion.isNullOrBlank()) {
                            binding.actGrpGenRegionEt.setText(userRegion)
                        }

                        // 2. 직거래 선호 장소
                        val userMeetPlace = data.meetPlace
                        if (!userMeetPlace.isNullOrBlank()) {
                            binding.actGrpGenPlaceEt.setText(userMeetPlace)
                        }

                        // 3. 데이터가 채워졌으니 유효성 검사 갱신 (버튼 활성화를 위해)
                        checkInputs()
                    }
                }
            } catch (e: Exception) {
                Log.e("GrpGen", "유저 정보 불러오기 실패 (자동입력 건너뜀)", e)
                // 실패해도 치명적이지 않으므로 조용히 넘어감 (사용자가 직접 입력하면 됨)
            }
        }
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

    private fun initHashTagChipListener() {
        val spaceFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.contains(" ")) "" else null
        }
        binding.actGrpGenDirectInputEt.filters = arrayOf(spaceFilter, InputFilter.LengthFilter(8))

        binding.actGrpGenChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val chipcurrentCount = checkedIds.size + (if (isCustomTagSelected) 1 else 0)

            if (chipcurrentCount > 3) {
                val newlyAddedIds = checkedIds - previousCheckedIds.toSet()
                if (newlyAddedIds.isNotEmpty()) {
                    val idToUncheck = newlyAddedIds.first()
                    binding.actGrpGenChipGroup.post {
                        group.findViewById<Chip>(idToUncheck)?.isChecked = false
                        showCustomToast("태그는 최대 3개까지만 선택 가능합니다.")
                    }
                }
                return@setOnCheckedStateChangeListener
            }
            previousCheckedIds = checkedIds
            checkInputs()
        }
    }

    private fun initDirectInputListener() {
        val et = binding.actGrpGenDirectInputEt

        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val currentText = et.text.toString()
                if (currentText.isEmpty() || currentText == "#") {
                    setCustomTagState(false)
                } else {
                    if (!isCustomTagSelected) setCustomTagState(true)
                }
                checkInputs()
            }
        })

        et.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val chipCount = binding.actGrpGenChipGroup.checkedChipIds.size
                if (chipCount >= 3 && !isCustomTagSelected) {
                    et.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    showCustomToast("태그는 최대 3개까지만 선택 가능합니다.")
                    return@setOnTouchListener true
                }
                val text = et.text.toString()
                if (text.isNotEmpty() && text != "#") {
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

        et.setOnFocusChangeListener { _, hasFocus ->
            val currentText = et.text.toString()
            if (hasFocus) {
                if (currentText.startsWith("#")) {
                    et.setText(currentText.removePrefix("#"))
                }
                val chipCount = binding.actGrpGenChipGroup.checkedChipIds.size
                if (!isCustomTagSelected && chipCount >= 3) {
                    et.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(et.windowToken, 0)
                    showCustomToast("태그는 최대 3개까지만 선택 가능합니다.")
                }
            } else {
                if (currentText.isNotEmpty() && !currentText.startsWith("#")) {
                    et.setText("#$currentText")
                } else if (currentText == "#" || currentText.isEmpty()) {
                    et.setText("")
                    setCustomTagState(false)
                }
            }
        }

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

    private fun setCustomTagState(isSelected: Boolean) {
        isCustomTagSelected = isSelected
        binding.actGrpGenDirectInputEt.isSelected = isSelected
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

            actGrpGenIntroduceBar.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val input = actGrpGenIntroduceBar.text.toString()
                    if (input.isNotEmpty() && input.length < 10) {
                        showCustomToast("그룹 소개는 최소 10자 이상 입력해주세요.")
                    }
                }
            }

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

        val hasValidTag = binding.actGrpGenChipGroup.checkedChipIds.isNotEmpty() || isCustomTagSelected

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
                if (selectedBookLink.isNotEmpty()) {
                    showCustomToast("구매페이지로 이동합니다")
                    lifecycleScope.launch {
                        delay(800)
                        try {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(selectedBookLink)
                            startActivity(intent)
                        } catch (e: Exception) {
                            showCustomToast("링크를 여는데 실패했습니다.")
                        }
                    }
                } else {
                    showCustomToast("구매 링크 정보가 없습니다.")
                }
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
        if (selectedIsbn.isEmpty()) {
            Toast.makeText(this, "도서를 검색해서 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val durationStr = binding.actGrpGenBookLimitBar.text.toString()
        val capacityStr = binding.actGrpGenMemberCountEt.text.toString()
        val comment = binding.actGrpGenIntroduceBar.text.toString()
        val readingPeriod = durationStr.toIntOrNull() ?: 0
        val maxCapacity = if (groupType == "TOGETHER") (capacityStr.toIntOrNull() ?: 0) else 2

        val finalTags = mutableListOf<GroupTagRequest>()
        var customTagString = ""

        // 1. 일반 칩 태그
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

        // 2. 직접 입력 태그
        if (isCustomTagSelected) {
            val directText = binding.actGrpGenDirectInputEt.text.toString().trim()
            if (directText.isNotEmpty()) {
                customTagString = directText.removePrefix("#")
            }
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

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().createGroup(request)
                if (response.isSuccessful) {
                    showCustomToast("🎉그룹 생성 완료🎉")
                    finish()
                } else {
                    val errorString = response.errorBody()?.string()
                    Log.e("API_ERROR", "실패 raw: $errorString")
                    try {
                        if (!errorString.isNullOrEmpty()) {
                            val jsonObject = JSONObject(errorString)
                            val serverMessage = jsonObject.getString("message")
                            showCustomToast(serverMessage)
                        } else {
                            showCustomToast("알 수 없는 오류가 발생했습니다.")
                        }
                    } catch (e: Exception) {
                        Log.e("API_PARSING", "JSON 파싱 실패", e)
                        showCustomToast("잠시 후 다시 시도해주세요.")
                    }
                }
            } catch (e: Exception) {
                Log.e("API_FAIL", "통신 오류", e)
                showCustomToast("네트워크 오류가 발생했습니다.")
            }
        }
    }

    private fun initSearchAdapter() {
        searchAdapter = GrpGenAladinSearchAdapter { bookItem ->
            isItemSelectMode = true
            binding.actGrpGenBookSearchBar.setText(bookItem.title)
            binding.actGrpGenBookSearchBar.setSelection(bookItem.title.length)

            selectedIsbn = bookItem.isbn13
            selectedBookLink = bookItem.link

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