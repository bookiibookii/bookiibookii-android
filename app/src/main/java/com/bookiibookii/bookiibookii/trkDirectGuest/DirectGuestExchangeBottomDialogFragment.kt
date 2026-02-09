package com.bookiibookii.bookiibookii.trkDirectGuest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestExchangeBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class DirectGuestExchangeBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectGuestExchangeBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val vm: DirectGuestViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDirectGuestExchangeBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNoSend.setOnClickListener {
            DirectGuestMeetIssueDialogFragment
                .newInstance(groupId)
                .show(parentFragmentManager, DirectGuestMeetIssueDialogFragment.TAG)
        }

        binding.btnSend.setOnClickListener {
            binding.btnSend.isEnabled = false
            vm.completeMeeting(groupId)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.event.collect { ev ->
                    when (ev) {
                        is DirectGuestEvent.ExchangeCompleteSuccess -> {
                            vm.loadTracker(groupId)
                            dismissAllowingStateLoss()
                        }

                        is DirectGuestEvent.ExchangeCompleteFail -> {
                            binding.btnSend.isEnabled = true
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectGuestExchangeDialogFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectGuestExchangeBottomDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GROUP_ID, groupId)
                }
            }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}
