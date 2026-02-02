package com.bookiibookii.bookiibookii.trkDirectHost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectHostExtendRequestBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DirectHostExtendRequestBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectHostExtendRequestBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectHostExtendRequestBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirm.setOnClickListener{
            val next = DirectHostMeetEmptyBottomSheetFragment()
            dismiss()
            next.show(parentFragmentManager, DirectHostMeetEmptyBottomSheetFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectExtendRequestFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}