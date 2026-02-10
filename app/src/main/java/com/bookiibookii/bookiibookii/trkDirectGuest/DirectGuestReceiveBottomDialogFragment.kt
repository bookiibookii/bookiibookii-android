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
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestReceiveBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DirectGuestReceiveBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectGuestReceiveBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    private val vm: DirectGuestViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDirectGuestReceiveBottomDialogBinding.inflate(inflater, container, false)
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

        binding.btnNoReceive.setOnClickListener {
            DirectGuestReceiveIssueDialogFragment
                .newInstance(groupId)
                .show(parentFragmentManager, DirectGuestReceiveIssueDialogFragment.TAG)
        }

        binding.btnReceive.setOnClickListener {
            binding.btnReceive.isEnabled = false
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
                            binding.btnReceive.isEnabled = true
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun formatMeetingTime(raw: String?): String {
        if (raw.isNullOrBlank()) return "-"

        return try {
            val dt = if (raw.endsWith("Z")) {
                OffsetDateTime.parse(raw)
                    .atZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDateTime()
            } else {
                LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }

            dt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
        } catch (_: Exception) {
            "-"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectGuestReceiveFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectGuestReceiveBottomDialogFragment().apply {
                arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
            }
    }

    override fun getTheme(): Int = R.style.Theme_Bookii_BottomSheet_NoDim
}
