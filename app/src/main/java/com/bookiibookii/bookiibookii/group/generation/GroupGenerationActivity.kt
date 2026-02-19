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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.BaseActivity
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

class GroupGenerationActivity :
    BaseActivity<ActivityGrpGenerationBinding>() {

    override fun getViewBinding(): ActivityGrpGenerationBinding {
        return ActivityGrpGenerationBinding.inflate(layoutInflater)
    }

    // --- 모드 및 데이터 변수 ---
    private var isEditMode = false
    private var currentGroupId = 0 // 수정 시 사용
    private var groupType = "RELAY" // RELAY or TOGETHER

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

    private var selectedBookTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // binding = ActivityGrpGenerationBinding.inflate(layoutInflater)
       // setContentView(binding.root)

        // 1. 인텐트 데이터 처리
        processIntentData()

        // 2. UI 초기 상태 설정 (Create vs Edit / Relay vs Together)
        initUiState()

        // 3. 리스너 등록 (클릭, 텍스트 변경 등)
        initListeners()
        initHashTagSystem() // 태그/칩 관련 리스너 분리
        initSearchSystem()  // 검색 리스너 분리

        // 4. 데이터 로딩 (수정 모드 채우기 OR 마이페이지 불러오기)
        loadInitialData()
    }

    override fun setupWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }


    //  1. 초기화 및 UI 설정
    private fun processIntentData() {
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        currentGroupId = intent.getIntExtra("GROUP_ID", 0)
        groupType = intent.getStringExtra("GROUP_TYPE") ?: "RELAY"
    }

    private fun initUiState() {
        with(binding) {
            // [A] 수정 모드 (Edit Mode)
            if (isEditMode) {
                actGrpGenMainTitleTv.text = "그룹 수정"
                actGrpGenRunBtn.text = "수정 완료"

                // 1. 책 검색 숨김 & 제목 표시
                actGrpGenBookSerachTv.visibility = View.GONE
                actGrpGenBookSerachStarTv.visibility = View.GONE
                actGrpGenBookSearchBar.visibility = View.GONE
                actGrpGenBookSearchRv.visibility = View.GONE
                actGrpGenEditBookTitleTv.visibility = View.VISIBLE
                actGrpGenEditBookTitleTv.text = intent.getStringExtra("BOOK_TITLE") ?: ""

                // 2. 수정 불가능한 영역 숨김 (책 소유, 거래 방식, 지역 입력)
                actGrpGenBookPossessionGroup.visibility = View.GONE // 책 소유 여부
                actGrpGenMethodContainer.visibility = View.GONE     // 거래 방식(택배/직거래)
                actGrpGenDirectInputGroup.visibility = View.GONE    // 지역 입력창
                actGrpGenMemberCountGroup.visibility = View.GONE    // 인원 설정

            }
            // [B] 생성 모드 (Create Mode)
            else {
                actGrpGenMainTitleTv.text = "그룹 만들기"
                actGrpGenRunBtn.text = "그룹 만들기"

                // 검색창 보이기
                actGrpGenBookSerachTv.visibility = View.VISIBLE
                actGrpGenBookSerachStarTv.visibility = View.VISIBLE
                actGrpGenBookSearchBar.visibility = View.VISIBLE
                actGrpGenEditBookTitleTv.visibility = View.GONE

                // 타입별 UI 분기 (Relay vs Together)
                val isRelay = (groupType == "RELAY")

                if (isRelay) {
                    actGrpGenMethodContainer.visibility = View.VISIBLE      // 거래 방식 보이기
                    actGrpGenBookPossessionGroup.visibility = View.VISIBLE  // 책 소유 여부 보이기
                    actGrpGenMemberCountGroup.visibility = View.GONE        // 인원 설정 숨김 (고정)
                } else {
                    // 같이 읽기
                    actGrpGenMethodContainer.visibility = View.GONE
                    actGrpGenDirectInputGroup.visibility = View.GONE
                    actGrpGenBookPossessionGroup.visibility = View.GONE
                    actGrpGenMemberCountGroup.visibility = View.VISIBLE     // 인원 설정 보이기
                }
            }
        }
    }

    private fun loadInitialData() {
        if (isEditMode) {
            fillEditData()
        }
//        else if (groupType == "RELAY") {
//            fetchMyPageDataAndPreFill()
//        }
    }

    // [API] 마이페이지 정보로 자동 채우기 (생성 시에만 사용)
    private fun fetchMyPageDataAndPreFill() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getMypage()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val profile = response.body()?.result
                    profile?.let { data ->
                        // 1. 데이터 확인
                        val hasRegion = !data.region.isNullOrBlank()
                        val hasPlace = !data.meetPlace.isNullOrBlank()

                        // 2. 텍스트 필드 채우기
                        if (hasRegion) binding.actGrpGenRegionEt.setText(data.region)
                        if (hasPlace) binding.actGrpGenPlaceEt.setText(data.meetPlace)

                        if (hasRegion || hasPlace) {
                            setTradeType("DIRECT")
                        } else {
                            // 데이터가 없으면 그냥 유효성 검사만 한번 수행
                            checkInputs()
                        }
                    }
                }
            } catch (e: Exception) {
                // 자동입력 실패는 조용히 무시
            }
        }
    }

    // [Logic] 수정 모드일 때 기존 데이터 채우기
    private fun fillEditData() {
        // 날짜
        val sDate = intent.getStringExtra("START_DATE")
        if (!sDate.isNullOrEmpty()) {
            selectedDate = sDate
            binding.actGrpGenBookSelectDateTv.text = sDate.replace("-", ".")
            binding.actGrpGenBookSelectDateTv.setTextColor(getColor(R.color.grey_900))
        }

        // 기간 & 소개글
        val period = intent.getIntExtra("PERIOD", 0)
        binding.actGrpGenBookLimitBar.setText(period.toString())

        val comment = intent.getStringExtra("COMMENT") ?: ""
        binding.actGrpGenIntroduceBar.setText(comment)

        // 커스텀 태그
        val customTag = intent.getStringExtra("CUSTOM_TAG")
        if (!customTag.isNullOrBlank()) {
            binding.actGrpGenDirectInputEt.setText("#$customTag")
            setCustomTagState(true)
        }

        // 일반 태그
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

    // ============================================================================================
    //  2. 리스너 설정
    // ============================================================================================

    private fun initListeners() {
        with(binding) {
            actGrpGenBackIv.setOnClickListener { finish() }

            // 텍스트 변경 감지
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

            // 소개글 포커스 아웃 경고
            actGrpGenIntroduceBar.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val input = actGrpGenIntroduceBar.text.toString()
                    if (input.isNotEmpty() && input.length < 10) {
                        showCustomToast("그룹 소개는 최소 10자 이상 입력해주세요.",false)
                    }
                }
            }

            actGrpGenBookDateContainer.setOnClickListener { showDatePicker() }

            // 거래 방식/책 소유 버튼 (생성 시에만 작동)
            actGrpGenBookDeliveryBtn.setOnClickListener { setTradeType("DELIVERY") }

            actGrpGenBookDirectBtn.setOnClickListener {
                setTradeType("DIRECT")      // 1. UI를 직거래 모드로 변경 (입력창 보이기)
                fetchMyPageDataAndPreFill() // 2. 마이페이지 정보 가져와서 채워넣기
            }
            actGrpGenBookYesBtn.setOnClickListener { updateBookHaveState(true); checkInputs() }
            actGrpGenBookNoBtn.setOnClickListener { updateBookHaveState(false); showBuyDialog() }

            // 실행 버튼
            actGrpGenRunBtn.setOnClickListener {
                val durationText = binding.actGrpGenBookLimitBar.text.toString().trim()
                val duration = durationText.toIntOrNull() ?: 0
                val comment = binding.actGrpGenIntroduceBar.text.toString().trim()
                val checkedTagIds = binding.actGrpGenChipGroup.checkedChipIds

                // [1] 수정 모드 (Edit Mode) 검증
                if (isEditMode) {
                    if (selectedDate.isNullOrEmpty()) {
                        showCustomToast("시작 날짜를 선택해주세요.", false)
                        return@setOnClickListener
                    }
                    if (durationText.isEmpty() || duration < 3 || duration > 30) {
                        showCustomToast("기간은 3일에서 30일 사이로 입력해주세요.", false)
                        return@setOnClickListener
                    }
                    if (checkedTagIds.isEmpty()) {
                        showCustomToast("독서 태그를 최소 1개 이상 선택해주세요.", false)
                        return@setOnClickListener
                    }
                    if (comment.length < 10) {
                        showCustomToast("그룹 소개는 최소 10자 이상 입력해주세요.", false)
                        return@setOnClickListener
                    }

                    modifyGroupApi()
                    return@setOnClickListener
                }

                // [2] 생성 모드 (Create Mode) 검증 - 공통 선행 조건 (도서 검색)
                if (selectedIsbn.isEmpty()) {
                    showCustomToast("먼저 읽을 책을 검색하여 선택해주세요.", false)
                    return@setOnClickListener
                }
                
                // [3] 모드별 상세 검증 (이어읽기 vs 함께읽기)

                if (groupType == "RELAY") {
                    // 이어읽기(RELAY) 순서: 책 소유 -> 교환방법 -> 시작날짜 -> 기간 -> 태그 -> 소개
                    if (selectedBookHave == null) {
                        showCustomToast("책 소유 여부를 선택해주세요.", false)
                        return@setOnClickListener
                    }
                    if (selectedTradeType == null) {
                        showCustomToast("교환 방식을 선택해주세요.", false)
                        return@setOnClickListener
                    }
                    if (selectedTradeType == "DIRECT") {
                        if (binding.actGrpGenRegionEt.text.toString().trim().isEmpty() ||
                            binding.actGrpGenPlaceEt.text.toString().trim().isEmpty()) {
                            showCustomToast("직접 교환할 지역과 장소를 입력해주세요.", false)
                            return@setOnClickListener
                        }
                    }
                    if (selectedDate.isNullOrEmpty()) {
                        showCustomToast("시작 날짜를 선택해주세요.", false)
                        return@setOnClickListener
                    }
                    if (durationText.isEmpty() || duration < 3 || duration > 30) {
                        showCustomToast("기간은 3일에서 30일 사이로 입력해주세요.", false)
                        return@setOnClickListener
                    }
                } else {
                    // 함께읽기(TOGETHER) 순서: 시작날짜 -> 기간 -> 최대인원 -> 태그 -> 소개
                    if (selectedDate.isNullOrEmpty()) {
                        showCustomToast("시작 날짜를 선택해주세요.", false)
                        return@setOnClickListener
                    }
                    if (durationText.isEmpty() || duration < 3 || duration > 30) {
                        showCustomToast("기간은 3일에서 30일 사이로 입력해주세요.", false)
                        return@setOnClickListener
                    }
                    val capacityText = binding.actGrpGenMemberCountEt.text.toString().trim()
                    val capacity = capacityText.toIntOrNull() ?: 0
                    if (capacityText.isEmpty() || capacity < 2 || capacity > 8) {
                        showCustomToast("모집 인원은 2명에서 8명 사이로 설정해주세요.", false)
                        return@setOnClickListener
                    }
                }

                // [공통 후행 조건] 태그 & 소개글
                if (checkedTagIds.isEmpty()) {
                    showCustomToast("독서 태그를 최소 1개 이상 선택해주세요.", false)
                    return@setOnClickListener
                }
                if (comment.length < 10) {
                    showCustomToast("그룹 소개는 최소 10자 이상 입력해주세요.", false)
                    return@setOnClickListener
                }

                // 모든 검증 통과 시 생성 API 호출
                createGroupApi()
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
            selectedBookTitle = bookItem.title

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
            checkInputs() // 상태가 바뀔 때마다 유효성 검사
        }

        val et = binding.actGrpGenDirectInputEt
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = et.text.toString()
                if (text.isEmpty() || text == "#") setCustomTagState(false)
                else if (!isCustomTagSelected) setCustomTagState(true)
                checkInputs() // 커스텀 태그 입력 시에도 유효성 검사
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

    // ============================================================================================
    //  3. 로직 및 유효성 검사
    // ============================================================================================

    private fun checkInputs() {
        val hasDate = !selectedDate.isNullOrEmpty()
        val hasPeriod = binding.actGrpGenBookLimitBar.text.toString().isNotEmpty()
        val commentText = binding.actGrpGenIntroduceBar.text.toString()
        val hasValidComment = commentText.length >= 10

        // ★ [핵심 수정] 커스텀 태그 여부와 상관없이 '기본 칩'이 최소 1개 있어야 함
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
                // hasValidTag(기본태그 필수) 포함
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

    // [생성]
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
                    showCustomToast("그룹 생성 완료되었습니다. ",true)
                    finish()
                }
            } catch (e: Exception) {
                showCustomToast("네트워크 오류가 발생했습니다.",false)
            }
        }
    }

    // [수정] - 날짜, 기간, 태그, 소개글만 전송
