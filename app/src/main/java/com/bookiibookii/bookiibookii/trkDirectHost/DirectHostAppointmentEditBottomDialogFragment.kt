package com.bookiibookii.bookiibookii.trkDirectHost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectHostAppointmentEditBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DirectHostAppointmentEditBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectHostAppointmentEditBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectHostAppointmentEditBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEditMeet.setOnClickListener{
            val dialog = DirectHostAppointmentEditDialogFragment()
            dialog.show(parentFragmentManager, DirectHostAppointmentEditDialogFragment.TAG)
        }

        binding.btnGoComment.setOnClickListener{
            // 이거 나중에 고치기
            val next = DirectHostExchangeBottomDialogFragment()
            dismiss()
            next.show(parentFragmentManager, DirectHostExchangeBottomDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val TAG = "DirectAppointmentEditDialogFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}

