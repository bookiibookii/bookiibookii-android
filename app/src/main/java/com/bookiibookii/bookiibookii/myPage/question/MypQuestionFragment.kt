package com.bookiibookii.bookiibookii.myPage.question

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentMypQuestionBinding
import kotlinx.coroutines.launch

class MypQuestionFragment : Fragment() {
    private var _binding: FragmentMypQuestionBinding? = null
    private val binding get() = _binding!!

    // 어댑터 선언
    private val adapter = MypQuestionAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mypQuestionBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 문의 작성하기 버튼
        binding.mypQuestionBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypQuestionWriteFragment())
                .addToBackStack(null)
                .commit()
        }

        // 리사이클러뷰 설정
        binding.mypQuestionListRv.layoutManager = LinearLayoutManager(context)
        binding.mypQuestionListRv.adapter = adapter

        // 데이터 불러오기
        fetchInquiryList()
    }

    private fun fetchInquiryList() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getInquiryList()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val list = response.body()!!.result

                    if (list.isNullOrEmpty()) {
                        // 데이터 없을 때
                        binding.mypQuestionListRv.visibility = View.GONE
                        binding.mypNoQuestionCl.visibility = View.VISIBLE
                    } else {
                        // 데이터 있을 때
                        binding.mypQuestionListRv.visibility = View.VISIBLE
                        binding.mypNoQuestionCl.visibility = View.GONE
                        adapter.submitList(list)
                    }
                } else {
                    Log.e("Inquiry", "리스트 조회 실패: ${response.code()}")
                    // 실패 시에도 비어있는 화면 처리 (혹은 에러 토스트)
                    binding.mypQuestionListRv.visibility = View.GONE
                    binding.mypNoQuestionCl.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("Inquiry", "네트워크 오류", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 작성 후 돌아왔을 때 리스트 갱신
        fetchInquiryList()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}