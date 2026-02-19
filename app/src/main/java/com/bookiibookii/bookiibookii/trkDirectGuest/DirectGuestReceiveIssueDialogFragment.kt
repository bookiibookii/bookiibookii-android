package com.bookiibookii.bookiibookii.trkDirectGuest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestReceiveIssueDialogBinding
import com.bookiibookii.bookiibookii.trkDirectHost.DirectHostAppointmentEditDialogFragment

class DirectGuestReceiveIssueDialogFragment : DialogFragment() {

    private var _binding: FragmentDirectGuestReceiveIssueDialogBinding? = null
    private val binding get() = _binding!!

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectGuestReceiveIssueDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.90).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener{dismiss()}

        binding.btnReport.setOnClickListener{
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("NAV_ACTION", "OPEN_MYP_REPORT")
            }
            startActivity(intent)
            dismissAllowingStateLoss()
        }

        binding.btnReschedule.setOnClickListener{
            dismiss()

            DirectHostAppointmentEditDialogFragment
                .newInstance(groupId)
                .show(parentFragmentManager, DirectHostAppointmentEditDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectGuestReceiveIssueDialogFragment"

        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectGuestReceiveIssueDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GROUP_ID, groupId)
                }
            }
    }
}


