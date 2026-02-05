package com.bookiibookii.bookiibookii.group

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R // 리소스 참조를 위해 필요
import com.bookiibookii.bookiibookii.databinding.FragmentGrpBinding
import com.google.android.material.chip.Chip

class GroupFragment : Fragment() {

    private var _binding: FragmentGrpBinding? = null
    private val binding get() = _binding!!

    private var isLoading = false
    private var isFabOpen = false

    // 필터 상태 관리 변수
    private var currentGroupTypeFilters: List<String> = listOf("전체")
    private var currentCategoryFilters: List<String> = listOf("전체")
    private var currentRegionFilter: String = "전체"

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
        initFragmentResultListeners()
        loadGroupData()
        initListeners()
        initFabMenu()
        initRefreshLayout()

        // 초기 UI 설정
        updateChipUI(binding.grpRegionCp, listOf("전체"), "지역별")
    }

    // ★ [추가] 다이얼로그가 취소되어 닫혔을 때,
    // 선택된 칩(검은색)을 다시 원래대로(흰색+테두리) 돌려놓기 위해 UI를 동기화합니다.
    override fun onResume() {
        super.onResume()
        // 현재 저장된 필터 변수(currentRegionFilter)를 기준으로 칩 상태를 강제 재설정
        val displayList = if (currentRegionFilter == "전체" || currentRegionFilter.endsWith(" 전체")) {
            listOf("전체")
        } else {
            currentRegionFilter.substringAfter(" ").split("/")
        }
        updateChipUI(binding.grpRegionCp, displayList, "지역별")
    }

    private fun initFragmentResultListeners() {
        setFragmentResultListener("requestKeyRegion") { _, bundle ->
            val rawResult = bundle.getString("regionResult") ?: "전체"
            val finalResult = if (rawResult.endsWith(" 전체")) "전체" else rawResult

            if (currentRegionFilter != finalResult) {
                currentRegionFilter = finalResult
                loadGroupData()

                val displayList = if (finalResult == "전체") {
                    listOf("전체")
                } else {
                    finalResult.substringAfter(" ").split("/")
                }
                updateChipUI(binding.grpRegionCp, displayList, "지역별")
            }
        }
    }

    // ... (RecyclerView, RefreshLayout, loadGroupData 등은 기존과 동일) ...
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

    private fun loadGroupData() {
        if (isLoading) return
        isLoading = true
        val allData = getDummyData()

        // 1. 그룹 유형
        var filteredData = if (currentGroupTypeFilters.contains("전체")) {
            allData
        } else {
            allData.filter { data ->
                currentGroupTypeFilters.any { filter ->
                    (filter == "함께 읽기" && data.groupType == "TOGETHER") ||
                            (filter == "택배 교환" && data.groupType == "RELAY") ||
                            (filter == "직접 교환" && data.groupType == "DIRECT")
                }
            }
        }

        // 2. 분야별
        filteredData = if (currentCategoryFilters.contains("전체")) {
            filteredData
        } else {
            filteredData.filter { data ->
                currentCategoryFilters.any { filter ->
                    data.bookGenre.contains(filter.split("/")[0]) ||
                            data.tags.any { tag -> tag.contains(filter) }
                }
            }
        }

        // 3. 지역별
        filteredData = if (currentRegionFilter == "전체") {
            filteredData
        } else {
            val searchKeywords = currentRegionFilter.substringAfter(" ").split("/")
            filteredData.filter { data ->
                searchKeywords.any { keyword ->
                    data.tags.any { it.contains(keyword) }
                }
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (_binding == null) {
                isLoading = false
                return@postDelayed
            }
            val groupAdapter = GroupAdapter(ArrayList(filteredData)) { groupData ->
                moveToDetail(groupData)
            }
            binding.groupRecyclerview.adapter = groupAdapter
            binding.grpSwipeRefreshLayout.isRefreshing = false
            isLoading = false
        }, 500)
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

            // 2) 지역별 칩 클릭
            grpRegionCp.setOnClickListener {
                // 클릭 시각적 피드백
                grpRegionCp.isChecked = true

                val dialog = GrpRegionBottomSheetFragment.newInstance(currentRegionFilter)
                dialog.show(parentFragmentManager, "RegionSearchBottomSheet")

                //  다이얼로그가 닫히는 순간을 감지하는 리스너 등록
                parentFragmentManager.registerFragmentLifecycleCallbacks(object : androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentDetached(fm: androidx.fragment.app.FragmentManager, f: androidx.fragment.app.Fragment) {
                        super.onFragmentDetached(fm, f)
                        if (f == dialog) {
                            // UI를 현재 데이터 상태(currentRegionFilter)에 맞춰서 다시 그리기
                            // (선택 안 하고 닫았다면 "전체"로 인식해서 흰색으로 원복됩니다)
                            val displayList = if (currentRegionFilter == "전체") {
                                listOf("전체")
                            } else {
                                currentRegionFilter.substringAfter(" ").split("/")
                            }
                            updateChipUI(grpRegionCp, displayList, "지역별")

                            // 리스너 해제 (메모리 누수 방지)
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
        }
    }

    // ★ [핵심 수정] 칩 UI 상태 변경 함수 (테두리 색상 gray_200 적용)
    private fun updateChipUI(chip: Chip, resultList: List<String>, defaultText: String) {
        if (resultList.isEmpty() || (resultList.size == 1 && resultList[0] == "전체")) {
            // [기본 상태: "지역별"]
            chip.text = defaultText
            chip.isChecked = false // 배경 흰색 등 Unchecked 스타일

            // ★ 테두리 적용 (1dp, gray_200)
            chip.chipStrokeWidth = dpToPx(1).toFloat()
            chip.setChipStrokeColorResource(R.color.grey_200)

        } else {
            // [선택된 상태: "송파구 · 동작구"]
            chip.text = resultList.joinToString(" · ")
            chip.isChecked = true // 배경 검은색 등 Checked 스타일

            // ★ 선택된 칩은 보통 테두리가 없거나 투명해야 깔끔합니다. (검은 배경이므로)
            chip.chipStrokeWidth = 0f
            // 또는 디자인에 따라 테두리를 유지해야 한다면 아래 주석 해제
            // chip.chipStrokeWidth = dpToPx(1).toFloat()
            // chip.setChipStrokeColorResource(R.color.transparent)
        }
    }

    private fun getDummyData(): ArrayList<GroupData> {
        val list = ArrayList<GroupData>()
        list.add(GroupData(
            "https://picsum.photos/300/200", "괴테는 모든 것을 말했다", "스즈키 유이", "(소설)", "모집 중", "7", "5", true,
            "https://picsum.photos/100/100", "noshel", "2025. 12. 16.", listOf("#메모환영", "#인사이트"), "RELAY"
        ))
        list.add(GroupData(
            "https://picsum.photos/300/201", "물고기는 존재하지 않는다", "룰루 밀러", "(에세이)", "모집 완료", "1", "2", false,
            "https://picsum.photos/100/101", "booklover", "2026. 02. 02.", listOf("#과학", "#철학", "#함께읽기"), "TOGETHER"
        ))
        return list
    }

    private fun moveToDetail(groupData: GroupData) {
        val intent = Intent(requireContext(), GroupDetailActivity::class.java)
        intent.putExtra("GROUP_TYPE", groupData.groupType)
        intent.putExtra("BOOK_TITLE", groupData.bookTitle)
        intent.putExtra("BOOK_AUTHOR", groupData.bookAuthor)
        intent.putExtra("BOOK_GENRE", groupData.bookGenre)
        intent.putExtra("START_DATE", groupData.date)
        intent.putExtra("COVER_IMG", groupData.coverImgUrl)
        intent.putExtra("PROFILE_IMG", groupData.profileImgUrl)
        intent.putExtra("USER_NICNAME", groupData.nickname)
        intent.putStringArrayListExtra("TAGS", ArrayList(groupData.tags))
        intent.putExtra("STATUS", groupData.status)
        intent.putExtra("DEADLINE", groupData.deadline)
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