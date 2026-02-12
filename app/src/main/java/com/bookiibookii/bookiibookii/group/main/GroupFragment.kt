package com.bookiibookii.bookiibookii.group.main

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentGrpBinding
import com.bookiibookii.bookiibookii.group.GroupDetailActivity
import com.bookiibookii.bookiibookii.group.generation.GroupGenerationActivity
import com.bookiibookii.bookiibookii.group.search.GrpSearchActivity
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class GroupFragment : Fragment() {

    // region [Properties] 데이터 및 상태 관리 변수
    private var _binding: FragmentGrpBinding? = null
    private val binding get() = _binding!!

    private lateinit var groupAdapter: GroupAdapter
    private val userViewModel: MyPageViewModel by activityViewModels()

    private var isLoading = false
    private var isFabOpen = false

    // 필터링 현재 상태 저장
    private var currentGroupTypeFilters: List<String> = listOf("전체")
    private var currentCategoryFilters: List<String> = listOf("전체")
    private var currentRegionFilter: String = "전체"
    private var currentSortType = "RECOMMEND" // RECOMMEND(추천순), LATEST(최신순), POPULAR(인기순)
    // endregion

    // region [Lifecycle] 생명주기 관리
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 데이터 로드 (마이페이지 정보는 배송지 검증용으로 미리 가져옴)
        userViewModel.fetchMypageData()

        setupRecyclerView()
        setupFragmentResultListeners() // 필터(지역 등) 선택 결과 수신 리스너

        // 어댑터 초기화 및 상세 페이지 진입 전 검증 로직 연결
        groupAdapter = GroupAdapter(ArrayList()) { groupData ->
            checkInfoAndMoveToDetail(groupData)
        }
        binding.groupRecyclerview.adapter = groupAdapter

        // 초기 UI 렌더링 및 데이터 로드
        updateSortUi()
        loadGroupData()

        // 클릭 리스너 바인딩
        initListeners()
        initFabMenu()
        initRefreshLayout()

        // 지역 칩 초기화
        updateChipUI(binding.grpRegionCp, listOf("전체"), "지역별")
    }

    override fun onResume() {
        super.onResume()
        // 지역 필터 상태에 따른 칩 텍스트 동기화
        val displayList = if (currentRegionFilter == "전체") {
            listOf("전체")
        } else if (currentRegionFilter.endsWith(" 전체")) {
            listOf(currentRegionFilter.substringBefore(" "))
        } else {
            currentRegionFilter.substringAfter(" ").split("/")
        }
        updateChipUI(binding.grpRegionCp, displayList, "지역별")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // endregion

    // region [Toast] 커스텀 토스트 시스템 (디자인 시스템 적용)
    /**
     * @param message 토스트에 표시할 텍스트
     * @param isSuccess true: 체크 아이콘(ic_check), false: 경고 아이콘(ic_info)
     */
    private fun showCustomToast(message: String, isSuccess: Boolean = true) {
        val inflater = LayoutInflater.from(requireContext())
        val layout = inflater.inflate(R.layout.toast_custom, null)

        val iconIv = layout.findViewById<ImageView>(R.id.toast_icon_iv)
        val messageTv = layout.findViewById<TextView>(R.id.toast_message_tv)

        messageTv.text = message
        iconIv.setImageResource(if (isSuccess) R.drawable.ic_check else R.drawable.ic_info)

        with(Toast(requireContext().applicationContext)) {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    // region [Data Loading] API 통신 로직
    private fun loadGroupData() {
        if (isLoading) return
        isLoading = true
        binding.grpSwipeRefreshLayout.isRefreshing = true

        val groupTypes = mutableListOf<String>()
        val tradeTypes = mutableListOf<String>()

        // 1. 그룹 유형 및 거래 방식 필터 매핑
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

        // 2. 지역(장소) 필터 매핑
        val meetPlace = if (currentRegionFilter == "전체") {
            null
        } else if (currentRegionFilter.endsWith(" 전체")) {
            listOf(currentRegionFilter.substringBefore(" "))
        } else {
            currentRegionFilter.substringAfter(" ").split("/").map { it.trim() }
        }

        // 3. 카테고리 한글 -> 서버 코드 변환
        val categories = if (currentCategoryFilters.contains("전체")) {
            null
        } else {
            currentCategoryFilters.map { convertCategoryToCode(it) }
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getGroupList(
                    groupTypes = groupTypes.ifEmpty { null },
                    tradeTypes = tradeTypes.ifEmpty { null },
                    meetPlace = meetPlace,
                    categories = categories,
                    sort = currentSortType,
                    page = 0,
                    size = 20
                )

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val serverList = response.body()?.result?.groupList ?: emptyList()
                    val uiList = serverList.map { it.toUiModel() }

                    // 어댑터 갱신 (로직 보존을 위해 새 인스턴스 할당)
                    groupAdapter = GroupAdapter(ArrayList(uiList)) { groupData ->
                        checkInfoAndMoveToDetail(groupData)
                    }
                    binding.groupRecyclerview.adapter = groupAdapter
                } else {
                    showCustomToast(response.body()?.message ?: "데이터 로드 실패", false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showCustomToast("네트워크 상태를 확인해주세요.", false)
            } finally {
                isLoading = false
                if (_binding != null) binding.grpSwipeRefreshLayout.isRefreshing = false
            }
        }
    }

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
            else -> "ETC"
        }
    }

    // region [UI Setup] 초기 설정 및 클릭 리스너
    private fun setupRecyclerView() {
        binding.groupRecyclerview.layoutManager = LinearLayoutManager(context)
        if (binding.groupRecyclerview.itemDecorationCount == 0) {
            val spaceInPx = dpToPx(16)
            binding.groupRecyclerview.addItemDecoration(VerticalSpaceItemDecoration(spaceInPx))
        }
    }

    private fun setupFragmentResultListeners() {
        setFragmentResultListener("requestKeyRegion") { _, bundle ->
            val rawResult = bundle.getString("regionResult") ?: "전체"

            if (currentRegionFilter != rawResult) {
                currentRegionFilter = rawResult
                loadGroupData() // 필터 변경 시 새로고침

                val displayList = if (rawResult == "전체") {
                    listOf("전체")
                } else if (rawResult.endsWith(" 전체")) {
                    listOf(rawResult.substringBefore(" "))
                } else {
                    rawResult.substringAfter(" ").split("/")
                }
                updateChipUI(binding.grpRegionCp, displayList, "지역별")
            }
        }
    }

    private fun initListeners() {
        with(binding) {
            grpSearchIv.setOnClickListener {
                startActivity(Intent(requireContext(), GrpSearchActivity::class.java))
            }

            // 필터 칩 클릭: 바텀시트 오픈
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

            grpRegionCp.setOnClickListener {
                val dialog = GrpRegionBottomSheetFragment.newInstance(currentRegionFilter)
                grpRegionCp.isChecked = true
                dialog.show(parentFragmentManager, "RegionSearchBottomSheet")

                // 다이얼로그 닫힘 감지 리스너 (UI 복구용)
                parentFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                        if (f is GrpRegionBottomSheetFragment) {
                            val displayList = if (currentRegionFilter == "전체") {
                                listOf("전체")
                            } else if (currentRegionFilter.endsWith(" 전체")) {
                                listOf(currentRegionFilter.substringBefore(" "))
                            } else {
                                currentRegionFilter.substringAfter(" ").split("/")
                            }
                            updateChipUI(binding.grpRegionCp, displayList, "지역별")
                            parentFragmentManager.unregisterFragmentLifecycleCallbacks(this)
                        }
                    }
                }, false)
            }

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

            // 정렬 탭 클릭
            grpMainSortRecommendTv.setOnClickListener { changeSortType("RECOMMEND") }
            grpMainSortLatestTv.setOnClickListener { changeSortType("LATEST") }
            grpMainSortPopularTv.setOnClickListener { changeSortType("POPULAR") }
        }
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

    private fun initRefreshLayout() {
        binding.grpSwipeRefreshLayout.setOnRefreshListener { loadGroupData() }
    }


    // region [UI Update] 상태 변경에 따른 UI 갱신
    private fun changeSortType(type: String) {
        if (currentSortType == type) return
        currentSortType = type
        updateSortUi()
        loadGroupData()
    }

    private fun updateSortUi() {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.pre_main)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.grey_500)

        with(binding) {
            grpMainSortRecommendTv.setTextColor(if (currentSortType == "RECOMMEND") activeColor else inactiveColor)
            grpMainSortLatestTv.setTextColor(if (currentSortType == "LATEST") activeColor else inactiveColor)
            grpMainSortPopularTv.setTextColor(if (currentSortType == "POPULAR") activeColor else inactiveColor)
        }
    }

    private fun updateChipUI(chip: Chip, resultList: List<String>, defaultText: String) {
        val isDefault = resultList.isEmpty() || (resultList.size == 1 && resultList[0] == "전체")

        if (isDefault) {
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

    private fun toggleFabMenu() {
        isFabOpen = !isFabOpen
        with(binding) {
            if (isFabOpen) {
                grpGroupFabMenuLayout.apply {
                    visibility = View.VISIBLE
                    alpha = 0f
                    translationY = 50f
                    animate().alpha(1f).translationY(0f).setDuration(300)
                        .setInterpolator(OvershootInterpolator()).start()
                }
                grpGroupFabMainBtn.animate().rotation(45f).setDuration(300).start()
            } else {
                grpGroupFabMenuLayout.animate().alpha(0f).translationY(50f).setDuration(300)
                    .withEndAction { grpGroupFabMenuLayout.visibility = View.GONE }.start()
                grpGroupFabMainBtn.animate().rotation(0f).setDuration(300).start()
            }
        }
    }
    // endregion

    // region [Navigation & Validation] 상세 페이지 이동 및 검증

    private fun checkInfoAndMoveToDetail(groupData: GroupData) {
        // '함께 읽기' 그룹은 별도 정보 없이 진입 가능
        if (groupData.groupType == "TOGETHER") {
            moveToDetail(groupData)
            return
        }

        val myProfile = userViewModel.profileData.value
        if (myProfile == null) {
            showCustomToast("사용자 정보를 확인하는 중입니다.", false)
            userViewModel.fetchMypageData()
            return
        }

        // 필수 배송지/직거래 정보 존재 여부 확인
        val hasAddress = !myProfile.address.isNullOrBlank() && !myProfile.zipCode.isNullOrBlank()
        val hasMeetPlace = !myProfile.meetPlace.isNullOrBlank()

        if (hasAddress && hasMeetPlace) {
            moveToDetail(groupData)
        } else {
            showRequiredInfoDialog() // 배송지 미등록 시 다이얼로그 노출
        }
    }

    private fun moveToDetail(groupData: GroupData) {
        val intent = Intent(requireContext(), GroupDetailActivity::class.java).apply {
            putExtra("GROUP_TYPE", groupData.groupType)
            putExtra("BOOK_TITLE", groupData.bookTitle)
            putExtra("BOOK_AUTHOR", groupData.bookAuthor)
            putExtra("BOOK_GENRE", groupData.genre)
            putExtra("START_DATE", groupData.date)
            putExtra("COVER_IMG", groupData.coverImgUrl)
            putExtra("PROFILE_IMG", groupData.profileImgUrl)
            putExtra("USER_NICNAME", groupData.nickname)
            putStringArrayListExtra("TAGS", ArrayList(groupData.tags))
            putExtra("STATUS", groupData.status)
            putExtra("READING_PERIOD", groupData.readingPeriod)
            putExtra("MEMBER_COUNT", groupData.memberCount)
            putExtra("IS_HOT", groupData.isHot)
            putExtra("GROUP_ID", groupData.groupId.toLong())
        }
        startActivity(intent)
    }

    private fun showRequiredInfoDialog() {
        Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_none_address)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            findViewById<TextView>(R.id.dialog_none_address_title_tv).text = "배송지 등록"
            findViewById<TextView>(R.id.dialog_none_address_content_tv).text = "책을 주고 받을 배송지를 등록해주세요."
            findViewById<View>(R.id.dialog_none_address_close_iv).setOnClickListener { dismiss() }
        }.show()
    }

    // region [Helpers] 유틸리티 함수 및 클래스
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