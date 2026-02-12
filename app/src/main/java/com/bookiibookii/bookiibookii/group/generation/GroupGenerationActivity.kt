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
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
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
import com.bookiibookii.bookiibookii.data.model.GroupItemDto
import com.bookiibookii.bookiibookii.data.model.GroupTagRequest
import com.bookiibookii.bookiibookii.databinding.ActivityGrpGenerationBinding
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

class GroupGenerationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpGenerationBinding

    // --- 모드 및 데이터 변수 ---
    private var isEditMode = false
    private var currentGroupId = 0
    private var groupType = "RELAY"

    // --- 검색 관련 변수 ---
    private lateinit var searchAdapter: GrpGenAladinSearchAdapter
    private var searchJob: Job? = null
    private var selectedIsbn: String = ""
    private var selectedBookLink: String = ""
    private var isItemSelectMode = false

    // --- 입력 상태 변수 ---
    private var selectedDate: String? = null
    private var selectedTradeType: String? = null
    private var selectedBookHave: Boolean? = null

    // --- 태그 관련 변수 ---
    private var previousCheckedIds: List<Int> = emptyList()
    private var isCustomTagSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrpGenerationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        processIntentData()
        initUiState()
        initListeners()
        initHashTagSystem()
        initSearchSystem()
        loadInitialData()
    }

    //  1. 초기화 및 UI 설정

    private fun processIntentData() {
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        currentGroupId = intent.getIntExtra("GROUP_ID", 0)
        groupType = intent.getStringExtra("GROUP_TYPE") ?: "RELAY"
    }

    private fun initUiState() {
        with(binding) {
            if (isEditMode) {
                actGrpGenMainTitleTv.text = "그룹 수정"
                actGrpGenRunBtn.text = "수정 완료"

                actGrpGenBookSerachTv.visibility = View.GONE
                actGrpGenBookSerachStarTv.visibility = View.GONE
                actGrpGenBookSearchBar.visibility = View.GONE
                actGrpGenBookSearchRv.visibility = View.GONE
                actGrpGenEditBookTitleTv.visibility = View.VISIBLE
                actGrpGenEditBookTitleTv.text = intent.getStringExtra("BOOK_TITLE") ?: ""

                actGrpGenBookPossessionGroup.visibility = View.GONE
                actGrpGenMethodContainer.visibility = View.GONE
                actGrpGenDirectInputGroup.visibility = View.GONE
                actGrpGenMemberCountGroup.visibility = View.GONE

            } else {
                actGrpGenMainTitleTv.text = "그룹 만들기"
                actGrpGenRunBtn.text = "그룹 만들기"

                actGrpGenBookSerachTv.visibility = View.VISIBLE
                actGrpGenBookSerachStarTv.visibility = View.VISIBLE
                actGrpGenBookSearchBar.visibility = View.VISIBLE
                actGrpGenEditBookTitleTv.visibility = View.GONE

                val isRelay = (groupType == "RELAY")

                if (isRelay) {
                    actGrpGenMethodContainer.visibility = View.VISIBLE
                    actGrpGenBookPossessionGroup.visibility = View.VISIBLE
                    actGrpGenMemberCountGroup.visibility = View.GONE
                } else {
                    actGrpGenMethodContainer.visibility = View.GONE
                    actGrpGenDirectInputGroup.visibility = View.GONE
                    actGrpGenBookPossessionGroup.visibility = View.GONE
                    actGrpGenMemberCountGroup.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadInitialData() {
        if (isEditMode) {
            fillEditData()
        }
    }

    private fun fetchMyPageDataAndPreFill() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getMypage()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val profile = response.body()?.result
                    profile?.let { data ->
                        val hasRegion = !data.region.isNullOrBlank()
                        val hasPlace = !data.meetPlace.isNullOrBlank()

                        if (hasRegion) binding.actGrpGenRegionEt.setText(data.region)
                        if (hasPlace) binding.actGrpGenPlaceEt.setText(data.meetPlace)

                        if (hasRegion || hasPlace) {
                            setTradeType("DIRECT")
                        } else {
                            checkInputs()
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun fillEditData() {
        val sDate = intent.getStringExtra("START_DATE")
        if (!sDate.isNullOrEmpty()) {
            selectedDate = sDate
            binding.actGrpGenBookSelectDateTv.text = sDate.replace("-", ".")
            binding.actGrpGenBookSelectDateTv.setTextColor(getColor(R.color.grey_900))
        }

        val period = intent.getIntExtra("PERIOD", 0)
        binding.actGrpGenBookLimitBar.setText(period.toString())

        val comment = intent.getStringExtra("COMMENT") ?: ""
        binding.actGrpGenIntroduceBar.setText(comment)

        val customTag = intent.getStringExtra("CUSTOM_TAG")
        if (!customTag.isNullOrBlank()) {
            binding.actGrpGenDirectInputEt.setText("#$customTag")
            setCustomTagState(true)
        }

        val tags = intent.getStringArrayListExtra("TAGS")
        tags?.forEach { code -> checkChipByCode(code) }

        checkInputs()
    }

    private fun checkChipByCode(code: String) {
        val chipId = when(code) {
            "MEMO" -> R.id.act_grp_gen_tag_memo_cp
            "POSTIT" -> R.id.act_grp_gen_tag_post_cp
            "CLEAN" -> R.id.act_grp_gen_tag_clean_cp
            "SERIOUS" -> R.id.act_grp_gen_tag_serious_cp
            "LIGHT_FUN" -> R.id.act_grp_gen_tag_fun_cp
            "INSIGHT" -> R.id.act_grp_gen_tag_insight_cp
            else -> null
        }
        chipId?.let { binding.actGrpGenChipGroup.check(it) }
    }

    //  2. 리스너 설정

    private fun initListeners() {
        with(binding) {
            actGrpGenBackIv.setOnClickListener { finish() }

            val commonTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { checkInputs() }
                override fun afterTextChanged(s: Editable?) {}
            }

            actGrpGenBookLimitBar.addTextChangedListener(commonTextWatcher)
            actGrpGenMemberCountEt.addTextChangedListener(commonTextWatcher)
            actGrpGenIntroduceBar.addTextChangedListener(commonTextWatcher)
            actGrpGenRegionEt.addTextChangedListener(commonTextWatcher)
            actGrpGenPlaceEt.addTextChangedListener(commonTextWatcher)

            actGrpGenIntroduceBar.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val input = actGrpGenIntroduceBar.text.toString()
                    if (input.isNotEmpty() && input.length < 10) {
                        showCustomToast("그룹 소개는 최소 10자 이상 입력해주세요.", false)
                    }
                }
            }

            actGrpGenBookDateContainer.setOnClickListener { showDatePicker() }

            actGrpGenBookDeliveryBtn.setOnClickListener { setTradeType("DELIVERY") }

            actGrpGenBookDirectBtn.setOnClickListener {
                setTradeType("DIRECT")
                fetchMyPageDataAndPreFill()
            }
            actGrpGenBookYesBtn.setOnClickListener { updateBookHaveState(true); checkInputs() }
            actGrpGenBookNoBtn.setOnClickListener { updateBookHaveState(false); showBuyDialog() }

            actGrpGenRunBtn.setOnClickListener {
                if (!it.isEnabled) {
                    if (binding.actGrpGenChipGroup.checkedChipIds.isEmpty()) {
                        showCustomToast("기본 태그를 최소 1개 이상 선택해주세요.", false)
                    }
                    return@setOnClickListener
                }
                if (isEditMode) modifyGroupApi() else createGroupApi()
            }
        }
    }

    private fun initSearchSystem() {
        searchAdapter = GrpGenAladinSearchAdapter { bookItem ->
            isItemSelectMode = true
            binding.actGrpGenBookSearchBar.setText(bookItem.title)
            binding.actGrpGenBookSearchBar.setSelection(bookItem.title.length)

            selectedIsbn = bookItem.isbn13
            selectedBookLink = bookItem.link

            binding.actGrpGenBookSearchRv.visibility = View.GONE
            hideKeyboard(binding.actGrpGenBookSearchBar)
            binding.actGrpGenBookSearchBar.clearFocus()
            checkInputs()
        }

        binding.actGrpGenBookSearchRv.layoutManager = LinearLayoutManager(this)
        binding.actGrpGenBookSearchRv.adapter = searchAdapter

        binding.actGrpGenBookSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isItemSelectMode) selectedIsbn = ""
                checkInputs()
            }
            override fun afterTextChanged(s: Editable?) {
                if (isItemSelectMode) { isItemSelectMode = false; return }
                val query = s.toString().trim()
                searchJob?.cancel()
                if (query.length >= 2) {
                    searchJob = lifecycleScope.launch {
                        delay(500L)
                        searchBooksFromApi(query)
                    }
                } else {
                    binding.actGrpGenBookSearchRv.visibility = View.GONE
                }
            }
        })
    }

    private fun initHashTagSystem() {
        binding.actGrpGenDirectInputEt.filters = arrayOf(
            InputFilter { source, _, _, _, _, _ -> if (source.contains(" ")) "" else null },
            InputFilter.LengthFilter(8)
        )

        binding.actGrpGenChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            previousCheckedIds = checkedIds
            checkInputs()
        }

        val et = binding.actGrpGenDirectInputEt
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = et.text.toString()
                if (text.isEmpty() || text == "#") setCustomTagState(false)
                else if (!isCustomTagSelected) setCustomTagState(true)
                checkInputs()
            }
        })

        et.setOnFocusChangeListener { _, hasFocus ->
            val text = et.text.toString()
            if (hasFocus) {
                if (text.startsWith("#")) et.setText(text.removePrefix("#"))
            } else {
                if (text.isNotEmpty() && !text.startsWith("#")) et.setText("#$text")
                else if (text.isEmpty() || text == "#") {
                    et.setText("")
                    setCustomTagState(false)
                }
            }
        }

        et.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                et.clearFocus()
                hideKeyboard(v)
                true
            } else false
        }
    }

    //  3. 로직 및 유효성 검사

    private fun checkInputs() {
        val hasDate = !selectedDate.isNullOrEmpty()
        val hasPeriod = binding.actGrpGenBookLimitBar.text.toString().isNotEmpty()
        val commentText = binding.actGrpGenIntroduceBar.text.toString()
        val hasValidComment = commentText.length >= 10
        val hasValidTag = binding.actGrpGenChipGroup.checkedChipIds.isNotEmpty()

        var isValid = false

        if (isEditMode) {
            isValid = hasDate && hasPeriod && hasValidComment && hasValidTag
        } else {
            val hasIsbn = selectedIsbn.isNotEmpty()
            if (groupType == "RELAY") {
                val hasPossession = (selectedBookHave != null)
                val hasTradeType = (selectedTradeType != null)
                val isDirect = (selectedTradeType == "DIRECT")
                val hasDirectLocation = if (!isDirect) true else {
                    binding.actGrpGenRegionEt.text.toString().isNotEmpty() &&
                            binding.actGrpGenPlaceEt.text.toString().isNotEmpty()
                }
                isValid = hasIsbn && hasDate && hasPeriod && hasPossession &&
                        hasTradeType && hasDirectLocation && hasValidComment && hasValidTag
            } else {
                val hasCapacity = binding.actGrpGenMemberCountEt.text.toString().isNotEmpty()
                isValid = hasIsbn && hasDate && hasPeriod && hasCapacity &&
                        hasValidComment && hasValidTag
            }
        }

        updateRunButtonState(isValid)
    }

    private fun getCombinedTags(): Pair<List<GroupTagRequest>, String?> {
        val finalTags = mutableListOf<GroupTagRequest>()
        val tagMap = mutableMapOf<String, MutableList<String>>()

        binding.actGrpGenChipGroup.checkedChipIds.forEach { id ->
            val chip = binding.actGrpGenChipGroup.findViewById<Chip>(id)
            val (code, type) = getTagCodeAndType(chip.text.toString())
            if (code != "UNKNOWN") {
                tagMap.getOrPut(type) { mutableListOf() }.add(code)
            }
        }
        for ((type, codes) in tagMap) {
            finalTags.add(GroupTagRequest(type, codes))
        }

        var customTagString: String? = null
        if (isCustomTagSelected) {
            val raw = binding.actGrpGenDirectInputEt.text.toString().trim()
            if (raw.isNotEmpty()) customTagString = raw.removePrefix("#")
        }
        return Pair(finalTags, customTagString)
    }

    private fun getTagCodeAndType(uiText: String): Pair<String, String> {
        return when {
            uiText.contains("메모") -> "MEMO" to "METHOD"
            uiText.contains("포스트잇") -> "POSTIT" to "METHOD"
            uiText.contains("깔끔") -> "CLEAN" to "METHOD"
            uiText.contains("진지함") -> "SERIOUS" to "VIBE"
            uiText.contains("재미있게") -> "LIGHT_FUN" to "VIBE"
            uiText.contains("인사이트") -> "INSIGHT" to "VIBE"
            else -> "UNKNOWN" to "UNKNOWN"
        }
    }

    //  4. API 호출

    private suspend fun searchBooksFromApi(query: String) {
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
            Log.e("GrpGen", "검색 에러", e)
        }
    }

    private fun createGroupApi() {
        val (finalTags, customTagString) = getCombinedTags()
        val duration = binding.actGrpGenBookLimitBar.text.toString().toIntOrNull() ?: 0
        val capacityStr = binding.actGrpGenMemberCountEt.text.toString()
        val maxCapacity = if (groupType == "TOGETHER") (capacityStr.toIntOrNull() ?: 0) else 2

        val regionText = binding.actGrpGenRegionEt.text.toString()
        val placeText = binding.actGrpGenPlaceEt.text.toString()

        val request = GroupCreateRequest(
            isbn13 = selectedIsbn,
            maxCapacity = maxCapacity,
            startDate = selectedDate ?: "",
            readingPeriod = duration,
            groupComment = binding.actGrpGenIntroduceBar.text.toString(),
            customTag = customTagString ?: "",
            groupType = groupType,
            tradeType = selectedTradeType ?: "NONE",
            preferRegion = regionText,
            meetPlace = placeText,
            tags = finalTags
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().createGroup(request)
                handleApiResponse(response.isSuccessful, response.errorBody()?.string()) {
                    showCustomToast(" 그룹 생성 완료 하였습니다.", true)
                    finish()
                }
            } catch (e: Exception) {
                showCustomToast("네트워크 오류가 발생했습니다.", false)
            }
        }
    }

    private fun modifyGroupApi() {
        val (finalTags, customTagString) = getCombinedTags()
        val duration = binding.actGrpGenBookLimitBar.text.toString().toIntOrNull() ?: 0

        val request = GroupItemDto.GroupModifyRequest(
            startDate = selectedDate ?: "",
            readingPeriod = duration,
            groupComment = binding.actGrpGenIntroduceBar.text.toString(),
            customTag = customTagString,
            tags = finalTags
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().modifyGroup(currentGroupId.toLong(), request)

                if (response.isSuccessful) {
                    showCustomToast("그룹 정보가 수정되었습니다", true)
                    finish()
                } else {
                    val errorString = response.errorBody()?.string()
                    val msg = try {
                        JSONObject(errorString ?: "{}").getString("message")
                    } catch (e: Exception) {
                        "수정에 실패했습니다."
                    }
                    showCustomToast(msg, false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showCustomToast("오류: ${e.message}", false)
            }
        }
    }

    private fun handleApiResponse(isSuccess: Boolean, errorBody: String?, onSuccess: () -> Unit) {
        if (isSuccess) onSuccess()
        else {
            try {
                val msg = if (errorBody != null) JSONObject(errorBody).getString("message") else "실패했습니다."
                showCustomToast(msg, false)
            } catch (e: Exception) {
                showCustomToast("오류가 발생했습니다.", false)
            }
        }
    }

   //  5. 유틸리티
    private fun showDatePicker() {
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
            binding.actGrpGenBookSelectDateTv.text = sdfUi.format(Date(selection))
            binding.actGrpGenBookSelectDateTv.setTextColor(getColor(R.color.grey_900))

            val sdfServer = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            selectedDate = sdfServer.format(Date(selection))
            checkInputs()
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun setTradeType(type: String) {
        selectedTradeType = type
        updateExchangeMethodState(type == "DIRECT")
        checkInputs()
    }

    private fun setCustomTagState(isSelected: Boolean) {
        isCustomTagSelected = isSelected
        binding.actGrpGenDirectInputEt.isSelected = isSelected
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
            content = "실물 책이 필요합니다.\n구매페이지로 이동할까요?",
            confirmBtnText = "구매하러 가기",
            confirmBtnColor = R.color.grey_900,
            onConfirmClick = {
                updateBookHaveState(null)
                if (selectedBookLink.isNotEmpty()) {
                    lifecycleScope.launch {
                        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(selectedBookLink))) }
                        catch (e: Exception) { showCustomToast("링크 연결 실패", false) }
                    }
                } else showCustomToast("링크가 없습니다.", false)
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
            if (isHave == true) {
                actGrpGenBookYesBtn.apply { strokeColor = mainColor; backgroundTintList = paleColor; setTextColor(mainColorInt) }
                actGrpGenBookNoBtn.apply { strokeColor = grey200; backgroundTintList = white; setTextColor(grey900Int) }
            } else if (isHave == false) {
                actGrpGenBookNoBtn.apply { strokeColor = mainColor; backgroundTintList = paleColor; setTextColor(mainColorInt) }
                actGrpGenBookYesBtn.apply { strokeColor = grey200; backgroundTintList = white; setTextColor(grey900Int) }
            } else {
                actGrpGenBookYesBtn.apply { strokeColor = grey200; backgroundTintList = white; setTextColor(grey900Int) }
                actGrpGenBookNoBtn.apply { strokeColor = grey200; backgroundTintList = white; setTextColor(grey900Int) }
            }
        }
    }

    private fun updateExchangeMethodState(isDirect: Boolean) {
        val mainColor = ContextCompat.getColorStateList(this, R.color.pre_main)
        val paleColor = ContextCompat.getColorStateList(this, R.color.pre_main_pale)
        val grey200 = ContextCompat.getColorStateList(this, R.color.grey_200)
        val white = ContextCompat.getColorStateList(this, R.color.white)
        val mainColorInt = ContextCompat.getColor(this, R.color.pre_main)
        val grey500Int = ContextCompat.getColor(this, R.color.grey_500)

        with(binding) {
            if (isDirect) {
                actGrpGenBookDirectBtn.apply { strokeColor = mainColor; backgroundTintList = paleColor; setTextColor(mainColorInt) }
                actGrpGenBookDeliveryBtn.apply { strokeColor = grey200; backgroundTintList = white; setTextColor(grey500Int) }
                actGrpGenDirectInputGroup.visibility = View.VISIBLE
            } else {
                actGrpGenBookDeliveryBtn.apply { strokeColor = mainColor; backgroundTintList = paleColor; setTextColor(mainColorInt) }
                actGrpGenBookDirectBtn.apply { strokeColor = grey200; backgroundTintList = white; setTextColor(grey500Int) }
                actGrpGenDirectInputGroup.visibility = View.GONE
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showCustomToast(message: String, isSuccess: Boolean) {
        val inflater = LayoutInflater.from(this)
        val layout = inflater.inflate(R.layout.toast_custom, null)
        layout.findViewById<TextView>(R.id.toast_message_tv).text = message
        val iconRes = if (isSuccess) R.drawable.ic_check else R.drawable.ic_info
        layout.findViewById<ImageView>(R.id.toast_icon_iv).setImageResource(iconRes)

        with(Toast(applicationContext)) {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }
}