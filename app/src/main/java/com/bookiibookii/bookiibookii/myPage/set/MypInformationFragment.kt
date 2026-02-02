package com.bookiibookii.bookiibookii.myPage.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentMypInformationBinding

class MypInformationFragment : Fragment() {
    private var _binding: FragmentMypInformationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mypInfoBackIv.setOnClickListener { parentFragmentManager.popBackStack() }
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