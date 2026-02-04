package com.bookiibookii.bookiibookii.trkDirectGuest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestReadingBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DirectGuestReadingBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectGuestReadingBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectGuestReadingBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnExtendPeriod.setOnClickListener{
            val dialog = DirectGuestExtendPeriodDialogFragment()
            dialog.show(parentFragmentManager, DirectGuestExtendPeriodDialogFragment.TAG)
        }

        binding.btnFinish.setOnClickListener{
            val next = DirectGuestAppointmentBottomDialogFragment()
            dismiss()
            next.show(parentFragmentManager, DirectGuestAppointmentBottomDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val TAG = "DirectGuestReadingBottomSheetDialogFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}
