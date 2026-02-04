package com.bookiibookii.bookiibookii.trkDirectGuest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestAppointmentEditBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DirectGuestAppointmentEditBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectGuestAppointmentEditBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectGuestAppointmentEditBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEditMeet.setOnClickListener{
            val dialog = DirectGuestAppointmentEditDialogFragment()
            dialog.show(parentFragmentManager, DirectGuestAppointmentEditDialogFragment.TAG)
        }

        binding.btnGoComment.setOnClickListener{
            // 이거 나중에 고치기
            val next = DirectGuestExchangeBottomDialogFragment()
            dismiss()
            next.show(parentFragmentManager, DirectGuestExchangeBottomDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val TAG = "DirectGuestAppointmentEditDialogFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}

