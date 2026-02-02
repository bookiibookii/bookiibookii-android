package com.bookiibookii.bookiibookii.lib

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.LibBook
import com.bookiibookii.bookiibookii.bookData.Data.ReadStatus
import com.bookiibookii.bookiibookii.databinding.FragmentLibBinding

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibBinding? = null
    private val binding get() = _binding!!

    private lateinit var libraryAdapter: LibraryBookAdapter

    private val allMyBooks = listOf(
        LibBook(
            title = "괴테는 모든 것을 말했다",
            author = "noshel",
            readStatus = ReadStatus.READING,
            progress = "50% 읽음"
        ),
        LibBook(
            title = "자바의 정석",
            author = "남궁성",
            readStatus = ReadStatus.READING,
            progress = "p.120"
        ),
        LibBook(
            title = "해리포터와 마법사의 돌",
            author = "J.K.롤링",
            readStatus = ReadStatus.DONE,
            rating = 5
        ),
        LibBook(
            title = "클린 코드",
            author = "로버트 C",
            readStatus = ReadStatus.READING,
            progress = "독서 시작 전"
        ),
        LibBook(title = "반지의 제왕", author = "톨킨", readStatus = ReadStatus.DONE, rating = 4),
        LibBook(
            title = "코틀린 인 액션",
            author = "드미트리",
            readStatus = ReadStatus.READING,
            progress = "80% 읽음"
        ),
        LibBook(title = "안드로이드 프로그래밍", author = "구글", readStatus = ReadStatus.DONE, rating = 3),
        LibBook(title = "없어질 행성에서 씁니다", author = "김초엽", readStatus = ReadStatus.READING, progress = "p.40"),
        LibBook(title = "모국어는 차라리 침묵", author = "목정원", readStatus = ReadStatus.READING, progress = "p.15")
    )

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

        showBooksByStatus(ReadStatus.READING)
    }

    private fun initRecyclerView() {
        libraryAdapter = LibraryBookAdapter(emptyList()) { clickedBook ->

            // 1. 상태에 따라 이동할 프래그먼트 결정 (targetFragment)
            val targetFragment = if (clickedBook.readStatus == ReadStatus.READING) {
                LibraryBookDetailIngFragment() // 진행 중 화면
            } else {
                LibraryBookDetailFragment()    // 종료(디테일) 화면
            }

            // 2. 데이터 전달 (Bundle)
            val bundle = Bundle().apply {
                putString("book_title", clickedBook.title)
                putString("book_author", clickedBook.author)
                // 필요하다면 ID도 전달
                // putLong("book_id", clickedBook.id)
            }

            targetFragment.arguments = bundle

            // 3. 트랜잭션 실행 (수정된 부분: detailFragment -> targetFragment)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, targetFragment) // <--- 여기가 targetFragment여야 합니다.
                .addToBackStack(null)
                .commit()
        }

        binding.libBookListRv.adapter = libraryAdapter

        setCoverModeLayout()
    }

    private fun initClickListeners() {
        // [진행 중] 버튼 클릭
        binding.libIngBtn.setOnClickListener {
            showBooksByStatus(ReadStatus.READING)
        }

        // [종료] 버튼 클릭
        binding.libEdBtn.setOnClickListener {
            showBooksByStatus(ReadStatus.DONE)
        }

        // [정렬] 버튼 클릭
        binding.libSortIv.setOnClickListener {
            val bottomSheet = LibrarySortBottomSheet()
            bottomSheet.show(parentFragmentManager, "LibrarySortBottomSheet")
        }

        // [검색] 버튼 클릭
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

        removeAllItemDecorations(binding.libBookListRv)
        val spacingHorizontal = dpToPx(12)
        val spacingVertical = dpToPx(36)

        binding.libBookListRv.addItemDecoration(
            LibDetailGridDecoration(3, spacingHorizontal, spacingVertical, false)
        )
    }

    private fun setSpineModeLayout() {
        val spanCount = 8
        // 🔹 일반 GridLayoutManager로 변경하여 정렬 기준선을 일관되게 유지
        val layoutManager = GridLayoutManager(context, spanCount)
        binding.libBookListRv.layoutManager = layoutManager

        // 🔹 RecyclerView 자체는 상단에 밀착 (여백 제거)
        binding.libBookListRv.setPadding(dpToPx(8), 0, dpToPx(8), dpToPx(80))
        binding.libBookListRv.clipToPadding = false

        removeAllItemDecorations(binding.libBookListRv)

        binding.libBookListRv.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.left = dpToPx(4)
                outRect.right = dpToPx(4)
                outRect.top = dpToPx(16) // 🔹 줄 사이의 간격
                outRect.bottom = 0
            }
        })
    }

    private fun showBooksByStatus(status: ReadStatus) {
        val filteredList = allMyBooks.filter { it.readStatus == status }
        libraryAdapter.submitList(filteredList)
        binding.libTotalTv.text = "${filteredList.size}권"
        updateButtonStyles(status)
    }

    private fun updateButtonStyles(currentStatus: ReadStatus) {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.white)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.grey_900)

        if (currentStatus == ReadStatus.READING) {
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

    private fun removeAllItemDecorations(recyclerView: RecyclerView) {
        while (recyclerView.itemDecorationCount > 0) {
            recyclerView.removeItemDecorationAt(0)
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}