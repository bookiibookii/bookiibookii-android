package com.bookiibookii.bookiibookii.home.notiSetting

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class HomKeywordNotiSettingActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar

    private lateinit var etKeyword: EditText
    private lateinit var btnAdd: MaterialButton

    private lateinit var tvCountCurrent: TextView
    private lateinit var tvCountTotal: TextView

    private lateinit var tvSortLatest: TextView
    private lateinit var tvSortAbc: TextView

    private lateinit var rvKeyword: RecyclerView

    private val maxCount = 10
    private val maxLen = 10 // “10자 이상(초과) 시 토스트” 기준

    // 허용: 한글/영문/숫자/?, !, ,, ., _, -
    private val allowedRegex = Regex("^[가-힣A-Za-z0-9?!,._\\-]+$")

    private var isLatestSort = true
    private val keywords = mutableListOf<String>()

    private lateinit var adapter: KeywordAdapter

    // 길이 초과 토스트 스팸 방지용
    private var hasShownLengthToast = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hom_keyword_noti_setting)

        bindViews()
        bindToolbar()
        setupRecycler()
        setupInputFilters()
        setupActions()

        renderCount()
        renderSortUi()
        refreshUiState()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)

        etKeyword = findViewById(R.id.et_keyword)
        btnAdd = findViewById(R.id.btn_add)

        tvCountCurrent = findViewById(R.id.tv_count_current)
        tvCountTotal = findViewById(R.id.tv_count_total)

        tvSortLatest = findViewById(R.id.tv_sort_latest)
        tvSortAbc = findViewById(R.id.tv_sort_abc)

        rvKeyword = findViewById(R.id.rv_keyword)
    }

    private fun bindToolbar() {
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecycler() {
        adapter = KeywordAdapter(
            onDeleteClick = { keyword -> removeKeyword(keyword) }
        )

        rvKeyword.layoutManager = LinearLayoutManager(this)
        rvKeyword.adapter = adapter
        adapter.submitList(getSortedList())
    }

    private fun setupInputFilters() {
        // 길이 제한 필터는 제거 (입력이 막히면 안 됨)

        // 공백/이모지(서로게이트) 차단 + 허용 문자만 통과
        val blockInvalidFilter = InputFilter { source, start, end, _, _, _ ->
            if (source.isEmpty()) return@InputFilter null // 삭제 허용

            for (i in start until end) {
                val ch = source[i]

                // 공백 금지
                if (ch.isWhitespace()) return@InputFilter ""

                // 이모지 등(서로게이트) 금지
                if (Character.isSurrogate(ch)) return@InputFilter ""

                // 허용 문자 외 금지
                val s = ch.toString()
                if (!allowedRegex.matches(s)) return@InputFilter ""
            }
            null
        }

        etKeyword.filters = arrayOf(blockInvalidFilter)
    }

    private fun setupActions() {
        btnAdd.setOnClickListener { tryAddKeyword() }

        etKeyword.setOnEditorActionListener { _, actionId, event ->
            val isDone = actionId == EditorInfo.IME_ACTION_DONE
            val isEnter =
                event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER

            if (isDone || isEnter) {
                tryAddKeyword()
                true
            } else {
                false
            }
        }

        etKeyword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString().orEmpty()

                // 10자 초과 시 입력은 허용하되 토스트만 노출
                if (text.length > maxLen) {
                    if (!hasShownLengthToast) {
                        showCustomToast("키워드는 최대 ${maxLen}자까지 입력할 수 있어요.")
                        hasShownLengthToast = true
                    }
                } else {
                    hasShownLengthToast = false
                }

                refreshUiState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        tvSortLatest.setOnClickListener {
            if (!isLatestSort) {
                isLatestSort = true
                renderSortUi()
                adapter.submitList(getSortedList())
            }
        }

        tvSortAbc.setOnClickListener {
            if (isLatestSort) {
                isLatestSort = false
                renderSortUi()
                adapter.submitList(getSortedList())
            }
        }
    }

    private fun tryAddKeyword() {
        val input = etKeyword.text.toString().trim()

        // 빈 값: 스펙상 무반응
        if (input.isEmpty()) return

        // 10자 초과: 입력은 되지만 추가는 막고 토스트
        if (input.length > maxLen) {
            showCustomToast("키워드는 최대 ${maxLen}자까지 입력할 수 있어요.")
            return
        }

        // 최대 개수 초과
        if (keywords.size >= maxCount) {
            showCustomToast("키워드는 최대 10개까지만 등록 가능합니다.")
            clearInput()
            refreshUiState()
            return
        }

        // 중복 키워드
        val duplicated = keywords.any { it.equals(input, ignoreCase = true) }
        if (duplicated) {
            showCustomToast("이미 등록된 키워드입니다.")
            clearInput()
            refreshUiState()
            return
        }

        keywords.add(input)

        clearInput()
        renderCount()
        adapter.submitList(getSortedList())
        refreshUiState()
    }

    private fun removeKeyword(keyword: String) {
        for (i in 0 until keywords.size) {
            if (keywords[i] == keyword) {
                keywords.removeAt(i)
                break
            }
        }

        renderCount()
        adapter.submitList(getSortedList())
        refreshUiState()
    }

    private fun clearInput() {
        etKeyword.setText("")
        hasShownLengthToast = false
    }

    private fun renderCount() {
        tvCountCurrent.text = keywords.size.toString()
        tvCountTotal.text = " / $maxCount 개"
    }

    private fun renderSortUi() {
        if (isLatestSort) {
            tvSortLatest.setTextColor(getColor(R.color.grey_700))
            tvSortLatest.setTypeface(null, android.graphics.Typeface.BOLD)

            tvSortAbc.setTextColor(getColor(R.color.grey_500))
            tvSortAbc.setTypeface(null, android.graphics.Typeface.NORMAL)
        } else {
            tvSortLatest.setTextColor(getColor(R.color.grey_500))
            tvSortLatest.setTypeface(null, android.graphics.Typeface.NORMAL)

            tvSortAbc.setTextColor(getColor(R.color.grey_700))
            tvSortAbc.setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }

    private fun refreshUiState() {
        val underLimit = keywords.size < maxCount
        val text = etKeyword.text.toString().trim()

        etKeyword.isEnabled = underLimit
        btnAdd.isEnabled = underLimit && text.isNotEmpty()
    }

    private fun getSortedList(): List<String> {
        return if (isLatestSort) {
            keywords.asReversed()
        } else {
            val sorted = keywords.toMutableList()
            sorted.sort()
            sorted
        }
    }

    private fun showCustomToast(message: String) {
        val inflater = LayoutInflater.from(this)
        val view: View = inflater.inflate(R.layout.toast_custom, null)
        val tv = view.findViewById<TextView>(R.id.toast_message_tv)
        tv.text = message

        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            this.view = view
            show()
        }
    }
}