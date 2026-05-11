package com.bookiibookii.bookiibookii.myPage.notice

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentMypNoticeDetailBinding
import kotlinx.coroutines.launch

class MypNoticeDetailFragment : BaseDetailFragment<FragmentMypNoticeDetailBinding>() {

    private lateinit var loadingDialog: LoadingDialog
    private var noticeId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            noticeId = it.getInt("noticeId", -1)
        }
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMypNoticeDetailBinding {
        return FragmentMypNoticeDetailBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        binding.mypNoticeBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        if (noticeId != -1) {
            fetchNoticeDetail(noticeId)
        } else {
            Log.e("NoticeDetail", "Invalid Notice ID")
        }
    }

    private fun fetchNoticeDetail(id: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.mypApi().getNoticeDetail(id)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val detail = response.body()!!.result
                    binding.mypNoticeDetailDateTv.text = DateUtils.formatDate(detail.createdAt)
                    binding.mypNoticeContentTv.text = detail.content
                } else {
                    Log.e("NoticeDetail", "상세 조회 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("NoticeDetail", "네트워크 오류", e)
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }
}