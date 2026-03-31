package com.bookiibookii.bookiibookii.myPage.question

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.InquiryRequest
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

        // ★ 기존에 있던 중복 WindowInsets 로직(이중 패딩의 원인)을 깨끗하게 삭제했습니다!

        binding.mypWriteBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        binding.mypWriteBtn.setOnClickListener {
            val title = binding.mypWriteTitleEt.text.toString().trim()
            val content = binding.mypWriteContentEt.text.toString().trim()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                sendInquiry(title, content)
            } else {
                Toast.makeText(context, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        setupKeyboardAutoScroll() // ★ 자동 스크롤 적용
    }

    // ★ 텍스트 박스 터치 시 키보드 위로 스크롤을 끌어올리는 함수
    private fun setupKeyboardAutoScroll() {
        val editTexts = listOf(binding.mypWriteTitleEt, binding.mypWriteContentEt)
        editTexts.forEach { et ->
            val scrollAction = {
                et.postDelayed({
                    et.requestRectangleOnScreen(
                        android.graphics.Rect(0, et.height, et.width, et.height), true
                    )
                }, 300)
            }
            et.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) scrollAction() }
            et.setOnClickListener { scrollAction() }
        }
    }

    private fun sendInquiry(title: String, content: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = InquiryRequest(title, content)
                val response = RetrofitClient.api().postInquiry(request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(context, "문의가 접수되었습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Log.e("InquiryWrite", "전송 실패: ${response.code()}")
                    Toast.makeText(context, "문의 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("InquiryWrite", "네트워크 오류", e)
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}