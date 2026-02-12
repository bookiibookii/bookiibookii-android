package com.bookiibookii.bookiibookii.group.main

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.OvershootInterpolator
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

    private lateinit var groupAdapter: GroupAdapter

    private val userViewModel: MyPageViewModel by activityViewModels()

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

        userViewModel.fetchMypageData()

        initRecyclerViewSetting()
        initFragmentResultListeners() // 리스너 등록

        // 어댑터 미리 초기화 (빈 리스트로)
        groupAdapter = GroupAdapter(ArrayList()) { groupData ->
            // [수정] 클릭 시 바로 이동하지 않고 검증 로직을 거침
            checkInfoAndMoveToDetail(groupData)
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
        val displayList = if (currentRegionFilter == "전체") {
            listOf("전체")
        } else if (currentRegionFilter.endsWith(" 전체")) {
            // "서울 전체" -> ["서울"]
            listOf(currentRegionFilter.substringBefore(" "))
        } else {
            currentRegionFilter.substringAfter(" ").split("/")
        }
        updateChipUI(binding.grpRegionCp, displayList, "지역별")
    }

    // 필터 결과 수신 리스너
    private fun initFragmentResultListeners() {
        setFragmentResultListener("requestKeyRegion") { _, bundle ->
            val rawResult = bundle.getString("regionResult") ?: "전체"

            // 데이터 변경이 있을 때만 로드
            if (currentRegionFilter != rawResult) {
                currentRegionFilter = rawResult
                loadGroupData() // 데이터 새로고침

                // 칩 텍스트 업데이트용 리스트 생성
                val displayList = if (rawResult == "전체") {
                    listOf("전체")
                } else if (rawResult.endsWith(" 전체")) {
                    // "서울 전체" -> ["서울"] 로 표시
                    listOf(rawResult.substringBefore(" "))
                } else {
                    // "서울 강남구/서초구" -> ["강남구", "서초구"] 로 표시 (혹은 "서울" 포함하고 싶으면 로직 변경)
                    rawResult.substringAfter(" ").split("/")
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
            null // 전체 검색
        } else if (currentRegionFilter.endsWith(" 전체")) {
            // "서울 전체", "경기 전체" 인 경우 -> "서울", "경기"만 추출해서 리스트로 만듦
            listOf(currentRegionFilter.substringBefore(" "))
        } else {
            // "서울 강남구/서초구" 인 경우 -> ["강남구", "서초구"] (또는 서버 스펙에 따라 ["서울 강남구", "서울 서초구"])
            // 기존 로직 유지 (구 이름만 보냄)
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
                            checkInfoAndMoveToDetail(groupData)
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

            grpRegionCp.setOnClickListener {
                // 다이얼로그 생성
                val dialog = GrpRegionBottomSheetFragment.newInstance(currentRegionFilter)

                // 칩을 체크 상태로 만듦
                grpRegionCp.isChecked = true

                // [중요] 다이얼로그가 닫힐 때 아무 동작이 없었다면 칩 상태를 다시 확인하는 리스너 추가
                // GrpRegionBottomSheetFragment에 onDismiss 콜백이 있다면 사용하고,
                // 없다면 아래처럼 FragmentManager의 Lifecycle을 이용해 감지할 수 있습니다.

                dialog.show(parentFragmentManager, "RegionSearchBottomSheet")

                // 다이얼로그가 닫혔을 때를 감지하여 UI 복구
                parentFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                        if (f is GrpRegionBottomSheetFragment) {
                            // 닫혔을 때 현재 필터 상태에 따라 UI 원복
                            val displayList = if (currentRegionFilter == "전체") {
                                listOf("전체")
                            } else if (currentRegionFilter.endsWith(" 전체")) {
                                listOf(currentRegionFilter.substringBefore(" "))
                            } else {
                                currentRegionFilter.substringAfter(" ").split("/")
                            }
                            updateChipUI(binding.grpRegionCp, displayList, "지역별")

                            // 등록한 콜백 해제
                            parentFragmentManager.unregisterFragmentLifecycleCallbacks(this)
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
            chip.isChecked = false // "전체"면 선택 안 된 상태로
            chip.chipStrokeWidth = dpToPx(1).toFloat()
            chip.setChipStrokeColorResource(R.color.grey_200)
        } else {
            chip.text = resultList.joinToString(" · ")
            chip.isChecked = true // 값이 있으면 선택 상태 유지
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
        intent.putExtra("GROUP_ID", groupData.groupId)
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
                grpGroupFabMenuLayout.animate().alpha(1f).translationY(0f).setDuration(300).setInterpolator(
                    OvershootInterpolator()
                ).start()
                grpGroupFabMainBtn.animate().rotation(45f).setDuration(300).start()
            } else {
                grpGroupFabMenuLayout.animate().alpha(0f).translationY(50f).setDuration(300).withEndAction { grpGroupFabMenuLayout.visibility = View.GONE }.start()
                grpGroupFabMainBtn.animate().rotation(0f).setDuration(300).start()
            }
        }
    }

    private fun checkInfoAndMoveToDetail(groupData: GroupData) { // GroupUiModel은 사용하시는 모델 클래스명

        // 1. 만약 '함께 읽기(TOGETHER)' 그룹이라면 배송지가 필요 없으므로 그냥 통과
        // (모델에 groupType 필드가 있다고 가정)
        if (groupData.groupType == "TOGETHER") {
            moveToDetail(groupData)
            return
        }

        val myProfile = userViewModel.profileData.value

        // 2. 프로필 데이터가 아직 로드되지 않았다면 잠시 대기
        if (myProfile == null) {
            Toast.makeText(requireContext(), "사용자 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            userViewModel.fetchMypageData() // 재시도 트리거
            return
        }

        // 3. 필수 정보 검증 로직
        // 조건: 주소&동지역/직거래장소 필수
        val hasAddress = !myProfile.address.isNullOrBlank() && !myProfile.zipCode.isNullOrBlank()
        val hasMeetPlace = !myProfile.meetPlace.isNullOrBlank() // 혹은 region

        // (예시) 교환(RELAY) 그룹은 배송지나 직거래 장소 중 하나는 반드시 설정되어 있어야 함
       // val isInfoValid = hasBasicContact && (hasAddress || hasMeetPlace)

        val isInfoValid = hasAddress && hasMeetPlace
        if (isInfoValid) {
            moveToDetail(groupData)
        } else {
            // 정보 부족 시 다이얼로그 출력
            showRequiredInfoDialog()
        }
    }

    private fun showRequiredInfoDialog() {
        // 1. Dialog 객체 생성 (Builder 대신 바로 Dialog 사용하면 커스텀하기 편함)
        val dialog = Dialog(requireContext())

        // 2. 타이틀 제거 (setContentView 전에 해야 함)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        // 3. 레이아웃 설정
        dialog.setContentView(R.layout.dialog_none_address)

        // 4. 배경 투명 처리 (둥근 모서리 적용을 위해 필수)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 5. 텍스트 강제 설정 (XML의 tools:text 문제 해결)
        // findViewById로 뷰를 찾아서 글자를 직접 넣습니다.
        dialog.findViewById<TextView>(R.id.dialog_none_address_title_tv).text = "배송지 등록"
        dialog.findViewById<TextView>(R.id.dialog_none_address_content_tv).text = "책을 주고 받을 배송지를 등록해주세요."

        // 6. 닫기 버튼 리스너 연결
        dialog.findViewById<View>(R.id.dialog_none_address_close_iv).setOnClickListener {
            dialog.dismiss()
        }

        // 7. 다이얼로그 띄우기
        dialog.show()
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