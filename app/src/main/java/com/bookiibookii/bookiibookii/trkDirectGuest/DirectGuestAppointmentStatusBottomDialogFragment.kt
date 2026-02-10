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
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestAppointmentStatusBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class DirectGuestAppointmentStatusBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectGuestAppointmentStatusBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    private val vm: DirectGuestViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDirectGuestAppointmentStatusBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitleDate.text = "-"
        binding.tvAppointmentDatetime.text = "-"
        binding.tvAppointmentPlace.text = "-"

        vm.loadMeeting(groupId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.meetingState.collectLatest { state ->
                    when (state) {
                        UiState.Idle -> Unit
                        UiState.Loading -> Unit

                        is UiState.Success -> {
                            val dto = state.data
                            val meetingTime = dto.meetingTime

                            binding.tvTitleDate.text = formatTitleDate(meetingTime)
                            binding.tvAppointmentDatetime.text = formatCardDateTime(meetingTime)
                            binding.tvAppointmentPlace.text = dto.meetingPlace ?: "-"
                        }

                        is UiState.Error -> {
                            binding.tvTitleDate.text = "-"
                            binding.tvAppointmentDatetime.text = "-"
                            binding.tvAppointmentPlace.text = "-"
                        }
                    }
                }
            }
        }

        binding.btnGoChat.setOnClickListener {

        }
    }

    private fun formatTitleDate(raw: String?): String {
        if (raw.isNullOrBlank()) return "-"

        val dt = parseAnyDateTime(raw) ?: return "-"

        val time = dt.format(DateTimeFormatter.ofPattern("HH:mm"))
        return "${dt.monthValue}월 ${dt.dayOfMonth}일 $time"
    }

    private fun formatCardDateTime(raw: String?): String {
        if (raw.isNullOrBlank()) return "-"

        val dt = parseAnyDateTime(raw) ?: return "-"

        return dt.format(DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm"))
    }

    private fun parseAnyDateTime(raw: String): LocalDateTime? {
        return try {
            if (raw.endsWith("Z")) {
                OffsetDateTime.parse(raw).toLocalDateTime()
            } else {
                LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectGuestAppointmentStatusFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectGuestAppointmentStatusBottomDialogFragment().apply {
                arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
            }
    }

    override fun getTheme(): Int = R.style.Theme_Bookii_BottomSheet_NoDim
}
