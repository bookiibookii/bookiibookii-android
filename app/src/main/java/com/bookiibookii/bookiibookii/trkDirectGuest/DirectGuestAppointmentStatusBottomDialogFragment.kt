package com.bookiibookii.bookiibookii.trkDirectGuest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestAppointmentStatusBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DirectGuestAppointmentStatusBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectGuestAppointmentStatusBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectGuestAppointmentStatusBottomDialogBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGoChat.setOnClickListener{
            val next = DirectGuestReceiveBottomDialogFragment()
            dismiss()
            next.show(parentFragmentManager, DirectGuestReceiveBottomDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectGusetAppointmentStatusFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}


