package com.bookiibookii.bookiibookii.trkHost

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentHostExtendRequestBottomDialogBinding
import com.bookiibookii.bookiibookii.databinding.FragmentHostReadingDoneBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class HostReadingDoneBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentHostReadingDoneBottomDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHostReadingDoneBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGoCard.setOnClickListener{
            parentFragmentManager.setFragmentResult(
                HostReadingDoneBottomDialogFragment.RESULT_KEY,
                Bundle().apply {
                    putString(HostReadingDoneBottomDialogFragment.BUNDLE_ACTION, "GUEST_SHIPPED")
                }
            )

            val next = HostShippedBottomDialogFragment()
            dismiss()
            next.show(parentFragmentManager, HostShippedBottomDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ReadingDoneFragment"
        const val RESULT_KEY = "host_action"
        const val BUNDLE_ACTION = "action"
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}