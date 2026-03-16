package com.bookiibookii.bookiibookii

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.common.LoadingDialog // ★ 로딩 다이얼로그 import
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.MypRelayReview
import com.bookiibookii.bookiibookii.databinding.FragmentMypMyReviewsBinding
import com.bookiibookii.bookiibookii.myPage.review.MypMyReviewAdapter
import kotlinx.coroutines.launch

class MypMyReviewFragment : BaseDetailFragment() {

    private var _binding: FragmentMypMyReviewsBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog // ★ 로딩 선언

    private lateinit var reviewAdapter: MypMyReviewAdapter
    private var reviewList: List<MypRelayReview> = listOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypMyReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext()) // ★ 로딩 초기화

        initRecyclerView()
        initListeners()
        fetchReviewData()
    }

    private fun initRecyclerView() {
        reviewAdapter = MypMyReviewAdapter(reviewList)
        binding.mypReviewsRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewAdapter
        }
    }

    private fun fetchReviewData() {
        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show() // ★ API 호출 전 로딩 시작
            try {
                val response = RetrofitClient.api().getRelayReviews()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    if (result != null) {
                        reviewList = result.reviews

                        binding.mypReviewCountTv.text = "${reviewList.size} 개"
                        sortReviews(true)
                    }
                } else {
                    Log.e("ReviewFragment", "API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ReviewFragment", "Network Error", e)
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss() // ★ 무조건 로딩 끝내기
            }
        }
    }

    private fun initListeners() {
        binding.mypReviewBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
        binding.mypReviewRateTv.setOnClickListener { sortReviews(true) }
        binding.mypReviewTimeTv.setOnClickListener { sortReviews(false) }
    }

    private fun sortReviews(isNewest: Boolean) {
        val sortedList = if (isNewest) {
            reviewList.sortedByDescending { it.finishedDate }
        } else {
            reviewList.sortedBy { it.finishedDate }
        }
        reviewAdapter.submitList(sortedList)

        val activeColor = ContextCompat.getColor(requireContext(), R.color.pre_main)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}