package com.bookiibookii.bookiibookii.trkGuest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentGuestShippedBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GuestShippedBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentGuestShippedBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuestShippedBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnViewShippingPhoto.setOnClickListener{
            val dialog = GuestShippingPhotoDialogFragment()
            dialog.show(parentFragmentManager, GuestShippingPhotoDialogFragment.TAG)
        }

        binding.btnDoReceiveConfirm.setOnClickListener{
            val dialog = GuestReceiveConfirmDialogFragment()
            dialog.show(parentFragmentManager, GuestReceiveConfirmDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "GuestShippedFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}