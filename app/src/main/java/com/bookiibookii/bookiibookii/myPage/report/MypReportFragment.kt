package com.bookiibookii.bookiibookii.myPage.report

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentMypReportBinding
import kotlinx.coroutines.launch

class MypReportFragment : Fragment() {
    private var _binding: FragmentMypReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog
    private lateinit var adapter: MypReportAdapter // ★ 지연 초기화로 변경

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypReportBinding.inflate(inflater, container, false)
        return binding.root
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

        // ★ 어댑터에 클릭 리스너 달아주기
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
        lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().getReportList()

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

    override fun onResume() {
        super.onResume()
        fetchReportList()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}