package com.bookiibookii.bookiibookii.myPage.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.MypRelayReview
import com.bookiibookii.bookiibookii.databinding.FragmentMypMyReviewsBinding
import kotlinx.coroutines.launch

class MypMyReviewFragment : BaseDetailFragment<FragmentMypMyReviewsBinding>() {

    private lateinit var loadingDialog: LoadingDialog
    private lateinit var reviewAdapter: MypMyReviewAdapter
    private var reviewList: List<MypRelayReview> = listOf()

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMypMyReviewsBinding {
        return FragmentMypMyReviewsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

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
        // ★ 코루틴 안전장치 적용 (viewLifecycleOwner)
        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().getRelayReviews()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    if (result != null) {
                        reviewList = result.reviews

                        binding.mypReviewCountTv.text = "${reviewList.size} 개"
                        sortReviews(true) // 기본: 최신순 정렬
                    }
                } else {
                    Log.e("ReviewFragment", "API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ReviewFragment", "Network Error", e)
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    private fun initListeners() {
        binding.mypReviewBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
        binding.mypReviewRateTv.setOnClickListener { sortReviews(true) } // 최신순
        binding.mypReviewTimeTv.setOnClickListener { sortReviews(false) } // 과거순
    }

    private fun sortReviews(isNewest: Boolean) {
        if (reviewList.isEmpty()) return

        // ★ 정렬 기준 문자열을 안전하게 처리 (null 대비)
        val sortedList = if (isNewest) {
            reviewList.sortedByDescending { it.finishedDate ?: "" }
        } else {
            reviewList.sortedBy { it.finishedDate ?: "" }
        }
        reviewAdapter.submitList(sortedList)

        // 탭 UI 컬러 변경
        val activeColor = ContextCompat.getColor(requireContext(), R.color.grey_900)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.grey_400)

        if (isNewest) {
            binding.mypReviewRateTv.setTextColor(activeColor)
            binding.mypReviewTimeTv.setTextColor(inactiveColor)
        } else {
            binding.mypReviewRateTv.setTextColor(inactiveColor)
            binding.mypReviewTimeTv.setTextColor(activeColor)
        }
    }
}