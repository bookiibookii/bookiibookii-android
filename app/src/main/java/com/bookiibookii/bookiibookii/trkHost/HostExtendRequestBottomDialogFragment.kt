package com.bookiibookii.bookiibookii.trkHost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentHostExtendPeriodDialogBinding
import com.bookiibookii.bookiibookii.databinding.FragmentHostExtendRequestBottomDialogBinding
import com.bookiibookii.bookiibookii.databinding.FragmentHostReadingStatusBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HostExtendRequestBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentHostExtendRequestBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHostExtendRequestBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirm.setOnClickListener{
            parentFragmentManager.setFragmentResult(
                HostExtendRequestBottomDialogFragment.RESULT_KEY,
                Bundle().apply {
                    putString(HostExtendRequestBottomDialogFragment.BUNDLE_ACTION, "GUEST_SHIPPING_READY")
                }
            )

            val next = HostReadingDoneBottomDialogFragment()
            dismiss()
            next.show(parentFragmentManager, HostReadingDoneBottomDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ExtendRequestFragment"
        const val RESULT_KEY = "host_action"
        const val BUNDLE_ACTION = "action"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}