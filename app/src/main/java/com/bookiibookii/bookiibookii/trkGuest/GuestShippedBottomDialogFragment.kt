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

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuestShippedBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnViewShippingPhoto.setOnClickListener {
            GuestShippingPhotoDialogFragment
                .newInstance(groupId)
                .show(parentFragmentManager, GuestShippingPhotoDialogFragment.TAG)
        }

        binding.btnDoReceiveConfirm.setOnClickListener {
            GuestReceiveConfirmDialogFragment
                .newInstance(groupId)
                .show(parentFragmentManager, GuestReceiveConfirmDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "GuestShippedFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) = GuestShippedBottomDialogFragment().apply {
            arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
        }
    }

    override fun getTheme(): Int = R.style.Theme_Bookii_BottomSheet_NoDim
}
