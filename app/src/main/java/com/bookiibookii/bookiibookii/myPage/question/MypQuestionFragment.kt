package com.bookiibookii.bookiibookii.myPage.question

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.databinding.FragmentMypQuestionBinding


class MypQuestionFragment : Fragment() {
    private var _binding: FragmentMypQuestionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyPageViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mypQuestionBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.mypQuestionBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypQuestionWriteFragment())
                .addToBackStack(null)
                .commit()
        }

        val adapter = MypQuestionAdapter() // 기존에 만든 어댑터 사용
        binding.mypQuestionListRv.layoutManager = LinearLayoutManager(context)
        binding.mypQuestionListRv.adapter = adapter

        viewModel.questionList.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                binding.mypQuestionListRv.visibility = View.GONE
                binding.mypNoQuestionCl.visibility = View.VISIBLE
            } else {
                binding.mypQuestionListRv.visibility = View.VISIBLE
                binding.mypNoQuestionCl.visibility = View.GONE
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