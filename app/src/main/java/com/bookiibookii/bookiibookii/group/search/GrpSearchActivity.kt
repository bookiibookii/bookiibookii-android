package com.bookiibookii.bookiibookii.group.search

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.BaseActivity
import com.bookiibookii.bookiibookii.common.SearchHistoryManager
import com.bookiibookii.bookiibookii.databinding.ActivityGrpSearchBinding
import com.bookiibookii.bookiibookii.group.GroupDetailActivity
import com.bookiibookii.bookiibookii.group.generation.GroupGenerationActivity
import com.bookiibookii.bookiibookii.group.main.GroupAdapter
import com.google.android.material.chip.Chip

class GrpSearchActivity : BaseActivity<ActivityGrpSearchBinding>(){

    override fun getViewBinding(): ActivityGrpSearchBinding {
        return ActivityGrpSearchBinding.inflate(layoutInflater)
    }

    private val viewModel: GrpSearchViewModel by viewModels()

    private lateinit var groupAdapter: GroupAdapter

    private lateinit var historyManager: SearchHistoryManager

    // 상태 변수
    private var currentSortType = "LATEST" // "LATEST" or "POPULAR"
    private var lastQuery = ""

    // 어댑터 설정
    // [인기 검색어 어댑터]
    private val popularAdapter by lazy {
        PopularSearchAdapter { keyword ->
            binding.actGrpSearchBar.setText(keyword)
            performSearch(keyword) // 클릭 시 바로 검색
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivityGrpSearchBinding.inflate(layoutInflater)
//        setContentView(binding.root)

        // 매니저 초기화
        historyManager = SearchHistoryManager(this)

        initViews()
        initViewModel()
        initListeners()
        setupChipGroup()
    }

    private fun initViews() {
        // 인기 검색어 어댑터 연결 (기존 코드)
        binding.actSearchHotGrpRv.adapter = popularAdapter

        // 2. 검색 결과 어댑터 초기화 (처음엔 빈 리스트로 시작)
        groupAdapter = GroupAdapter(emptyList()) { group ->
            val intent = Intent(this, GroupDetailActivity::class.java).apply {
                // 그룹의 식별자(ID)를 넘겨줍니다.
                // 키값("GROUP_ID")은 GroupDetailActivity에서 받는 키값과 동일해야 합니다.
                putExtra("GROUP_ID", group.groupId.toLong())

                // 만약 그룹 객체 전체를 넘기고 싶다면, Group 모델이 Parcelable을 구현해야 합니다.
                // putExtra("GROUP_DATA", group)
            }
            startActivity(intent)
        }

        // 3. 리사이클러뷰에 연결
        binding.rvSearchResult.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(this@GrpSearchActivity)

            addItemDecoration(VerticalSpaceItemDecoration(16))
        }
    }
    private fun initViewModel() {
        // 인기 검색어 리스트 관찰
        viewModel.displayList.observe(this) { list ->
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup, AutoTransition())
            popularAdapter.submitList(list)
        }

        // 화살표 회전 관찰
        viewModel.isExpanded.observe(this) { isExpanded ->
            val angle = if (isExpanded) 180f else 0f
            binding.actSearchHotGrpCardArrowDownIv.animate().rotation(angle).setDuration(300).start()
        }

