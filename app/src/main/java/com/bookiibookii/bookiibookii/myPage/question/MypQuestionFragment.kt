package com.bookiibookii.bookiibookii.myPage.question

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentMypQuestionBinding
import kotlinx.coroutines.launch

class MypQuestionFragment : BaseDetailFragment<FragmentMypQuestionBinding>() {

    private lateinit var loadingDialog: LoadingDialog
    private val adapter = MypQuestionAdapter()

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMypQuestionBinding {
        return FragmentMypQuestionBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

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
        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
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
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchInquiryList()
    }
}