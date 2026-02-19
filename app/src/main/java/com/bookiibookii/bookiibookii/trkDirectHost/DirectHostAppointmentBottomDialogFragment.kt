package com.bookiibookii.bookiibookii.trkDirectHost

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectHostAppointmentBottomDialogBinding
import com.bookiibookii.bookiibookii.group.GroupDetailActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DirectHostAppointmentBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectHostAppointmentBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectHostAppointmentBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGoComment.setOnClickListener{
            val intent = Intent(requireContext(), GroupDetailActivity::class.java).apply {
                putExtra("GROUP_ID", groupId)
                putExtra("GROUP_TYPE", "RELAY")
            }
            startActivity(intent)
            dismiss()
        }

        binding.btnRegisterMeet.setOnClickListener{
            DirectHostSetAppointmentDialogFragment
                .newInstance(groupId)
                .show(parentFragmentManager, DirectHostSetAppointmentDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectAppointmentFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectHostAppointmentBottomDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GROUP_ID, groupId)
                }
            }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }

}