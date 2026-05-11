package com.bookiibookii.bookiibookii.myPage.question

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.common.showCustomToast // ★ 커스텀 토스트 임포트
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.mypage.InquiryRequest
import com.bookiibookii.bookiibookii.databinding.FragmentMypQuestionWriteBinding
import kotlinx.coroutines.launch

class MypQuestionWriteFragment : BaseDetailFragment<FragmentMypQuestionWriteBinding>() {

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMypQuestionWriteBinding {
        return FragmentMypQuestionWriteBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mypWriteBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        binding.mypWriteBtn.setOnClickListener {
            val title = binding.mypWriteTitleEt.text.toString().trim()
            val content = binding.mypWriteContentEt.text.toString().trim()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                sendInquiry(title, content)
            } else {
                requireContext().showCustomToast("제목과 내용을 모두 입력해주세요.", false)
            }
        }
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val imeVisible = insets.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime()).bottom
            val navBarHeight = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()).bottom

            v.setPadding(0, 0, 0, if (imeVisible) imeHeight else navBarHeight)
            insets
        }
    }

    private fun sendInquiry(title: String, content: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = InquiryRequest(title, content)
                val response = RetrofitClient.mypApi().postInquiry(request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    requireContext().showCustomToast("문의가 접수되었습니다.", true)
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Log.e("InquiryWrite", "전송 실패: ${response.code()}")
                    requireContext().showCustomToast("문의 전송에 실패했습니다.", false)
                }
            } catch (e: Exception) {
                Log.e("InquiryWrite", "네트워크 오류", e)
                requireContext().showCustomToast("네트워크 오류가 발생했습니다.", false)
            }
        }
    }
}