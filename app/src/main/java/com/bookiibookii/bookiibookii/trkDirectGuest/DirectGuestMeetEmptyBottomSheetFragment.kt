package com.bookiibookii.bookiibookii.trkDirectGuest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestMeetEmptyBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DirectGuestMeetEmptyBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectGuestMeetEmptyBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentDirectGuestMeetEmptyBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirm.setOnClickListener{
            val next = DirectGuestAppointmentStatusBottomDialogFragment()
            dismiss()
            next.show(parentFragmentManager, DirectGuestAppointmentStatusBottomDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectGuestMeetEmptyFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}

