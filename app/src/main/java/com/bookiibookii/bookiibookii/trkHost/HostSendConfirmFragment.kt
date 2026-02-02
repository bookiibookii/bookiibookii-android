package com.bookiibookii.bookiibookii.trkHost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bookiibookii.bookiibookii.databinding.FragmentHostSendConfirmBinding

class HostSendConfirmFragment : DialogFragment() {

    private var _binding: FragmentHostSendConfirmBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHostSendConfirmBinding.inflate(inflater, container, false)
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

        binding.btnConfirm.setOnClickListener{
            parentFragmentManager.setFragmentResult(
                HostReceiveConfirmDialogFragment.RESULT_KEY,
                Bundle().apply {
                    putString(HostReceiveConfirmDialogFragment.BUNDLE_ACTION, "GUEST_READING")
                }
            )

            val next = HostReadingStatusBottomDialogFragment()
            val prevBottomSheet = parentFragmentManager.findFragmentByTag(HostShippingStatusBottomDialogFragment.TAG) as? DialogFragment
            dismiss()
            prevBottomSheet?.dismiss()
            next.show(parentFragmentManager, HostReadingStatusBottomDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SendConfirmFragment"
        const val RESULT_KEY = "host_action"
        const val BUNDLE_ACTION = "action"
    }
}