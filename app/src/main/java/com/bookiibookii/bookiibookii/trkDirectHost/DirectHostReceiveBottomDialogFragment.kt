package com.bookiibookii.bookiibookii.trkDirectHost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectHostReceiveBottomDialogBinding
import com.bookiibookii.bookiibookii.trkDirectHost.DateTimeUtils.formatMeetingTime
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DirectHostReceiveBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectHostReceiveBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val vm: DirectHostViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectHostReceiveBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.loadMeeting(groupId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.meetingState.collectLatest { state ->
                    when (state) {
                        UiState.Idle -> Unit

                        UiState.Loading -> {
                            binding.tvAppointmentDatetime.text = "-"
                            binding.tvAppointmentPlace.text = "-"
                        }

                        is UiState.Success -> {
                            val dto = state.data
                            binding.tvAppointmentDatetime.text = formatMeetingTime(dto.meetingTime)

                            binding.tvAppointmentPlace.text = dto.meetingPlace ?: "-"
                        }

                        is UiState.Error -> {
                            binding.tvAppointmentDatetime.text = "-"
                            binding.tvAppointmentPlace.text = "-"
                        }
                    }
                }
            }
        }

        binding.btnNoReceive.setOnClickListener{
            DirectHostReceiveIssueDialogFragment
                .newInstance(groupId)
                .show(parentFragmentManager, DirectHostReceiveIssueDialogFragment.TAG)
        }

        binding.btnReceive.setOnClickListener{
            binding.btnReceive.isEnabled = false
            vm.completeMeeting(groupId)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.event.collect { ev ->
                    when (ev) {
                        is DirectHostEvent.ExchangeCompleteSuccess -> {
                            vm.loadTracker(groupId)

                            dismissAllowingStateLoss()
                        }

                        is DirectHostEvent.ExchangeCompleteFail -> {
                            binding.btnReceive.isEnabled = true
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
        const val TAG = "DirectReceiveFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectHostReceiveBottomDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GROUP_ID, groupId)
                }
            }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}


