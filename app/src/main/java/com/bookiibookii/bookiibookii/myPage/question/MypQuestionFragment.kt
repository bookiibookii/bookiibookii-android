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
import com.bookiibookii.bookiibookii.common.LoadingDialog // ★ 로딩 다이얼로그 import
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentMypQuestionBinding
import kotlinx.coroutines.launch

class MypQuestionFragment : Fragment() {
    private var _binding: FragmentMypQuestionBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog // ★ 로딩 선언

    private val adapter = MypQuestionAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext()) // ★ 로딩 초기화

        binding.mypQuestionBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        binding.mypQuestionBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypQuestionWriteFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.mypQuestionListRv.layoutManager = LinearLayoutManager(context)
        binding.mypQuestionListRv.adapter = adapter

        fetchInquiryList()
    }

    private fun fetchInquiryList() {
        lifecycleScope.launch {
            loadingDialog.show() // ★ API 호출 전 로딩 시작
            try {
                val response = RetrofitClient.api().getInquiryList()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val list = response.body()!!.result

                    if (list.isNullOrEmpty()) {
                        binding.mypQuestionListRv.visibility = View.GONE
                        binding.mypNoQuestionCl.visibility = View.VISIBLE
                    } else {
                        binding.mypQuestionListRv.visibility = View.VISIBLE
                        binding.mypNoQuestionCl.visibility = View.GONE
                        adapter.submitList(list)
                    }
                } else {
                    Log.e("Inquiry", "리스트 조회 실패: ${response.code()}")
                    binding.mypQuestionListRv.visibility = View.GONE
                    binding.mypNoQuestionCl.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("Inquiry", "네트워크 오류", e)
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss() // ★ 무조건 로딩 끝내기
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchInquiryList()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}