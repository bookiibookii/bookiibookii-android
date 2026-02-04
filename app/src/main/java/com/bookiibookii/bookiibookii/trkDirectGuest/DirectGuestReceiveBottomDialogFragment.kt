package com.bookiibookii.bookiibookii.trkDirectGuest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestReceiveBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DirectGuestReceiveBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectGuestReceiveBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectGuestReceiveBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNoReceive.setOnClickListener{
            val dialog = DirectGuestReceiveIssueDialogFragment()
            dialog.show(parentFragmentManager, DirectGuestReceiveIssueDialogFragment.TAG)
        }

        binding.btnReceive.setOnClickListener{
            val next = DirectGuestStartBottomDialogFragment()
            dismiss()
            next.show(parentFragmentManager, DirectGuestStartBottomDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectGuestReceiveFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}


