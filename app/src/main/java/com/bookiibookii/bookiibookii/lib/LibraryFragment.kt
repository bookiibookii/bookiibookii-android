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
                val response = RetrofitClient.api().getLibraryBooks()

                // 1. HTTP 응답 상태 확인 로그
                Log.d("LibraryAPI", "Response Code: ${response.code()}")

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val resultList = response.body()?.result ?: emptyList()

                    // 2. API에서 받아온 원본 데이터 리스트 로그 출력
                    Log.d("LibraryAPI", "받아온 도서 개수: ${resultList.size}")
                    resultList.forEachIndexed { index, apiData ->
                        Log.d("LibraryAPI", "--- API 원본 Data [$index] ---")
                        Log.d("LibraryAPI", "userBookId: ${apiData.userBookId}, groupId: ${apiData.groupId}")
                        Log.d("LibraryAPI", "title: ${apiData.title}, author: ${apiData.author}")
                        Log.d("LibraryAPI", "rating: ${apiData.rating}, duration: ${apiData.duration}")
                        Log.d("LibraryAPI", "startDate: ${apiData.startDate}, endDate: ${apiData.endDate}")
                        Log.d("LibraryAPI", "image: ${apiData.image}")
                        Log.d("LibraryAPI", "groupType: ${apiData.groupType}")
                    }

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
                            hostName = apiData.hostId.toString(),
                            hostProfileUrl = apiData.hostProfileImageUrl,

                            // [수정] 날짜 정보 매핑
                            startDate = apiData.startDate,
                            endDate = apiData.endDate,

                            // [수정] 리뷰 작성 여부 매핑
                            isReviewed = reviewWritten,

                            readStatus = status,
                            progress = if (status == ReadStatus.READING) "${apiData.duration}일째" else "완독",
                            rating = apiData.rating,
                            groupType = apiData.groupType
                        )

                        // 3. 변환된 LibBook 객체 로그 출력
                        Log.d("LibraryAPI", "=> 변환된 모델: $mappedBook")

                        mappedBook
                    }
                    showBooksByStatus(currentTabStatus)
                } else {
                    Log.e("LibraryAPI", "API 호출 실패: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                Log.e("LibraryAPI", "에러 발생: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun applySort(sortType: SortType) {
        if (allMyBooks.isEmpty()) {
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
        libraryAdapter.submitList(filteredList)
        binding.libTotalTv.text = "${filteredList.size}권"
        updateButtonStyles(status)
    }

    private fun initRecyclerView() {
        libraryAdapter = LibraryBookAdapter(emptyList()) { clickedBook ->

            val targetFragment: Fragment

            // [수정된 로직]
            if (clickedBook.groupType == "TOGETHER") {
                // [함께 읽기] 기존 로직 유지
                if (clickedBook.readStatus == ReadStatus.DONE) {
                    targetFragment = LibraryBookDetailTogetherFragment() // 완료됨 -> 투게더 상세
                } else {
                    targetFragment = LibraryBookDetailIngFragment() // 진행중 -> Ing 상세
                }
            } else {
                // [이어 읽기 (RELAY)]
                // ★ 수정: 후기 유무와 상관없이 무조건 상세 화면으로 이동
                targetFragment = LibraryBookDetailFragment()
            }

            val bundle = Bundle().apply {
                putInt("userBookId", clickedBook.id)
                putInt("groupId", clickedBook.groupId)
                putString("bookTitle", clickedBook.title)
                putString("bookAuthor", clickedBook.author)
                putString("bookCover", clickedBook.coverUrl)
                putString("hostName", clickedBook.hostName)
                putString("hostProfileUrl", clickedBook.hostProfileUrl)

                // 날짜 및 평점 전달 (상세 화면에서 UI 분기 처리에 사용)
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