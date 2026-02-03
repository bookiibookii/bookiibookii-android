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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.databinding.FragmentGrpBinding
import com.google.android.material.chip.Chip

class GroupFragment : Fragment() {

    private var _binding: FragmentGrpBinding? = null
    private val binding get() = _binding!!

    private var isFabOpen = false

    // 필터 상태 관리 변수 (기본값: 전체)
    private var currentGroupTypeFilters: List<String> = listOf("전체")
    private var currentCategoryFilters: List<String> = listOf("전체")

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
        loadGroupData() // 초기 데이터 로드

        initListeners()
        initFabMenu()
        initRefreshLayout()
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
            loadGroupData() // 현재 필터 상태 유지하며 새로고침
        }
    }

    private fun loadGroupData() {
        val allData = getDummyData()

        // 1. 그룹 유형 필터링
        var filteredData = if (currentGroupTypeFilters.contains("전체")) {
            allData
        } else {
            allData.filter { data ->
                currentGroupTypeFilters.any { filter ->
                    (filter == "함께 읽기" && data.groupType == "TOGETHER") ||
                            (filter == "택배 교환" && data.groupType == "RELAY") ||
                            (filter == "직접 교환" && data.groupType == "DIRECT") // 예시
                }
            }
        }

        // 2. 분야별 필터링 (1차 결과에서 다시 필터링)
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

        // 3. 결과 반영 (로딩 딜레이 시뮬레이션)
        // 여기서 NPE 에러 발생하는 거 같음. 일단 수정은 안함
        Handler(Looper.getMainLooper()).postDelayed({
            val groupAdapter = GroupAdapter(ArrayList(filteredData)) { groupData ->
                moveToDetail(groupData)
            }
            binding.groupRecyclerview.adapter = groupAdapter
            binding.grpSwipeRefreshLayout.isRefreshing = false
        }, 500)
    }

    private fun initListeners() {
        with(binding) {
            grpSearchIv.setOnClickListener {
                val intent = Intent(requireContext(), GrpSearchActivity::class.java)
                startActivity(intent)
            }

            // 1) 그룹 유형 칩 클릭
            grpGroupTypeCp.setOnClickListener {
                FilterBottomSheetFragment(
                    filterType = FilterType.GROUP_TYPE,
                    preSelectedList = currentGroupTypeFilters,
                    onConfirm = { resultList ->
                        // 확인 눌렀을 때만 데이터 갱신
                        currentGroupTypeFilters = resultList
                        loadGroupData()
                    },
                    onDismissAction = {
                        // ★ [핵심] 창이 닫히면 무조건 UI를 현재 데이터(`currentGroupTypeFilters`)에 맞게 동기화
                        // 변경사항 없이 껐다면 기존 데이터(전체)이므로 자동으로 흰색으로 원복됨
                        updateChipUI(grpGroupTypeCp, currentGroupTypeFilters, "그룹 유형")
                    }
                ).show(parentFragmentManager, "GroupTypeFilter")
            }

            // 2) 분야별 칩 클릭
            grpCategoryCp.setOnClickListener {
                FilterBottomSheetFragment(
                    filterType = FilterType.CATEGORY,
                    preSelectedList = currentCategoryFilters,
                    onConfirm = { resultList ->
                        currentCategoryFilters = resultList
                        loadGroupData()
                    },
                    onDismissAction = {
                        // 여기도 마찬가지로 닫힐 때 UI 강제 동기화
                        updateChipUI(grpCategoryCp, currentCategoryFilters, "분야별")
                    }
                ).show(parentFragmentManager, "CategoryFilter")
            }
        }
    }

    // 칩 UI 상태 변경 함수 (원복/활성화 처리)
    private fun updateChipUI(chip: Chip, resultList: List<String>, defaultText: String) {
        if (resultList.isEmpty() || resultList.contains("전체")) {
            chip.text = defaultText
            chip.isChecked = false
        } else {
            // 필터 적용 시 -> 선택된 항목들 텍스트 & 체크 활성화 (유색 배경)
            chip.text = resultList.joinToString(" · ")
            chip.isChecked = true
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