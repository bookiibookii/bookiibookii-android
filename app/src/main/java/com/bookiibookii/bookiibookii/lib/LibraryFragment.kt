package com.bookiibookii.bookiibookii.lib

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.LibBook
import com.bookiibookii.bookiibookii.data.model.ReadStatus
import com.bookiibookii.bookiibookii.data.viewModel.LibraryViewModel
import com.bookiibookii.bookiibookii.data.viewModel.SortType
import com.bookiibookii.bookiibookii.databinding.FragmentLibBinding
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.launch
import kotlin.math.log

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibBinding? = null
    private val binding get() = _binding!!

    private lateinit var libraryAdapter: LibraryBookAdapter

    // 전체 데이터 (필터링/정렬 전 원본)
    private var allMyBooks: List<LibBook> = emptyList()

    // 현재 탭 상태 (기본값: 읽는 중)
    private var currentTabStatus: ReadStatus = ReadStatus.READING

    private val viewModel: LibraryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initClickListeners()
        fetchBooks()

        // 정렬 옵션 관찰
        viewModel.sortType.observe(viewLifecycleOwner) { sortType ->
            Log.d("LibraryFragment", "정렬 타입 변경됨: $sortType")
            applySort(sortType)
        }
    }

    private fun fetchBooks() {
        // [변경] 로딩바 표시 (MainActivity 호출 대신 내부 뷰 제어)
        binding.loadingProgressBar.visibility = View.VISIBLE

        // (선택) 로딩 중엔 리스트를 잠깐 숨겨서 깜빡임 방지 (원치 않으시면 이 줄 삭제하세요)
        binding.libBookListRv.visibility = View.INVISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getLibraryBooks()

                // HTTP 응답 상태 확인 로그
                Log.d("LibraryAPI", "Response Code: ${response.code()}")

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val resultList = response.body()?.result ?: emptyList()

                    // API에서 받아온 원본 데이터 리스트 로그 출력
                    Log.d("LibraryAPI", "받아온 도서 개수: ${resultList.size}")
                    Log.d("LibraryAPI", "받아온 도서 : ${resultList}")

                    allMyBooks = resultList.map { apiData ->
                        // 평점이 0보다 크면 완독/리뷰 작성한 것으로 간주
                        val status = if (apiData.rating > 0.0) ReadStatus.DONE else ReadStatus.READING
                        val reviewWritten = apiData.rating > 0.0

                        val mappedBook = LibBook(
                            id = apiData.userBookId,
                            groupId = apiData.groupId,
                            title = apiData.title,
                            author = apiData.author,
                            coverUrl = apiData.image,
                            hostName = apiData.hostNickname,
                            hostProfileUrl = apiData.hostProfileImageUrl,
                            startDate = apiData.startDate,
                            endDate = apiData.endDate,
                            isReviewed = reviewWritten,
                            readStatus = status,
                            progress = if (status == ReadStatus.READING) "${apiData.duration}일째" else "완독",
                            rating = apiData.rating,
                            groupType = apiData.groupType
                        )
                        mappedBook
                    }
                    // 데이터 갱신
                    showBooksByStatus(currentTabStatus)
                } else {
                    Log.e("LibraryAPI", "API 호출 실패: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                Log.e("LibraryAPI", "에러 발생: ${e.message}")
                e.printStackTrace()
            } finally {
                // [변경] 로딩 종료 (성공하든 실패하든 무조건 실행)
                binding.loadingProgressBar.visibility = View.GONE
                binding.libBookListRv.visibility = View.VISIBLE
            }
        }
    }

    private fun applySort(sortType: SortType) {
        if (allMyBooks.isEmpty()) {
            return
        }

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
    }

    private fun initRecyclerView() {
        libraryAdapter = LibraryBookAdapter(emptyList()) { clickedBook ->
            val targetFragment: Fragment
            if (clickedBook.groupType == "TOGETHER") {
                if (clickedBook.readStatus == ReadStatus.DONE) {
                    targetFragment = LibraryBookDetailTogetherFragment()
                } else {
                    targetFragment = LibraryBookDetailIngFragment()
                }
            } else {
                if (clickedBook.readStatus == ReadStatus.DONE) {
                    // 완료됨 -> 디테일 화면
                    targetFragment = LibraryBookDetailFragment()
                } else {
                    // 진행중 -> 트래커 화면
                    // ★ 주의: TrackerFragment의 정확한 패키지 경로와 클래스명을 확인해주세요.
                    // 예: com.bookiibookii.bookiibookii.tracker.TrackerFragment
                    targetFragment = LibraryFragment()
                }
            }

            val bundle = Bundle().apply {
                putInt("userBookId", clickedBook.id)
                putInt("groupId", clickedBook.groupId)
                putString("bookTitle", clickedBook.title)
                putString("bookAuthor", clickedBook.author)
                putString("bookCover", clickedBook.coverUrl)
                putString("hostName", clickedBook.hostName)
                putString("hostProfileUrl", clickedBook.hostProfileUrl)
                putString("startDate", clickedBook.startDate)
                putString("endDate", clickedBook.endDate)
                putDouble("rating", clickedBook.rating)
            }
            targetFragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, targetFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.libBookListRv.layoutManager = GridLayoutManager(context, 3)
        binding.libBookListRv.adapter = libraryAdapter
        setCoverModeLayout()
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
        binding.libSortIv.setOnClickListener {
            LibrarySortBottomSheet().show(parentFragmentManager, "LibrarySortBottomSheet")
        }
        binding.libSearchIv.setOnClickListener {
            val searchFragment = LibrarySearchFragment().apply {
                arguments = Bundle().apply { putString("SOURCE", "LIBRARY") }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, searchFragment)
                .addToBackStack(null)
                .commit()
        }
        binding.libBookIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LibraryBookmarkFragment())
                .addToBackStack(null)
                .commit()
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
    }

    private fun setCoverModeLayout() {
        binding.libBookListRv.layoutManager = GridLayoutManager(context, 3)
        binding.libBookListRv.setPadding(0, 0, 0, dpToPx(80))
        binding.libBookListRv.clipToPadding = false
        removeAllItemDecorations()
        binding.libBookListRv.addItemDecoration(LibDetailGridDecoration(3, dpToPx(12), dpToPx(36), false))
    }


    private fun setSpineModeLayout() {
        // ★ FlexboxLayoutManager 설정
        val flexboxLayoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW        // 가로 방향
            flexWrap = FlexWrap.WRAP                 // 자동 줄바꿈
            justifyContent = JustifyContent.FLEX_START // 왼쪽 정렬
            alignItems = AlignItems.FLEX_END         // ★ 핵심: 바닥(Bottom) 기준 정렬
        }
        binding.libBookListRv.layoutManager = flexboxLayoutManager

        // 패딩 설정: XML에서 아이템 간격을 처리하므로 RV는 외곽 패딩만 잡습니다.
        binding.libBookListRv.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(80))
        binding.libBookListRv.clipToPadding = false

        // Flexbox 모드에서는 ItemDecoration 없이 XML 마진으로 간격을 제어합니다.
        removeAllItemDecorations()

        // 데이터 강제 갱신 (LayoutManager 교체 시 크기 재계산 유도)
        libraryAdapter.notifyDataSetChanged()
    }

    private fun removeAllItemDecorations() {
        while (binding.libBookListRv.itemDecorationCount > 0) {
            binding.libBookListRv.removeItemDecorationAt(0)
        }
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