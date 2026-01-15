package com.bookiibookii.bookiibookii

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bookiibookii.bookiibookii.bookData.Data.Book
import com.bookiibookii.bookiibookii.bookData.Data.LibBook
import com.bookiibookii.bookiibookii.bookData.Data.ReadStatus
import com.bookiibookii.bookiibookii.databinding.FragmentLibBinding

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibBinding? = null
    private val binding get() = _binding!!

    private lateinit var libraryAdapter: LibraryBookAdapter

    private val allMyBooks = listOf(
        LibBook(title = "괴테는 모든 것을 말했다", author = "noshel", readStatus = ReadStatus.READING, progress = "50% 읽음"),
        LibBook(title = "자바의 정석", author = "남궁성", readStatus = ReadStatus.READING, progress = "p.120"),
        LibBook(title = "해리포터와 마법사의 돌", author = "J.K.롤링", readStatus = ReadStatus.DONE, rating = 5),
        LibBook(title = "클린 코드", author = "로버트 C", readStatus = ReadStatus.READING, progress = "독서 시작 전"),
        LibBook(title = "반지의 제왕", author = "톨킨", readStatus = ReadStatus.DONE, rating = 4),
        LibBook(title = "코틀린 인 액션", author = "드미트리", readStatus = ReadStatus.READING, progress = "80% 읽음"),
        LibBook(title = "안드로이드 프로그래밍", author = "구글", readStatus = ReadStatus.DONE, rating = 3)
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
        libraryAdapter = LibraryBookAdapter(emptyList()) // 초기 데이터

        binding.libBookListRv.apply {
            layoutManager = GridLayoutManager(context, 3)

            adapter = libraryAdapter

            val spacingHorizontal = dpToPx(12) // 좌우 간격
            val spacingVertical = dpToPx(36)   // 상하 간격

            if (itemDecorationCount > 0) removeItemDecorationAt(0)

            addItemDecoration(LibDetailGridDecoration(3, spacingHorizontal, spacingVertical, false))
        }
    }

    // dp -> px 변환 함수 (프래그먼트 내에 없다면 추가)
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
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
        binding.libSortIv.setOnClickListener {
            val bottomSheet = LibrarySortBottomSheet()
            bottomSheet.show(parentFragmentManager, "LibrarySortBottomSheet")
        }
        binding.libSearchIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, LibrarySearchFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showBooksByStatus(status: ReadStatus) {

        val filteredList = allMyBooks.filter { it.readStatus == status }
        libraryAdapter.submitList(filteredList)
        binding.libTotalTv.text = "${filteredList.size}권"
        updateButtonStyles(status)
    }

    private fun updateButtonStyles(currentStatus: ReadStatus) {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.white)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.grey_900) // 본인 색상코드 사용

        if (currentStatus == ReadStatus.READING) {
            // 진행 중 활성화
            binding.libIngBtn.setBackgroundResource(R.drawable.bg_toggle_black10)
            binding.libIngBtn.setTextColor(activeColor)

            binding.libEdBtn.setBackgroundResource(R.drawable.bg_toggle_white10)
            binding.libEdBtn.setTextColor(inactiveColor)
        } else {
            // 종료 활성화
            binding.libIngBtn.setBackgroundResource(R.drawable.bg_toggle_white10)
            binding.libIngBtn.setTextColor(inactiveColor)

            binding.libEdBtn.setBackgroundResource(R.drawable.bg_toggle_black10)
            binding.libEdBtn.setTextColor(activeColor)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}