        // 검색 결과 관찰 및 어댑터 갱신
        viewModel.searchResult.observe(this) { groupList ->
            binding.tvResultCount.text = "${groupList.size} 권"

            // ★ 여기서 데이터 갱신!
            groupAdapter.updateList(groupList)

            // (선택사항) 결과가 없을 때 처리
            if (groupList.isEmpty()) {
                // binding.layoutEmpty.visibility = View.VISIBLE
            } else {
                // binding.layoutEmpty.visibility = View.GONE
            }
        }
    }

    private fun initListeners() {
        // [뒤로가기 버튼]
        binding.actGrpSearchBackIv.setOnClickListener {
            handleBackPress()
        }

        // [인기 검색어 펼치기/접기]
        binding.actSearchHotGrpCardArrowDownIv.setOnClickListener {
            viewModel.toggleExpansion()
        }

        // [검색 실행 (엔터키)]
        binding.actGrpSearchBar.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == 0 || // ★ 추가: 키보드가 아무 액션 ID도 안 줄 때(기본 엔터) 처리
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                val query = binding.actGrpSearchBar.text.toString()
                if (query.isNotBlank()) {
                    performSearch(query)
                }
                true
            } else {
                false
            }
        }

        binding.actGrpSearchBar.setOnTouchListener { v, event ->
            // 터치를 했다가 손을 뗐을 때 (ACTION_UP)
            if (event.action == MotionEvent.ACTION_UP) {
                // 터치한 위치(event.x)가 왼쪽 아이콘 영역(totalPaddingStart) 안쪽인지 확인
                if (event.x <= binding.actGrpSearchBar.totalPaddingStart) {
                    val query = binding.actGrpSearchBar.text.toString()
                    if (query.isNotBlank()) {
                        performSearch(query)
                    }
                    // 터치 이벤트를 여기서 소비함 (키보드 올라오는 것 방지 등)
                    return@setOnTouchListener true
                }
            }
            // 그 외 영역(글자 입력 부분)을 터치하면 정상적으로 입력 모드 진입
            false
        }

        // [그룹 만들기 버튼]
        binding.actGrpNextBtn.setOnClickListener {
            val intent = Intent(this, GroupGenerationActivity::class.java)
            intent.putExtra("IS_EDIT_MODE", false)
            startActivity(intent)
        }

        // [정렬 필터 버튼]
        binding.tvSortLatest.setOnClickListener {
            changeSortType("LATEST")
        }
        binding.tvSortPopular.setOnClickListener {
            changeSortType("POPULAR")
        }
    }

    // 검색 실행 로직
    private fun performSearch(query: String) {
        lastQuery = query
        hideKeyboard()

        // ★ 4. 검색할 때마다 저장하고 -> 칩 갱신
        historyManager.addHistory(query)
        setupChipGroup() // 칩 화면 새로고침

        viewModel.searchGroups(query, currentSortType)
        showResultView()
    }

    // 정렬 타입 변경
    private fun changeSortType(type: String) {
        if (currentSortType == type) return // 이미 선택된 상태면 무시

        currentSortType = type
        updateSortUi() // 텍스트 색상 변경

        // 검색어가 있을 때만 다시 검색 수행
        if (lastQuery.isNotBlank()) {
            viewModel.searchGroups(lastQuery, currentSortType)
        }
    }

    // 정렬 UI (색상) 업데이트
    private fun updateSortUi() {
        if (currentSortType == "LATEST") {
            binding.tvSortLatest.setTextColor(ContextCompat.getColor(this, R.color.pre_main))
            binding.tvSortPopular.setTextColor(ContextCompat.getColor(this, R.color.grey_500))
        } else {
            binding.tvSortLatest.setTextColor(ContextCompat.getColor(this, R.color.grey_500))
            binding.tvSortPopular.setTextColor(ContextCompat.getColor(this, R.color.pre_main))
        }
    }

    // --- 화면 전환 (Visibility) 헬퍼 함수들 ---

    private fun showResultView() {
        binding.layoutSearchBefore.visibility = View.GONE
        binding.layoutSearchResult.visibility = View.VISIBLE
    }

    private fun showBeforeView() {
        binding.layoutSearchBefore.visibility = View.VISIBLE
        binding.layoutSearchResult.visibility = View.GONE

        // (선택) 검색창 텍스트 지우기
        binding.actGrpSearchBar.text.clear()
    }

    // --- 유틸리티 ---

    private fun handleBackPress() {
        // 검색 결과 화면이 떠 있다면 -> 검색 전 화면으로
        if (binding.layoutSearchResult.visibility == View.VISIBLE) {
            showBeforeView()
        } else {
            // 검색 전 화면이라면 -> 액티비티 종료
            finish()
        }
    }

//    // 시스템 뒤로가기 버튼(하단바) 눌렀을 때도 동일하게 동작하도록 오버라이드
//    override fun onBackPressed() {
//        // super.onBackPressed() // 이걸 지우거나 조건부 호출
//        if (binding.layoutSearchResult.visibility == View.VISIBLE) {
//            showBeforeView()
//        } else {
//            super.onBackPressed()
//        }
//    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.actGrpSearchBar.windowToken, 0)
    }

    // --- 칩(Chip) 관련 로직 (기존 유지) ---
    private fun setupChipGroup() {
        // 기존 칩 다 지우기 (초기화)
        binding.chipGroupRecentSearch.removeAllViews()

        // ★ 저장된 리스트 가져와서 칩 만들기
        val historyList = historyManager.getHistoryList()

        // 리스트가 비어있으면 숨기거나 처리 가능 (선택사항)
        if (historyList.isEmpty()) {
            // binding.actGrpTitleTv.visibility = View.GONE // "최근 검색어" 타이틀 숨기기 등
        } else {
            // binding.actGrpTitleTv.visibility = View.VISIBLE
            historyList.forEach { addChip(it) }
        }
    }

    private fun addChip(text: String) {
        val chip = layoutInflater.inflate(R.layout.view_chip_entry, binding.chipGroupRecentSearch, false) as Chip
        chip.text = text

        // 클릭 시 검색
        chip.setOnClickListener {
            binding.actGrpSearchBar.setText(text)
            performSearch(text)
        }

        // ★ X 버튼 클릭 시 삭제 로직 연결
        chip.setOnCloseIconClickListener {
            historyManager.removeHistory(text) // 저장소에서 삭제
            binding.chipGroupRecentSearch.removeView(chip) // 화면에서 삭제
        }

        binding.chipGroupRecentSearch.addView(chip)
    }

    // 리사이클러뷰 아이템 간격 조절용 클래스
    class VerticalSpaceItemDecoration(private val verticalSpaceDp: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            // 마지막 아이템이 아니면 바텀 마진 추가 (마지막 아이템은 바닥에 딱 붙게 하고 싶으면 조건문 유지)
            if (parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1) {
                outRect.bottom = dpToPx(parent.context, verticalSpaceDp)
            } else {
                // 마지막 아이템에도 여백 주고 싶으면 위 조건문 지우고 그냥 이것만 쓰면 됨:
                // outRect.bottom = dpToPx(parent.context, verticalSpaceDp)
            }
        }

        // dp -> px 변환 함수
        private fun dpToPx(context: Context, dp: Int): Int {
            return (dp * context.resources.displayMetrics.density).toInt()
        }
    }
}