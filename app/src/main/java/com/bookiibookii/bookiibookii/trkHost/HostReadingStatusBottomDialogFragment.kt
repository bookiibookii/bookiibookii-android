package com.bookiibookii.bookiibookii.trkHost

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentHostReadingStatusBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class HostReadingStatusBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentHostReadingStatusBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHostReadingStatusBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGoRead.setOnClickListener{
            val next = HostExtendRequestBottomDialogFragment()
            dismiss()
            next.show(parentFragmentManager, HostExtendRequestBottomDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ReadingStatusFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}