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
import com.bookiibookii.bookiibookii.databinding.FragmentDirectHostExchangeBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DirectHostExchangeBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectHostExchangeBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val vm: DirectHostViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectHostExchangeBottomDialogBinding.inflate(inflater, container, false)
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

        binding.btnNoSend.setOnClickListener {
            DirectHostMeetIssueDialogFragment
                .newInstance(groupId)
                .show(parentFragmentManager, DirectHostMeetIssueDialogFragment.TAG)
        }

        binding.btnSend.setOnClickListener {
            binding.btnSend.isEnabled = false
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
                            binding.btnSend.isEnabled = true
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
                java.time.OffsetDateTime.parse(raw)
                    .atZoneSameInstant(java.time.ZoneId.systemDefault())
                    .toLocalDateTime()
            } else {
                LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }

            dt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
        } catch (e: Exception) {
            "-"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val TAG = "DirectExchangeDialogFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectHostExchangeBottomDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GROUP_ID, groupId)
                }
            }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}

