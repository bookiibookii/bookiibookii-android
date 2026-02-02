package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.LibBookmarkItem
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookmarkBinding

class LibraryBookmarkFragment : Fragment() {

    private var _binding: FragmentLibBookmarkBinding? = null
    private val binding get() = _binding!!

    private lateinit var bookmarkAdapter: LibraryBookmarkAdapter

    // 더미 데이터
    private val bookmarkList = listOf(
        LibBookmarkItem(1, "괴테는 모든 것을 말했다", 72, "여기서 개뿔을...", "2026.01.20", "kanghunsim", null, R.drawable.bg_round_8dp_gray300),
        LibBookmarkItem(2, "어린왕자", 15, "정말 중요한 건 눈에 보이지 않아", "2026.02.01", "kanghunsim", null, null),
        LibBookmarkItem(3, "코틀린 인 액션", 100, "확장 함수란...", "2025.12.30", "kanghunsim", null, null)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initListeners()
        sortBookmarks(isLatest = true) // 기본: 최신순
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun initRecyclerView() {
        bookmarkAdapter = LibraryBookmarkAdapter(bookmarkList) {
            // 아이템 클릭 시 상세 이동
        }
        binding.libBookListRv.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = bookmarkAdapter

            // 데코레이션 중복 방지
            while (itemDecorationCount > 0) removeItemDecorationAt(0)
            addItemDecoration(LibDetailGridDecoration(2, dpToPx(10), dpToPx(12), false))
        }
        updateTotalCount(bookmarkList.size)

    }

    private fun initListeners() {
        // 뒤로가기
        binding.libDetailBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.libSearch.setOnClickListener {
            val searchFragment = LibrarySearchFragment().apply {
                arguments = Bundle().apply {
                    putString("SOURCE", "BOOKMARK")
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, searchFragment)
                .addToBackStack(null)
                .commit()
        }

        // 정렬 텍스트 클릭 리스너
        binding.libBookLatelyTv.setOnClickListener { sortBookmarks(isLatest = true) }
        binding.libDetailPageTv.setOnClickListener { sortBookmarks(isLatest = false) }
    }

    private fun sortBookmarks(isLatest: Boolean) {
        val sortedList = if (isLatest) {
            bookmarkList.sortedByDescending { it.date } // 날짜 내림차순
        } else {
            bookmarkList.sortedBy { it.title } // 제목 오름차순
        }
        bookmarkAdapter.submitList(sortedList)

        // 텍스트 색상 변경 UI 업데이트
        val activeColor = ContextCompat.getColor(requireContext(), R.color.grey_900)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.grey_500)

        if (isLatest) {
            binding.libBookLatelyTv.setTextColor(activeColor)
            binding.libDetailPageTv.setTextColor(inactiveColor)
        } else {
            binding.libBookLatelyTv.setTextColor(inactiveColor)
            binding.libDetailPageTv.setTextColor(activeColor)
        }
    }

    private fun updateTotalCount(count: Int) {
        binding.libBookTotalTv.text = "${count}개"
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.VISIBLE
        _binding = null
    }
}