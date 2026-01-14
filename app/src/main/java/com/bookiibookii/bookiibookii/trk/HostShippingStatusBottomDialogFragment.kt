package com.bookiibookii.bookiibookii.trk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentHostShippingStatusBottomDialogBinding
import com.bookiibookii.bookiibookii.databinding.FragmentReadingBottomSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HostShippingStatusBottomDialogFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentHostShippingStatusBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHostShippingStatusBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val TAG = "StatusBottomSheetDialogFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}