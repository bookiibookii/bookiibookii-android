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
import kotlinx.coroutines.launch

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
        lifecycleScope.launch {
            try {
                Log.d("LibraryFragment", "API 요청 시작")
                val response = RetrofitClient.api().getLibraryBooks()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val resultList = response.body()?.result ?: emptyList()
                    Log.d("LibraryFragment", "API 성공: ${resultList.size}권 받아옴")

                    allMyBooks = resultList.map { apiData ->
                        // 평점이 0.0 초과면 완독(DONE)으로 간주
                        val status = if (apiData.rating > 0.0) ReadStatus.DONE else ReadStatus.READING
                        val progressText = if (status == ReadStatus.READING) {
                            "${apiData.duration}일째 독서 중"
                        } else {
                            "완독"
                        }

                        LibBook(
                            id = apiData.userBookId,
                            title = apiData.title,
                            author = apiData.author,
                            coverUrl = apiData.image,
                            hostProfileUrl = apiData.hostProfileImageUrl,
                            readStatus = status,
                            progress = progressText,
                            rating = apiData.rating,
                            groupType = apiData.groupType // "TOGETHER" or "RELAY"
                        )
                    }

                    // ViewModel에 원본 데이터 세팅 (선택 사항)
                    viewModel.setBookList(allMyBooks)

                    // 현재 탭 상태로 리스트 표시
                    val currentSort = viewModel.sortType.value ?: SortType.TITLE
                    applySort(currentSort)

                } else {
                    Log.e("LibraryFragment", "API 오류: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("LibraryFragment", "네트워크 오류: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun applySort(sortType: SortType) {
        if (allMyBooks.isEmpty()) {
            Log.d("LibraryFragment", "정렬할 데이터가 없음")
            return
        }

        // 1. 정렬 수행
        allMyBooks = when (sortType) {
            SortType.TITLE -> allMyBooks.sortedBy { it.title }
            SortType.RATING_HIGH -> allMyBooks.sortedByDescending { it.rating }
            SortType.RATING_LOW -> allMyBooks.sortedBy { it.rating }
            SortType.RECENT -> allMyBooks.sortedByDescending { it.id } // ID 기준 최신순
            SortType.OLD -> allMyBooks.sortedBy { it.id }
        }

        // 2. 현재 탭(읽는중/완독)에 맞춰 필터링 후 표시
        showBooksByStatus(currentTabStatus)
    }

    private fun showBooksByStatus(status: ReadStatus) {
        val filteredList = allMyBooks.filter { it.readStatus == status }
        Log.d("LibraryFragment", "탭 필터링 결과: 상태=$status, 개수=${filteredList.size}")

        libraryAdapter.submitList(filteredList)
        binding.libTotalTv.text = "${filteredList.size}권"
        updateButtonStyles(status)
    }

    private fun initRecyclerView() {
        libraryAdapter = LibraryBookAdapter(emptyList()) { clickedBook ->
            Log.d("LibraryFragment", "책 클릭됨: ${clickedBook.title}, 타입=${clickedBook.groupType}")

            // ★ [핵심 분기] 함께읽기(TOGETHER) -> Ing 화면 / 이어읽기(RELAY) -> Detail 화면
            val targetFragment = if (clickedBook.groupType == "TOGETHER") {
                LibraryBookDetailIngFragment() // 함께읽기 (진행중)
            } else {
                LibraryBookDetailFragment()    // 이어읽기 (상세)
            }

            val bundle = Bundle().apply {
                putInt("userBookId", clickedBook.id)
                putString("bookTitle", clickedBook.title)
                putString("bookAuthor", clickedBook.author)
                putString("bookCover", clickedBook.coverUrl)
                putBoolean("isMine", true)
            }
            targetFragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, targetFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.libBookListRv.adapter = libraryAdapter
        setCoverModeLayout() // 초기: 커버 모드
    }

    private fun initClickListeners() {
        // [진행 중] 탭
        binding.libIngBtn.setOnClickListener {
            currentTabStatus = ReadStatus.READING
            showBooksByStatus(ReadStatus.READING)
        }

        // [종료] 탭
        binding.libEdBtn.setOnClickListener {
            currentTabStatus = ReadStatus.DONE
            showBooksByStatus(ReadStatus.DONE)
        }

        // [정렬] 바텀시트
        binding.libSortIv.setOnClickListener {
            LibrarySortBottomSheet().show(parentFragmentManager, "LibrarySortBottomSheet")
        }

        // [검색]
        binding.libSearchIv.setOnClickListener {
            val searchFragment = LibrarySearchFragment().apply {
                arguments = Bundle().apply { putString("SOURCE", "LIBRARY") }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, searchFragment)
                .addToBackStack(null)
                .commit()
        }

        // [북마크]
        binding.libBookIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LibraryBookmarkFragment())
                .addToBackStack(null)
                .commit()
        }

        // [뷰 모드 전환]
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

        // 아이템 데코레이션 (기존 사용하시던 클래스)
        binding.libBookListRv.addItemDecoration(LibDetailGridDecoration(3, dpToPx(12), dpToPx(36), false))
    }

    private fun setSpineModeLayout() {
        binding.libBookListRv.layoutManager = GridLayoutManager(context, 8)
        binding.libBookListRv.setPadding(dpToPx(8), 0, dpToPx(8), dpToPx(80))
        binding.libBookListRv.clipToPadding = false
        removeAllItemDecorations()

        binding.libBookListRv.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.left = dpToPx(4); outRect.right = dpToPx(4); outRect.top = dpToPx(16); outRect.bottom = 0
            }
        })
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