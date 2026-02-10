package com.bookiibookii.bookiibookii.trkHost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentHostReadingDoneBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class HostReadingDoneBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentHostReadingDoneBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

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

            dismiss()
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

        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) = HostReadingDoneBottomDialogFragment().apply {
            arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
        }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}