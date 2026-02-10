package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // ★ 추가됨
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel // ★ 추가됨
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.CardItem
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookmarkBinding
import kotlinx.coroutines.launch

class LibraryBookmarkFragment : Fragment() {

    private var _binding: FragmentLibBookmarkBinding? = null
    private val binding get() = _binding!!

    // ★ [추가] 내 닉네임을 알기 위해 뷰모델 연결
    private val myPageViewModel: MyPageViewModel by activityViewModels()
    private var myNickname = ""

    private lateinit var bookmarkAdapter: LibraryBookmarkAdapter
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

        // ★ [추가] 내 닉네임 가져오기
        setupMyProfileData()

        initRecyclerView()
        initListeners()
    }

    // ★ [추가] 뷰모델 관찰 함수
    private fun setupMyProfileData() {
        // 이미 데이터가 있다면 즉시 할당
        myPageViewModel.profileData.value?.let { myNickname = it.nickname }

        // 데이터가 변경되면 업데이트
        myPageViewModel.profileData.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                myNickname = profile.nickname
            }
        }

        // 데이터가 없으면 불러오기 요청
        if (myPageViewModel.profileData.value == null) {
            myPageViewModel.fetchMypageData()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchBookmarks()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    private fun initRecyclerView() {
        bookmarkAdapter = LibraryBookmarkAdapter { clickedCard ->

            // ★ [수정] 내 닉네임과 작성자를 비교하여 isMine 설정
            val isMyCard = (clickedCard.creatorName == myNickname)

            val detailFragment = LibraryCardDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong("cardId", clickedCard.cardId.toLong())

                    // ★ true/false를 동적으로 전달
                    putBoolean("isMine", isMyCard)

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
            while (itemDecorationCount > 0) removeItemDecorationAt(0)
            addItemDecoration(LibDetailGridDecoration(2, dpToPx(10), dpToPx(12), false))
        }
    }

    private fun fetchBookmarks() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getBookmarkedCards()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val apiList = response.body()?.result ?: emptyList()
                    originalList = apiList
                    updateTotalCount(apiList.size)
                    sortBookmarks(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sortBookmarks(isLatest: Boolean) {
        val sortedList = if (isLatest) {
            originalList.sortedByDescending { it.createdAt }
        } else {
            originalList.sortedBy { it.bookTitle }
        }
        bookmarkAdapter.submitList(sortedList)

        val activeColor = ContextCompat.getColor(requireContext(), R.color.grey_900)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.grey_500)

        binding.libBookLatelyTv.setTextColor(if (isLatest) activeColor else inactiveColor)
        binding.libDetailPageTv.setTextColor(if (isLatest) inactiveColor else activeColor)
    }

    private fun initListeners() {
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