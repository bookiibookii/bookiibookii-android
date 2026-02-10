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

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        binding.btnConfirm.setOnClickListener {

            parentFragmentManager.setFragmentResult(
                RESULT_KEY,
                Bundle().apply {
                    putString(BUNDLE_ACTION, "GUEST_READING")
                    putLong(ARG_GROUP_ID, groupId)
                }
            )

            (parentFragmentManager
                .findFragmentByTag(HostShippingStatusBottomDialogFragment.TAG) as? DialogFragment)
                ?.dismissAllowingStateLoss()

            dismissAllowingStateLoss()
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
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) = HostSendConfirmFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_GROUP_ID, groupId)
            }
        }
    }
}
