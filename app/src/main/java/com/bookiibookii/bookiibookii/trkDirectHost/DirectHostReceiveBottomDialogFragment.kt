package com.bookiibookii.bookiibookii.trkDirectHost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectHostReceiveBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DirectHostReceiveBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectHostReceiveBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectHostReceiveBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNoReceive.setOnClickListener{
            val dialog = DirectHostReceiveIssueDialogFragment()
            dialog.show(parentFragmentManager, DirectHostReceiveIssueDialogFragment.TAG)
        }

        binding.btnReceive.setOnClickListener{
            val next = DirectTradeFinishBottomDialogFragment()
            dismiss()
            next.show(parentFragmentManager, DirectTradeFinishBottomDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectReceiveFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}


