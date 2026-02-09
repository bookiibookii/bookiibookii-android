package com.bookiibookii.bookiibookii.trkGuest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentGuestReadingStatusBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class GuestReadingStatusBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentGuestReadingStatusBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuestReadingStatusBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGoRead.setOnClickListener{
            // TODO: 독서카드 확인하러 가기
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "GuestReadingStatusFragment"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}