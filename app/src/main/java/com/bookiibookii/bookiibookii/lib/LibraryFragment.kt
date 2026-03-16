package com.bookiibookii.bookiibookii.lib

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.LibBook
import com.bookiibookii.bookiibookii.data.model.ReadStatus
import com.bookiibookii.bookiibookii.data.viewModel.LibraryViewModel
import com.bookiibookii.bookiibookii.data.viewModel.SortType
import com.bookiibookii.bookiibookii.databinding.FragmentLibBinding
import com.bookiibookii.bookiibookii.group.generation.GroupGenerationActivity
import com.bookiibookii.bookiibookii.trkDirectGuest.DirectGuestActivity
import com.bookiibookii.bookiibookii.trkDirectHost.DirectHostActivity
import com.bookiibookii.bookiibookii.trkGuest.GuestActivity
import com.bookiibookii.bookiibookii.trkHost.HostActivity
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog

    private lateinit var libraryAdapter: LibraryBookAdapter
    private var allMyBooks: List<LibBook> = emptyList()
    private var currentTabStatus: ReadStatus = ReadStatus.READING

    private val viewModel: LibraryViewModel by activityViewModels()
    private val myPageViewModel: MyPageViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        binding.loadingProgressBar.visibility = View.GONE

        initRecyclerView()
        initClickListeners()
        setupProfileAndFetchBooks()

        viewModel.sortType.observe(viewLifecycleOwner) { sortType -> applySort(sortType) }

        requireActivity().supportFragmentManager.setFragmentResultListener("REFRESH_LIBRARY", viewLifecycleOwner) { _, _ ->
            fetchBooks()
        }
    }
    // ★ 새로 추가되는 함수
    private fun setupProfileAndFetchBooks() {
        // 프로필 정보가 서버에서 도착하면 실행됨
        myPageViewModel.profileData.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                if (allMyBooks.isEmpty()) {
                    // 책 목록이 없으면 새로 불러오기
                    fetchBooks()
                } else {
                    // 책 목록이 이미 있으면 내 닉네임과 비교해서 isMine 상태만 업데이트 후 색상 새로고침
                    allMyBooks.forEach { it.isMine = (it.hostName == profile.nickname) }
                    libraryAdapter.notifyDataSetChanged()
                }
            }
        }

        // 프로필 데이터가 아직 비어있다면 API 요청, 이미 있다면 바로 책 불러오기
        if (myPageViewModel.profileData.value == null) {
            myPageViewModel.fetchMypageData()
        } else {
            fetchBooks()
        }
    }

    private fun fetchBooks() {
        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
            binding.libBookListRv.visibility = View.INVISIBLE
            try {
                val response = RetrofitClient.api().getLibraryBooks()
                Log.d("LibraryAPI", "${response.body()}")

                val myNickname = myPageViewModel.profileData.value?.nickname ?: ""
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val resultList = response.body()?.result ?: emptyList()

                    val filteredList = resultList.filter {
                        it.groupStatus == "MATCHED" || it.groupStatus == "COMPLETED"
                    }

                    allMyBooks = filteredList.map { apiData ->
                        val state = apiData.groupStatus
                        val status = if (state == "COMPLETED") ReadStatus.DONE else ReadStatus.READING
                        val reviewWritten = apiData.rating > 0.0

                        LibBook(
                            id = apiData.userBookId,
                            groupId = apiData.groupId,
                            title = apiData.title,
                            author = apiData.author,
                            coverUrl = apiData.image,
                            hostName = apiData.hostNickName,
                            hostProfileUrl = apiData.hostProfileImageUrl,
                            startDate = apiData.startDate,
                            endDate = apiData.endDate,
                            isReviewed = reviewWritten,
                            readStatus = status,
                            progress = if (status == ReadStatus.READING) "${apiData.duration}일째" else "완독",
                            rating = apiData.rating,
                            groupType = apiData.groupType,
                            groupState = state ?: "",
                            isMine = (apiData.hostNickName == myNickname)
                        )
                    }
                    showBooksByStatus(currentTabStatus)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
                binding.libBookListRv.visibility = View.VISIBLE
            }
        }
    }

    private fun applySort(sortType: SortType) {
        if (allMyBooks.isEmpty()) return
        allMyBooks = when (sortType) {
            SortType.TITLE -> allMyBooks.sortedBy { it.title }
            SortType.RATING_HIGH -> allMyBooks.sortedByDescending { it.rating }
            SortType.RATING_LOW -> allMyBooks.sortedBy { it.rating }
            SortType.RECENT -> allMyBooks.sortedByDescending { it.id }
            SortType.OLD -> allMyBooks.sortedBy { it.id }
        }
        showBooksByStatus(currentTabStatus)
    }

    private fun showBooksByStatus(status: ReadStatus) {
        val filteredList = allMyBooks.filter { it.readStatus == status }
        libraryAdapter.submitList(filteredList)
        binding.libTotalTv.text = "${filteredList.size}권"
        updateButtonStyles(status)

        if (filteredList.isEmpty()) {
            // ★ [수정] XML의 include ID(layoutEmptyGroup)를 통해 접근
            binding.layoutEmptyGroup.root.visibility = View.VISIBLE
            binding.libBookListRv.visibility = View.GONE
            binding.libGridIv.visibility = View.GONE
            binding.libTotalTv.visibility = View.GONE
            binding.libSortIv.visibility = View.GONE

            if (status == ReadStatus.READING) {
                binding.layoutEmptyGroup.tvEmptyTitle.text = "아직 진행 중인 독서가 없어요 \uD83D\uDE2D"
                binding.layoutEmptyGroup.tvEmptyDesc.text = "읽고 싶은 책을 골라 그룹을 만들어볼까요?"
                binding.layoutEmptyGroup.btnCreateGroup.visibility = View.VISIBLE
            } else {
                binding.layoutEmptyGroup.tvEmptyTitle.text = "완료한 독서가 없어요 \uD83D\uDE2D"
                binding.layoutEmptyGroup.tvEmptyDesc.text = "진행 중인 독서를 완료하고 기록을 남겨보세요!"
                // 완료 탭에서는 그룹 만들기 버튼을 가리려면 GONE, 보이게 두려면 VISIBLE로 설정하세요
                binding.layoutEmptyGroup.btnCreateGroup.visibility = View.GONE
            }
        } else {
            binding.layoutEmptyGroup.root.visibility = View.GONE
            binding.libBookListRv.visibility = View.VISIBLE
            binding.libGridIv.visibility = View.VISIBLE
            binding.libTotalTv.visibility = View.VISIBLE
            binding.libSortIv.visibility = View.VISIBLE
        }
    }

    private fun initRecyclerView() {
        libraryAdapter = LibraryBookAdapter(emptyList()) { clickedBook ->
            Log.d("LibraryClick", "클릭: ${clickedBook.title}, Type: ${clickedBook.groupType}, Status: ${clickedBook.readStatus}, Reviewed: ${clickedBook.isReviewed}")

            // 1. 함께 읽기 (TOGETHER) 처리
            if (clickedBook.groupType == "TOGETHER") {
                val targetFragment: Fragment = if (clickedBook.readStatus == ReadStatus.DONE || clickedBook.isReviewed) {
                    LibraryBookDetailTogetherFragment()
                } else {
                    LibraryBookDetailIngFragment()
                }

                navigateToFragment(targetFragment, clickedBook)
                return@LibraryBookAdapter
            }

            // 2. 이어 읽기 (RELAY) 처리
            if (clickedBook.readStatus == ReadStatus.DONE) {
                navigateToFragment(LibraryBookDetailFragment(), clickedBook)
            } else {
                checkTradeTypeAndNavigate(clickedBook)
            }
        }

        binding.libBookListRv.layoutManager = GridLayoutManager(context, 3)
        binding.libBookListRv.adapter = libraryAdapter
        setCoverModeLayout()
    }

    private fun checkTradeTypeAndNavigate(book: LibBook) {
        Log.d("TrackerCheck", "========== 네비게이션 로직 시작 ==========")
        Log.d("TrackerCheck", "Target GroupID: ${book.groupId}, isMine(Host여부): ${book.isMine}")

        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().getMyTrackers()

                Log.d("TrackerCheck", "API Response Code: ${response.code()}")

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val trackerList = response.body()?.result ?: emptyList()

                    Log.d("TrackerCheck", "받아온 트래커 개수: ${trackerList.size}")
                    Log.d("TrackerCheck", "목록 내 ID들: ${trackerList.map { it.groupId }}")

                    val targetTracker = trackerList.find { it.groupId == book.groupId }

                    if (targetTracker != null) {
                        val tradeType = targetTracker.tradeType
                        val isHost = book.isMine

                        Log.d("TrackerCheck", ">> 매칭된 트래커 발견!")
                        Log.d("TrackerCheck", "   - tradeType: '$tradeType'")
                        Log.d("TrackerCheck", "   - isHost: $isHost")

                        val targetActivityClass = when {
                            isHost && tradeType == "DELIVERY" -> {
                                Log.d("TrackerCheck", "결정: HostActivity (택배/호스트)")
                                HostActivity::class.java
                            }
                            !isHost && tradeType == "DELIVERY" -> {
                                Log.d("TrackerCheck", "결정: GuestActivity (택배/게스트)")
                                GuestActivity::class.java
                            }
                            isHost && tradeType == "DIRECT" -> {
                                Log.d("TrackerCheck", "결정: DirectHostActivity (직거래/호스트)")
                                DirectHostActivity::class.java
                            }
                            !isHost && tradeType == "DIRECT" -> {
                                Log.d("TrackerCheck", "결정: DirectGuestActivity (직거래/게스트)")
                                DirectGuestActivity::class.java
                            }
                            else -> {
                                Log.e("TrackerCheck", "결정 실패: 조건에 맞는 케이스 없음 (Else 분기)")
                                null
                            }
                        }

                        if (targetActivityClass != null) {
                            val intent = Intent(requireActivity(), targetActivityClass)
                            intent.putExtra("group_id", book.groupId.toLong())
                            startActivity(intent)
                            Log.d("TrackerCheck", "StartActivity 실행 완료")
                        } else {
                            Toast.makeText(context, "이동할 수 없는 상태입니다 (조건 불일치).", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("TrackerCheck", "!! 해당 GroupID(${book.groupId})를 가진 트래커를 리스트에서 찾지 못함")
                        Toast.makeText(context, "트래커 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("TrackerCheck", "API 실패 메시지: ${response.body()?.message}")
                    Toast.makeText(context, "정보 조회 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("TrackerCheck", "Exception 발생: ${e.message}")
                e.printStackTrace()
                Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    private fun navigateToFragment(targetFragment: Fragment, book: LibBook) {
        val bundle = Bundle().apply {
            putInt("userBookId", book.id)
            putInt("groupId", book.groupId)
            putString("bookTitle", book.title)
            putString("bookAuthor", book.author)
            putString("bookCover", book.coverUrl)
            putString("hostName", book.hostName)
            putString("hostProfileUrl", book.hostProfileUrl)
            putString("startDate", book.startDate)
            putString("endDate", book.endDate)
            putDouble("rating", book.rating)
            putBoolean("isMine", book.isMine)
        }
        targetFragment.arguments = bundle
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, targetFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun initClickListeners() {
        binding.libIngBtn.setOnClickListener {
            currentTabStatus = ReadStatus.READING
            showBooksByStatus(ReadStatus.READING)
        }
        binding.libEdBtn.setOnClickListener {
            currentTabStatus = ReadStatus.DONE
            showBooksByStatus(ReadStatus.DONE)
        }
        binding.libSortIv.setOnClickListener { LibrarySortBottomSheet().show(requireActivity().supportFragmentManager, "LibrarySortBottomSheet") }
        binding.libSearchIv.setOnClickListener {
            val searchFragment = LibrarySearchFragment().apply { arguments = Bundle().apply { putString("SOURCE", "LIBRARY") } }
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, searchFragment).addToBackStack(null).commit()
        }
        binding.libBookIv.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, LibraryBookmarkFragment()).addToBackStack(null).commit()
        }
        binding.libGridIv.setOnClickListener {
            libraryAdapter.toggleMode()
            if (libraryAdapter.isSpineMode) {
                setSpineModeLayout()
                binding.libGridIv.setImageResource(R.drawable.ic_grid_list)
            } else {
                setCoverModeLayout()
                binding.libGridIv.setImageResource(R.drawable.ic_grid)
            }
        }

        // ★ [수정] 포함된 레이아웃의 그룹 생성 버튼 클릭 리스너 연결
        binding.layoutEmptyGroup.btnCreateGroup.setOnClickListener {
            val intent = Intent(requireContext(), GroupGenerationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setCoverModeLayout() {
        binding.libBookListRv.layoutManager = GridLayoutManager(context, 3)
        binding.libBookListRv.setPadding(0, 0, 0, dpToPx(80))
        binding.libBookListRv.clipToPadding = false
        removeAllItemDecorations()
        binding.libBookListRv.addItemDecoration(LibDetailGridDecoration(3, dpToPx(12), dpToPx(36), false))
    }

    private fun setSpineModeLayout() {
        val flexboxLayoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_END
        }
        binding.libBookListRv.layoutManager = flexboxLayoutManager
        binding.libBookListRv.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(80))
        binding.libBookListRv.clipToPadding = false
        removeAllItemDecorations()
        libraryAdapter.notifyDataSetChanged()
    }

    private fun removeAllItemDecorations() {
        while (binding.libBookListRv.itemDecorationCount > 0) binding.libBookListRv.removeItemDecorationAt(0)
    }

    private fun updateButtonStyles(status: ReadStatus) {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.white)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.grey_900)

        if (status == ReadStatus.READING) {
            binding.libIngBtn.setBackgroundResource(R.drawable.bg_toggle_black10)
            binding.libIngBtn.setTextColor(activeColor)
            binding.libEdBtn.setBackgroundResource(R.drawable.bg_toggle_white10)
            binding.libEdBtn.setTextColor(inactiveColor)
        } else {
            binding.libIngBtn.setBackgroundResource(R.drawable.bg_toggle_white10)
            binding.libIngBtn.setTextColor(inactiveColor)
            binding.libEdBtn.setBackgroundResource(R.drawable.bg_toggle_black10)
            binding.libEdBtn.setTextColor(activeColor)
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}