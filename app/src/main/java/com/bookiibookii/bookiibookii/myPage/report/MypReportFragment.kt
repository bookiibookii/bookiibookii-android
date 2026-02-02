package com.bookiibookii.bookiibookii.myPage.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.databinding.FragmentMypReportBinding


class MypReportFragment : Fragment() {
    private var _binding: FragmentMypReportBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyPageViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mypReportBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.mypReportBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypReportWriteFragment())
                .addToBackStack(null)
                .commit()
        }

        val adapter = MypReportAdapter() // 기존에 만든 어댑터 사용
        binding.mypReportListRv.layoutManager = LinearLayoutManager(context)
        binding.mypReportListRv.adapter = adapter

        viewModel.reportList.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                // 1. 리스트가 비어있으면 -> 리사이클러뷰 숨김 / 안내 뷰 보임
                binding.mypReportListRv.visibility = View.GONE
                binding.mypNoReportCl.visibility = View.VISIBLE
            } else {
                // 2. 리스트가 있으면 -> 리사이클러뷰 보임 / 안내 뷰 숨김
                binding.mypReportListRv.visibility = View.VISIBLE
                binding.mypNoReportCl.visibility = View.GONE
                adapter.submitList(list)
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