// [수정] - 날짜, 기간, 태그, 소개글만 전송
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
                // 1. API 호출
                val response = RetrofitClient.api().modifyGroup(currentGroupId.toLong(), request)

                // 2. 응답 처리
                if (response.isSuccessful) {
                    // 성공했을 때 (200 OK)
                    showCustomToast("그룹 정보가 정상적으로 수정 되었습니다",true)
                    finish()
                } else {
                    // 서버가 거절했을 때 (4xx, 5xx)
                    val errorString = response.errorBody()?.string()
                    val msg = try {
                        JSONObject(errorString ?: "{}").getString("message")
                    } catch (e: Exception) {
                        "수정에 실패했습니다."
                    }
                    showCustomToast(msg,false)
                }
            } catch (e: Exception) {
                // 3. 앱 내부 에러 (JSON 파싱 실패 등)
                e.printStackTrace()

                showCustomToast("오류: ${e.message}",false)

            }
        }
    }

    private fun handleApiResponse(isSuccess: Boolean, errorBody: String?, onSuccess: () -> Unit) {
        if (isSuccess) onSuccess()
        else {
            try {
                val msg = if (errorBody != null) JSONObject(errorBody).getString("message") else "실패했습니다."
                showCustomToast(msg,false)
            } catch (e: Exception) {
                showCustomToast("오류가 발생했습니다.",false)
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
        //binding.actGrpGenRunBtn.isEnabled = isEnabled

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
            subtitle = if (selectedBookTitle.isNotEmpty()) selectedBookTitle else "선택하신 도서",
            content = "실물 책이 필요합니다.\n구매페이지로 이동할까요?",
            confirmBtnText = "구매하러 가기",
            confirmBtnColor = R.color.grey_900,
            onConfirmClick = {
                updateBookHaveState(null)
                if (selectedBookLink.isNotEmpty()) {
                    lifecycleScope.launch {
                        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(selectedBookLink))) }
                        catch (e: Exception) { showCustomToast("링크 연결 실패",false) }
                    }
                } else showCustomToast("책 정보가 없습니다.",false)
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
        val layout = LayoutInflater.from(this).inflate(R.layout.toast_custom, null)
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