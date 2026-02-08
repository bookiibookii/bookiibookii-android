package com.bookiibookii.bookiibookii.group

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.GroupListRequest
import com.bookiibookii.bookiibookii.databinding.FragmentGrpBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class GroupFragment : Fragment() {

    private lateinit var groupAdapter: GroupAdapter
    private var _binding: FragmentGrpBinding? = null
    private val binding get() = _binding!!

    private var isLoading = false
    private var isFabOpen = false

    // 필터 상태 관리 변수
    private var currentGroupTypeFilters: List<String> = listOf("전체")
    private var currentCategoryFilters: List<String> = listOf("전체")
    private var currentRegionFilter: String = "전체"

    private var currentSortType = "RECOMMEND"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGrpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerViewSetting()
        initFragmentResultListeners() // 리스너 등록

        // 어댑터 미리 초기화 (빈 리스트로)
        groupAdapter = GroupAdapter(ArrayList()) { groupData ->
            moveToDetail(groupData)
        }
        binding.groupRecyclerview.adapter = groupAdapter

        updateSortUi() // 초기정렬
        loadGroupData() // 데이터 로드

        initListeners()
        initFabMenu()
        initRefreshLayout()

        updateChipUI(binding.grpRegionCp, listOf("전체"), "지역별")
    }

    override fun onResume() {
        super.onResume()
        // 필터 상태와 칩 UI 동기화 (다이얼로그 닫힌 후 등 대비)
        val displayList = if (currentRegionFilter == "전체" || currentRegionFilter.endsWith(" 전체")) {
            listOf("전체")
        } else {
            currentRegionFilter.substringAfter(" ").split("/")
        }
        updateChipUI(binding.grpRegionCp, displayList, "지역별")
    }

    // 필터 결과 수신 리스너
    private fun initFragmentResultListeners() {
        setFragmentResultListener("requestKeyRegion") { _, bundle ->
            val rawResult = bundle.getString("regionResult") ?: "전체"
            val finalResult = if (rawResult.endsWith(" 전체")) "전체" else rawResult

            if (currentRegionFilter != finalResult) {
                currentRegionFilter = finalResult
                loadGroupData() // 변경되었으니 재조회

                val displayList = if (finalResult == "전체") {
                    listOf("전체")
                } else {
                    finalResult.substringAfter(" ").split("/")
                }
                updateChipUI(binding.grpRegionCp, displayList, "지역별")
            }
        }
    }

    private fun initRecyclerViewSetting() {
        binding.groupRecyclerview.layoutManager = LinearLayoutManager(context)
        if (binding.groupRecyclerview.itemDecorationCount == 0) {
            val spaceInPx = dpToPx(16)
            binding.groupRecyclerview.addItemDecoration(VerticalSpaceItemDecoration(spaceInPx))
        }
    }

    private fun initRefreshLayout() {
        binding.grpSwipeRefreshLayout.setOnRefreshListener {
            loadGroupData()
        }
    }


    // ★ [헬퍼] 한글 카테고리 -> 서버 코드 변환
    private fun convertCategoryToCode(uiName: String): String {
        return when {
            uiName.contains("경제") || uiName.contains("경영") -> "ECON_BIZ"
            uiName.contains("과학") || uiName.contains("IT") -> "SCI_IT"
            uiName.contains("소설") || uiName.contains("장르") -> "NOVEL_GENRE"
            uiName.contains("시") || uiName.contains("에세이") -> "POEM_ESSAY"
            uiName.contains("가정") || uiName.contains("취미") -> "HOME_HOBBY"
            uiName.contains("예술") || uiName.contains("문화") -> "ART_CULTURE"
            uiName.contains("인문") || uiName.contains("역사") -> "HUMAN_HISTORY"
            uiName.contains("자기계발") -> "SELF_DEV"
            uiName.contains("정치") || uiName.contains("사회") -> "POL_SOC"
            else -> "ETC" // 기타
        }
    }

    // ★ 2. 실제 API 통신 로직
    private fun loadGroupData() {
        if (isLoading) return
        isLoading = true
        binding.grpSwipeRefreshLayout.isRefreshing = true

        val groupTypes = mutableListOf<String>()
        val tradeTypes = mutableListOf<String>()

        if (!currentGroupTypeFilters.contains("전체")) {
            if (currentGroupTypeFilters.contains("함께 읽기")) groupTypes.add("TOGETHER")
            if (currentGroupTypeFilters.contains("택배 교환")) {
                groupTypes.add("RELAY")
                tradeTypes.add("DELIVERY")
            }
            if (currentGroupTypeFilters.contains("직접 교환")) {
                groupTypes.add("RELAY")
                tradeTypes.add("DIRECT")
            }
        }

        val meetPlace = if (currentRegionFilter == "전체") {
            null
        } else {
            currentRegionFilter.substringAfter(" ").split("/").map { it.trim() }
        }

        val categories = if (currentCategoryFilters.contains("전체")) {
            null
        } else {
            currentCategoryFilters.map { convertCategoryToCode(it) }
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getGroupList(
                    groupTypes = if (groupTypes.isEmpty()) null else groupTypes,
                    tradeTypes = if (tradeTypes.isEmpty()) null else tradeTypes,
                    meetPlace = meetPlace,
                    categories = categories,
                    sort = currentSortType, // 기본 추천순
                    page = 0,
                    size = 20
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.isSuccess == true) {
                        val serverList = body.result?.groupList ?: emptyList()
                        val uiList = serverList.map { it.toUiModel() }

                        groupAdapter = GroupAdapter(ArrayList(uiList)) { groupData ->
                            moveToDetail(groupData)
                        }
                        binding.groupRecyclerview.adapter = groupAdapter

                    } else {
                        Toast.makeText(context, body?.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "네트워크 오류", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
                if (_binding != null) binding.grpSwipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun initListeners() {
        with(binding) {
            grpSearchIv.setOnClickListener {
                startActivity(Intent(requireContext(), GrpSearchActivity::class.java))
            }

            // 1) 그룹 유형
            grpGroupTypeCp.setOnClickListener {
                FilterBottomSheetFragment(
                    filterType = FilterType.GROUP_TYPE,
                    preSelectedList = currentGroupTypeFilters,
                    onConfirm = { resultList ->
                        currentGroupTypeFilters = resultList
                        loadGroupData()
                    },
                    onDismissAction = {
                        updateChipUI(grpGroupTypeCp, currentGroupTypeFilters, "그룹 유형")
                    }
                ).show(parentFragmentManager, "GroupTypeFilter")
            }

            // 2) 지역별
            grpRegionCp.setOnClickListener {
                grpRegionCp.isChecked = true
                val dialog = GrpRegionBottomSheetFragment.newInstance(currentRegionFilter)
                dialog.show(parentFragmentManager, "RegionSearchBottomSheet")

                parentFragmentManager.registerFragmentLifecycleCallbacks(object :
                    androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentDetached(
                        fm: androidx.fragment.app.FragmentManager,
                        f: androidx.fragment.app.Fragment
                    ) {
                        super.onFragmentDetached(fm, f)
                        if (f == dialog) {
                            val displayList = if (currentRegionFilter == "전체") {
                                listOf("전체")
                            } else {
                                currentRegionFilter.substringAfter(" ").split("/")
                            }
                            updateChipUI(grpRegionCp, displayList, "지역별")
                            fm.unregisterFragmentLifecycleCallbacks(this)
                        }
                    }
                }, false)
            }

            // 3) 분야별
            grpCategoryCp.setOnClickListener {
                FilterBottomSheetFragment(
                    filterType = FilterType.CATEGORY,
                    preSelectedList = currentCategoryFilters,
                    onConfirm = { resultList ->
                        currentCategoryFilters = resultList
                        loadGroupData()
                    },
                    onDismissAction = {
                        updateChipUI(grpCategoryCp, currentCategoryFilters, "분야별")
                    }
                ).show(parentFragmentManager, "CategoryFilter")
            }
            //  정렬 필터 클릭 리스너
            //
            grpMainSortRecommendTv.setOnClickListener { changeSortType("RECOMMEND") }
            grpMainSortLatestTv.setOnClickListener { changeSortType("LATEST") }
            grpMainSortPopularTv.setOnClickListener { changeSortType("POPULAR") }
        }
    }

    private fun changeSortType(type: String) {
        if (currentSortType == type) return // 이미 선택된 거면 무시
        currentSortType = type
        updateSortUi()
        loadGroupData()
    }

    // 정렬 UI (텍스트 색상) 업데이트
    private fun updateSortUi() {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.pre_main)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.grey_500)

        // 1. 추천순
        if (currentSortType == "RECOMMEND") {
            binding.grpMainSortRecommendTv.setTextColor(activeColor)
        } else {
            binding.grpMainSortRecommendTv.setTextColor(inactiveColor)
        }

        // 2. 최신순
        if (currentSortType == "LATEST") {
            binding.grpMainSortLatestTv.setTextColor(activeColor)
        } else {
            binding.grpMainSortLatestTv.setTextColor(inactiveColor)
        }

        // 3. 인기순
        if (currentSortType == "POPULAR") {
            binding.grpMainSortPopularTv.setTextColor(activeColor)
        } else {
            binding.grpMainSortPopularTv.setTextColor(inactiveColor)
        }
    }
    private fun updateChipUI(chip: Chip, resultList: List<String>, defaultText: String) {
        if (resultList.isEmpty() || (resultList.size == 1 && resultList[0] == "전체")) {
            chip.text = defaultText
            chip.isChecked = false
            chip.chipStrokeWidth = dpToPx(1).toFloat()
            chip.setChipStrokeColorResource(R.color.grey_200)
        } else {
            chip.text = resultList.joinToString(" · ")
            chip.isChecked = true
            chip.chipStrokeWidth = 0f
        }
    }

    private fun moveToDetail(groupData: GroupData) {
        val intent = Intent(requireContext(), GroupDetailActivity::class.java)
        // GroupData의 필드들을 Intent에 담아 이동
        intent.putExtra("GROUP_TYPE", groupData.groupType)
        intent.putExtra("BOOK_TITLE", groupData.bookTitle)
        intent.putExtra("BOOK_AUTHOR", groupData.bookAuthor)
        intent.putExtra("BOOK_GENRE", groupData.genre)
        intent.putExtra("START_DATE", groupData.date)
        intent.putExtra("COVER_IMG", groupData.coverImgUrl)
        intent.putExtra("PROFILE_IMG", groupData.profileImgUrl)
        intent.putExtra("USER_NICNAME", groupData.nickname)
        intent.putStringArrayListExtra("TAGS", ArrayList(groupData.tags))
        intent.putExtra("STATUS", groupData.status)
        intent.putExtra("READING_PERIOD", groupData.readingPeriod)
        intent.putExtra("MEMBER_COUNT", groupData.memberCount)
        intent.putExtra("IS_HOT", groupData.isHot)
        startActivity(intent)
    }

    private fun initFabMenu() {
        with(binding) {
            grpGroupFabMainBtn.setOnClickListener { toggleFabMenu() }
            grpGroupFabOptionTogether.setOnClickListener {
                startActivity(Intent(requireContext(), GroupGenerationActivity::class.java).apply {
                    putExtra("IS_EDIT_MODE", false)
                    putExtra("GROUP_TYPE", "TOGETHER")
                })
                toggleFabMenu()
            }
            grpGroupFabOptionRelay.setOnClickListener {
                startActivity(Intent(requireContext(), GroupGenerationActivity::class.java).apply {
                    putExtra("IS_EDIT_MODE", false)
                    putExtra("GROUP_TYPE", "RELAY")
                })
                toggleFabMenu()
            }
        }
    }

    private fun toggleFabMenu() {
        isFabOpen = !isFabOpen
        with(binding) {
            if (isFabOpen) {
                grpGroupFabMenuLayout.visibility = View.VISIBLE
                grpGroupFabMenuLayout.alpha = 0f
                grpGroupFabMenuLayout.translationY = 50f
                grpGroupFabMenuLayout.animate().alpha(1f).translationY(0f).setDuration(300).setInterpolator(OvershootInterpolator()).start()
                grpGroupFabMainBtn.animate().rotation(45f).setDuration(300).start()
            } else {
                grpGroupFabMenuLayout.animate().alpha(0f).translationY(50f).setDuration(300).withEndAction { grpGroupFabMenuLayout.visibility = View.GONE }.start()
                grpGroupFabMainBtn.animate().rotation(0f).setDuration(300).start()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    inner class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            if (parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1) {
                outRect.bottom = verticalSpaceHeight
            }
        }
    }
}