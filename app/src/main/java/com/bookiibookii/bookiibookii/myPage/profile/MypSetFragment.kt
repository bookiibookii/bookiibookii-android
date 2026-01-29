package com.bookiibookii.bookiibookii.myPage.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentMypSetBinding

class MypSetFragment : Fragment() {

    private var _binding: FragmentMypSetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypSetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로가기
        binding.mypSettingBackIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 스위치 리스너 등 추가 구현 가능
    }

    override fun onResume() {
        super.onResume()
        // 하단 탭 숨기기
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav)
        bottomNav?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}