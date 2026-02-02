package com.bookiibookii.bookiibookii.trkDirectHost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectHostReadingBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DirectHostReadingBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectHostReadingBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectHostReadingBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnExtendPeriod.setOnClickListener{
            val dialog = DirectHostExtendPeriodDialogFragment()
            dialog.show(parentFragmentManager, DirectHostExtendPeriodDialogFragment.TAG)
        }

        binding.btnFinish.setOnClickListener{
            val next = DirectHostAppointmentBottomDialogFragment()
            dismiss()
            next.show(parentFragmentManager, DirectHostAppointmentBottomDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val TAG = "DirectReadingBottomSheetDialogFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}
