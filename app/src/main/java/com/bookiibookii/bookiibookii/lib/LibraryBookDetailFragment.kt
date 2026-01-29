package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.LibReview
import com.bookiibookii.bookiibookii.bookData.viewModel.ReviewModel
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailBinding


class LibraryBookDetailFragment : Fragment() {

    private var _binding: FragmentLibBookDetailBinding? = null
    private val binding get() = _binding!!

    // SharedViewModel 연결
    private val viewModel: ReviewModel by activityViewModels()

    private lateinit var reviewAdapter: LibraryReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRecyclerView()
        observeViewModel()
    }

    private fun initView() {
        binding.libDetailBackIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.libReviewAddBtn.setOnClickListener {
            val addCardFragment = LibraryAddCardFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, addCardFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun initRecyclerView() {
        reviewAdapter = LibraryReviewAdapter { clickedItem ->
            // 아이템 클릭 시 다이얼로그 띄우기
            val dialog = LibraryAddDialogFragment.newInstance(clickedItem.id)
            dialog.show(parentFragmentManager, "LibAddDialog")
        }

        binding.libReviewListRv.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = reviewAdapter

            // 간격 데코레이션 (기존 코드와 동일하게 사용)
            val spacingHorizontal = dpToPx(10)
            val spacingVertical = dpToPx(12)
            if (itemDecorationCount > 0) removeItemDecorationAt(0)

            addItemDecoration(LibDetailGridDecoration(2, spacingHorizontal, spacingVertical, false))
        }
    }

    private fun observeViewModel() {
        viewModel.reviewList.observe(viewLifecycleOwner) { list ->
            // 어댑터에 데이터 전달
            reviewAdapter.submitList(list.toList())

            // 총 개수 텍스트 갱신
            binding.libDetailTotalTv.text = "${list.size}개"
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onResume() {
        super.onResume()
        hideBottomNavigation(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideBottomNavigation(false)
        _binding = null // 기존 onDestroyView에 있던 코드
    }

    private fun hideBottomNavigation(shouldHide: Boolean) {
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav)
        if (bottomNav != null) {
            bottomNav.visibility = if (shouldHide) View.GONE else View.VISIBLE
        }
    }
}