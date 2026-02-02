package com.bookiibookii.bookiibookii.trkDirectHost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectHostExchangeBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DirectHostExchangeBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectHostExchangeBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectHostExchangeBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNoSend.setOnClickListener{
            val dialog = DirectHostMeetIssueDialogFragment()
            dialog.show(parentFragmentManager, DirectHostMeetIssueDialogFragment.TAG)
        }

        binding.btnSend.setOnClickListener{
            val next = DirectReadingStatusBottomDialogFragment()
            dismiss()
            next.show(parentFragmentManager, DirectReadingStatusBottomDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val TAG = "DirectExchangeDialogFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}

