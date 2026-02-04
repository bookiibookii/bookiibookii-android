package com.bookiibookii.bookiibookii.myPage.question

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.InquiryRequest
import com.bookiibookii.bookiibookii.databinding.FragmentMypQuestionWriteBinding
import kotlinx.coroutines.launch

class MypQuestionWriteFragment : Fragment() {
    private var _binding: FragmentMypQuestionWriteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypQuestionWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mypWriteBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 전송 버튼 클릭
        binding.mypWriteBtn.setOnClickListener {
            val title = binding.mypWriteTitleEt.text.toString().trim()
            val content = binding.mypWriteContentEt.text.toString().trim()

            // XML에 기본 text가 "문의 내용을 입력해주세요."로 되어 있다면,
            // 사용자가 수정하지 않고 그대로 보낼 수도 있으니 체크하거나,
            // 실제 앱에서는 hint로 바꾸는 것이 좋습니다.
            // 여기서는 일단 비어있는지만 체크합니다.

            if (title.isNotEmpty() && content.isNotEmpty()) {
                sendInquiry(title, content)
            } else {
                Toast.makeText(context, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendInquiry(title: String, content: String) {
        lifecycleScope.launch {
            try {
                val request = InquiryRequest(title, content)
                val response = RetrofitClient.getInstance(requireContext()).postInquiry(request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(context, "문의가 접수되었습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack() // 목록 화면으로 돌아가기 (자동 갱신됨)
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

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}