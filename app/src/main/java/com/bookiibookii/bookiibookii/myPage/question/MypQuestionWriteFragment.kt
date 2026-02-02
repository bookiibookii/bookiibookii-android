package com.bookiibookii.bookiibookii.myPage.question

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bookiibookii.bookiibookii.bookData.Data.MypQuestion
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.databinding.FragmentMypQuestionWriteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MypQuestionWriteFragment : Fragment() {
    private var _binding: FragmentMypQuestionWriteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyPageViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypQuestionWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mypWriteBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 전송 버튼
        binding.mypWriteBtn.setOnClickListener {
            val title = binding.mypWriteTitleEt.text.toString()
            val content = binding.mypWriteContentEt.text.toString()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                val today = SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault()).format(Date())

                // 데이터 추가
                viewModel.addQuestion(
                    MypQuestion(
                        id = System.currentTimeMillis(),
                        title = title,
                        content = content,
                        date = today
                    )
                )
                Toast.makeText(context, "문의가 접수되었습니다.", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack() // 목록으로 복귀
            } else {
                Toast.makeText(context, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // ID 확인 필요
        requireActivity().findViewById<View>(com.bookiibookii.bookiibookii.R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}