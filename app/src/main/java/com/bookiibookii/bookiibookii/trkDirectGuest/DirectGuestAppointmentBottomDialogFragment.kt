package com.bookiibookii.bookiibookii.trkDirectGuest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestAppointmentBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DirectGuestAppointmentBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectGuestAppointmentBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectGuestAppointmentBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegisterMeet.setOnClickListener{
            val dialog = DirectGuestSetAppointmentDialogFragment()
            dialog.show(parentFragmentManager, DirectGuestSetAppointmentDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectGuestAppointmentFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }

}