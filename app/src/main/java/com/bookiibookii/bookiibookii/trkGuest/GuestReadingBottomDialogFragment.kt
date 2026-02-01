package com.bookiibookii.bookiibookii.trkGuest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentGuestReadingBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GuestReadingBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentGuestReadingBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuestReadingBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnExtendPeriod.setOnClickListener{
            val dialog = GuestExtendPeriodDialogFragment()
            dialog.show(parentFragmentManager, GuestExtendPeriodDialogFragment.TAG)
        }

        binding.btnFinish.setOnClickListener{
            val next = GuestShippingBottomDialogFragment()
            dismiss()
            next.show(parentFragmentManager, GuestShippingBottomDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val TAG = "GuestReadingBottomSheetDialogFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}