package com.bookiibookii.bookiibookii.group.search

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.SearchHistoryManager
import com.bookiibookii.bookiibookii.databinding.ActivityGrpSearchBinding
import com.bookiibookii.bookiibookii.group.GroupDetailActivity
import com.bookiibookii.bookiibookii.group.generation.GroupGenerationActivity
import com.bookiibookii.bookiibookii.group.main.GroupAdapter
import com.google.android.material.chip.Chip

class GrpSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpSearchBinding
    private val viewModel: GrpSearchViewModel by viewModels()

    // Properties - Adapters & Managers
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var historyManager: SearchHistoryManager
    private val popularAdapter by lazy {
        PopularSearchAdapter { keyword ->
            binding.actGrpSearchBar.setText(keyword)
            performSearch(keyword)
        }
    }
    private var currentSortType = "LATEST"
    private var lastQuery = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrpSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        historyManager = SearchHistoryManager(this)

        initViews()
        initViewModel()
        initListeners()
        setupChipGroup()
    }

    // Initialization & UI Setup
    private fun initViews() {
        binding.actSearchHotGrpRv.adapter = popularAdapter

        groupAdapter = GroupAdapter(emptyList()) { group ->
            val intent = Intent(this, GroupDetailActivity::class.java).apply {
                putExtra("GROUP_ID", group.groupId.toLong())
            }
            startActivity(intent)
        }

        binding.rvSearchResult.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(this@GrpSearchActivity)
            addItemDecoration(VerticalSpaceItemDecoration(16))
        }
    }

    private fun initViewModel() {
        viewModel.displayList.observe(this) { list ->
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup, AutoTransition())
            popularAdapter.submitList(list)
        }

        viewModel.isExpanded.observe(this) { isExpanded ->
            val angle = if (isExpanded) 180f else 0f
            binding.actSearchHotGrpCardArrowDownIv.animate().rotation(angle).setDuration(300).start()
        }

        viewModel.searchResult.observe(this) { groupList ->
            binding.tvResultCount.text = "${groupList.size} 권"
            groupAdapter.updateList(groupList)

            if (groupList.isEmpty()) {
                showAppToast("검색 결과가 없습니다.", isSuccess = false)
            }
        }
    }

    private fun initListeners() {
        with(binding) {
            actGrpSearchBackIv.setOnClickListener { handleBackPress() }
            actSearchHotGrpCardArrowDownIv.setOnClickListener { viewModel.toggleExpansion() }

            actGrpSearchBar.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || actionId == 0 ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                    val query = actGrpSearchBar.text.toString()
                    if (query.isNotBlank()) performSearch(query) else showAppToast("검색어를 입력해주세요.", isSuccess = false)
                    true
                } else false
            }

            actGrpSearchBar.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.x <= actGrpSearchBar.totalPaddingStart) {
                        val query = actGrpSearchBar.text.toString()
                        if (query.isNotBlank()) performSearch(query) else showAppToast("검색어를 입력해주세요.", isSuccess = false)
                        return@setOnTouchListener true
                    }
                }
                false
            }

            actGrpNextBtn.setOnClickListener {
                startActivity(Intent(this@GrpSearchActivity, GroupGenerationActivity::class.java).apply {
                    putExtra("IS_EDIT_MODE", false)
                })
            }

            tvSortLatest.setOnClickListener { changeSortType("LATEST") }
            tvSortPopular.setOnClickListener { changeSortType("POPULAR") }
        }
    }


    // 3. Search Logic
    private fun performSearch(query: String) {
        lastQuery = query
        hideKeyboard()
        historyManager.addHistory(query)
        setupChipGroup()
        viewModel.searchGroups(query, currentSortType)
        showResultView()
    }

    private fun changeSortType(type: String) {
        if (currentSortType == type) return
        currentSortType = type
        updateSortUi()
        if (lastQuery.isNotBlank()) viewModel.searchGroups(lastQuery, currentSortType)
    }

    private fun updateSortUi() {
        val isLatest = currentSortType == "LATEST"
        binding.tvSortLatest.setTextColor(ContextCompat.getColor(this, if (isLatest) R.color.pre_main else R.color.grey_500))
        binding.tvSortPopular.setTextColor(ContextCompat.getColor(this, if (isLatest) R.color.grey_500 else R.color.pre_main))
    }

    private fun showResultView() {
        binding.layoutSearchBefore.visibility = View.GONE
        binding.layoutSearchResult.visibility = View.VISIBLE
    }

    private fun showBeforeView() {
        binding.layoutSearchBefore.visibility = View.VISIBLE
        binding.layoutSearchResult.visibility = View.GONE
        binding.actGrpSearchBar.text.clear()
    }


    // 4. Recent Search Chips
    private fun setupChipGroup() {
        binding.chipGroupRecentSearch.removeAllViews()
        val historyList = historyManager.getHistoryList()
        if (historyList.isNotEmpty()) {
            historyList.forEach { addChip(it) }
        }
    }

    private fun addChip(text: String) {
        val chip = layoutInflater.inflate(R.layout.view_chip_entry, binding.chipGroupRecentSearch, false) as Chip
        chip.text = text
        chip.setOnClickListener {
            binding.actGrpSearchBar.setText(text)
            performSearch(text)
        }
        chip.setOnCloseIconClickListener {
            historyManager.removeHistory(text)
            binding.chipGroupRecentSearch.removeView(chip)
        }
        binding.chipGroupRecentSearch.addView(chip)
    }

    // 5. Utilities & Common UI
    private fun handleBackPress() {
        if (binding.layoutSearchResult.visibility == View.VISIBLE) showBeforeView() else finish()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.actGrpSearchBar.windowToken, 0)
    }

    private fun showAppToast(message: String, isSuccess: Boolean = true) {
        val layout = LayoutInflater.from(this).inflate(R.layout.toast_custom, null)
        layout.findViewById<TextView>(R.id.toast_message_tv).text = message
        val iconRes = if (isSuccess) R.drawable.ic_check else R.drawable.ic_info
        layout.findViewById<ImageView>(R.id.toast_icon_iv)?.setImageResource(iconRes)

        with(Toast(applicationContext)) {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    class VerticalSpaceItemDecoration(private val verticalSpaceDp: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            if (parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1) {
                outRect.bottom = (verticalSpaceDp * parent.context.resources.displayMetrics.density).toInt()
            }
        }
    }

}