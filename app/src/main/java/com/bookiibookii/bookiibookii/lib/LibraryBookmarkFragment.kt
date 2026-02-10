package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.CardItem
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookmarkBinding
import kotlinx.coroutines.launch

class LibraryBookmarkFragment : Fragment() {

    private var _binding: FragmentLibBookmarkBinding? = null
    private val binding get() = _binding!!

    // 어댑터 (CardItem 사용)
    private lateinit var bookmarkAdapter: LibraryBookmarkAdapter

    // 원본 데이터 저장소 (정렬용)
    private var originalList: List<CardItem> = emptyList()

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
    }

    override fun onResume() {
        super.onResume()
        // 화면에 돌아올 때마다 갱신 (상세화면에서 북마크 해제했을 수 있음)
        fetchBookmarks()
        // 바텀 네비게이션 숨김
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    private fun initRecyclerView() {
        // 어댑터 초기화 (CardItem 클릭 시 상세 이동)
        bookmarkAdapter = LibraryBookmarkAdapter { clickedCard ->
            val detailFragment = LibraryCardDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong("cardId", clickedCard.cardId.toLong())
                    // 북마크 목록에서는 내 카드인지 알 수 없거나 수정 권한이 없다고 가정 (필요시 writerId 비교 로직 추가)
                    putBoolean("isMine", false)
                    putString("writerName", clickedCard.creatorName)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.libBookListRv.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = bookmarkAdapter
            // 데코레이션 (기존 코드 유지)
            while (itemDecorationCount > 0) removeItemDecorationAt(0)
            addItemDecoration(LibDetailGridDecoration(2, dpToPx(10), dpToPx(12), false))
        }
    }

    private fun fetchBookmarks() {
        lifecycleScope.launch {
            try {
                // API 호출 (GET /api/cards/bookmarks)
                val response = RetrofitClient.api().getBookmarkedCards()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    // 서버에서 받은 CardItem 리스트
                    val apiList = response.body()?.result ?: emptyList()

                    originalList = apiList
                    updateTotalCount(apiList.size)

                    // 기본 정렬 (최신순)
                    sortBookmarks(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sortBookmarks(isLatest: Boolean) {
        val sortedList = if (isLatest) {
            // 날짜 내림차순 (String 비교지만 ISO 포맷이면 작동함)
            originalList.sortedByDescending { it.createdAt }
        } else {
            // 책 제목 오름차순
            originalList.sortedBy { it.bookTitle }
        }
        bookmarkAdapter.submitList(sortedList)

        // 정렬 텍스트 색상 UI 업데이트
        val activeColor = ContextCompat.getColor(requireContext(), R.color.grey_900)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.grey_500)

        binding.libBookLatelyTv.setTextColor(if (isLatest) activeColor else inactiveColor)
        binding.libDetailPageTv.setTextColor(if (isLatest) inactiveColor else activeColor)
    }

    private fun initListeners() {
        binding.libDetailBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 검색 화면 이동 (SOURCE = "BOOKMARK")
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

        binding.libBookLatelyTv.setOnClickListener { sortBookmarks(true) }
        binding.libDetailPageTv.setOnClickListener { sortBookmarks(false) }
    }

    private fun updateTotalCount(count: Int) {
        binding.libBookTotalTv.text = "${count}개"
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.VISIBLE
        _binding = null
    }
}