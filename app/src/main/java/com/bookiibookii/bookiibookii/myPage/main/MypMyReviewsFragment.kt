package com.bookiibookii.bookiibookii

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.MyReceivedReview
import com.bookiibookii.bookiibookii.databinding.FragmentMypMyReviewsBinding
import com.bookiibookii.bookiibookii.myPage.review.MypMyReviewAdapter

class MypMyReviewFragment : Fragment() {

    private var _binding: FragmentMypMyReviewsBinding? = null
    private val binding get() = _binding!!

    private lateinit var reviewAdapter: MypMyReviewAdapter

    // 리스트 타입 명시
    private var reviewList: List<MyReceivedReview> = listOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypMyReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initRecyclerView()
        initListeners()
    }

    private fun initData() {
        reviewList = listOf(
            MyReceivedReview(1, "noshel", "괴테는 모든 것을 말했다", "스즈키 유이", "2025.12.18~2026.01.12",
                listOf("#글씨가 예뻐요", "#코멘트가 다정해요"), "이동진 평론가도 추천한...", "파트너의 한줄평 내용...", System.currentTimeMillis()),
            MyReceivedReview(2, "kanghun", "총 균 쇠", "제러드", "2025.11.01~2025.11.20",
                listOf("#칼답", "#약속철저"), "정말 좋은 책이었습니다.", "재밌었어요!", System.currentTimeMillis() - 1000000)
        )
        binding.mypReviewCountTv.text = "${reviewList.size} 개"
    }

    private fun initRecyclerView() {
        // 어댑터 생성 시 List<MyReceivedReview> 타입을 전달
        reviewAdapter = MypMyReviewAdapter(reviewList)

        binding.mypReviewsRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewAdapter
        }
        sortReviews(true)
    }

    private fun initListeners() {
        binding.mypReviewBackIv.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.mypReviewRateTv.setOnClickListener { sortReviews(true) }
        binding.mypReviewTimeTv.setOnClickListener { sortReviews(false) }
    }

    private fun sortReviews(isNewest: Boolean) {
        val sortedList = if (isNewest) {
            reviewList.sortedByDescending { it.timestamp }
        } else {
            reviewList.sortedBy { it.timestamp }
        }
        reviewAdapter.submitList(sortedList)

        val activeColor = ContextCompat.getColor(requireContext(), R.color.grey_700)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.grey_500)

        if (isNewest) {
            binding.mypReviewRateTv.setTextColor(activeColor)
            binding.mypReviewTimeTv.setTextColor(inactiveColor)
        } else {
            binding.mypReviewRateTv.setTextColor(inactiveColor)
            binding.mypReviewTimeTv.setTextColor(activeColor)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}