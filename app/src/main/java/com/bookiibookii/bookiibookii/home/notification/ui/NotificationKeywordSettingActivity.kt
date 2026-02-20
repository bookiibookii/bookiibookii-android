package com.bookiibookii.bookiibookii.home.notification.ui

import android.graphics.Typeface
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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.BaseActivity
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.KeywordSort
import com.bookiibookii.bookiibookii.databinding.ActivityNotificationKeywordSettingBinding
import com.bookiibookii.bookiibookii.home.notification.adapter.KeywordAdapter
import com.bookiibookii.bookiibookii.home.notification.data.KeywordRepository
import com.bookiibookii.bookiibookii.home.notification.vm.KeywordViewModel
import com.bookiibookii.bookiibookii.home.notification.vm.KeywordViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class NotificationKeywordSettingActivity : BaseActivity<ActivityNotificationKeywordSettingBinding>() {

    override fun getViewBinding(): ActivityNotificationKeywordSettingBinding {
        return ActivityNotificationKeywordSettingBinding.inflate(layoutInflater)
    }
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

    private var currentSort: String = KeywordSort.LATEST.name

    private lateinit var adapter: KeywordAdapter

    // 길이 초과 토스트 스팸 방지용
    private var hasShownLengthToast = false

    private val viewModel: KeywordViewModel by viewModels {
        val api = RetrofitClient.api()
        val repo = KeywordRepository(api)
        KeywordViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_notification_keyword_setting)

        bindViews()
        bindToolbar()
        setupRecycler()
        setupInputFilters()
        setupActions()

        renderSortUi(isLatest = true)

        // 첫 로딩
        viewModel.load(currentSort)

        // state 수신
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { s ->
                    adapter.submitList(s.items)
                    renderCount(s.items.size)
                    refreshUiState(s.items.size)

                    // TODO: 서버 에러 메시지 정책 확정 후 토스트 문구 통일
                    // if (!s.errorMessage.isNullOrBlank()) showCustomToast(s.errorMessage!!)
                }
            }
        }
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
            onDeleteClick = { item ->
                viewModel.delete(item.keywordId)
            }
        )

        rvKeyword.layoutManager = LinearLayoutManager(this)
        rvKeyword.adapter = adapter
    }

    private fun setupInputFilters() {
        // 공백/이모지(서로게이트) 차단 + 허용 문자만 통과
        val blockInvalidFilter = InputFilter { source, start, end, _, _, _ ->
            if (source.isEmpty()) return@InputFilter null // 삭제 허용

            // 한글 IME 조합(composing) 중이면 필터가 끼어들지 않게 통과
            val sp = source as? android.text.Spannable
            if (sp != null) {
                val composingStart = android.view.inputmethod.BaseInputConnection.getComposingSpanStart(sp)
                val composingEnd = android.view.inputmethod.BaseInputConnection.getComposingSpanEnd(sp)
                val isComposing = composingStart >= 0 && composingEnd >= 0
                if (isComposing) return@InputFilter null
            }

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

                refreshUiState(viewModel.state.value.items.size)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        tvSortLatest.setOnClickListener {
            if (currentSort != KeywordSort.LATEST.name) {
                currentSort = KeywordSort.LATEST.name
                renderSortUi(isLatest = true)
                viewModel.load(currentSort)
            }
        }

        tvSortAbc.setOnClickListener {
            if (currentSort != KeywordSort.ALPHABETICAL.name) {
                currentSort = KeywordSort.ALPHABETICAL.name
                renderSortUi(isLatest = false)
                viewModel.load(currentSort)
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

        val currentCount = viewModel.state.value.items.size

        // 최대 개수 초과
        if (currentCount >= maxCount) {
            showCustomToast("키워드는 최대 10개까지만 등록 가능합니다.")
            clearInput()
            refreshUiState(currentCount)
            return
        }

        // 중복 키워드
        val duplicated = viewModel.state.value.items.any { it.content.equals(input, ignoreCase = true) }
        if (duplicated) {
            showCustomToast("이미 등록된 키워드입니다.")
            clearInput()
            refreshUiState(currentCount)
            return
        }

        viewModel.add(input)

        clearInput()
        refreshUiState(currentCount)
    }

    private fun clearInput() {
        etKeyword.setText("")
        hasShownLengthToast = false
    }

    private fun renderCount(count: Int) {
        tvCountCurrent.text = count.toString()
        tvCountTotal.text = " / $maxCount 개"
    }

    private fun renderSortUi(isLatest: Boolean) {
        if (isLatest) {
            tvSortLatest.setTextColor(getColor(R.color.grey_700))
            tvSortLatest.setTypeface(null, Typeface.BOLD)

            tvSortAbc.setTextColor(getColor(R.color.grey_500))
            tvSortAbc.setTypeface(null, Typeface.NORMAL)
        } else {
            tvSortLatest.setTextColor(getColor(R.color.grey_500))
            tvSortLatest.setTypeface(null, Typeface.NORMAL)

            tvSortAbc.setTextColor(getColor(R.color.grey_700))
            tvSortAbc.setTypeface(null, Typeface.BOLD)
        }
    }

    private fun refreshUiState(count: Int) {
        val underLimit = count < maxCount
        val text = etKeyword.text.toString().trim()

        etKeyword.isEnabled = underLimit
        btnAdd.isEnabled = underLimit && text.isNotEmpty()
    }

    @Suppress("DEPRECATION")
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