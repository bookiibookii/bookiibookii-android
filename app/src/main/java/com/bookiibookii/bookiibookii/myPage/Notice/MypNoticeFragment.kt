package com.bookiibookii.bookiibookii.myPage.Notice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.databinding.FragmentMypNoticeBinding

class MypNoticeFragment  : Fragment() {
    private var _binding: FragmentMypNoticeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyPageViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypNoticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mypNoticeBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        val adapter = MypNoticeAdapter { notice ->
            // 상세 화면 이동
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypNoticeDetailFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.rvNoticeList.layoutManager = LinearLayoutManager(context)
        binding.rvNoticeList.adapter = adapter

        viewModel.noticeList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
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