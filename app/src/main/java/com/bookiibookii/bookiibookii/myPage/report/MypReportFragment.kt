package com.bookiibookii.bookiibookii.myPage.report

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
import com.bookiibookii.bookiibookii.databinding.FragmentMypReportBinding
import kotlinx.coroutines.launch

class MypReportFragment : BaseDetailFragment<FragmentMypReportBinding>() {

    private lateinit var loadingDialog: LoadingDialog
    private lateinit var adapter: MypReportAdapter

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMypReportBinding {
        return FragmentMypReportBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        binding.mypReportBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.mypReportBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypReportWriteFragment())
                .addToBackStack(null)
                .commit()
        }

        adapter = MypReportAdapter { clickedItem ->
            val detailFragment = MypReportDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("groupName", clickedItem.groupName)
                    putString("reportType", clickedItem.reportType)
                    putString("content", clickedItem.content)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.mypReportListRv.layoutManager = LinearLayoutManager(context)
        binding.mypReportListRv.adapter = adapter

        fetchReportList()
    }

    private fun fetchReportList() {
        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.mypApi().getReportList()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val list = response.body()!!.result

                    if (list.isNullOrEmpty()) {
                        binding.mypReportListRv.visibility = View.GONE
                        binding.mypNoReportCl.visibility = View.VISIBLE
                    } else {
                        binding.mypReportListRv.visibility = View.VISIBLE
                        binding.mypNoReportCl.visibility = View.GONE
                        adapter.submitList(list)
                    }
                } else {
                    Log.e("Report", "리스트 조회 실패: ${response.code()}")
                    binding.mypReportListRv.visibility = View.GONE
                    binding.mypNoReportCl.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("Report", "네트워크 오류", e)
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }
}