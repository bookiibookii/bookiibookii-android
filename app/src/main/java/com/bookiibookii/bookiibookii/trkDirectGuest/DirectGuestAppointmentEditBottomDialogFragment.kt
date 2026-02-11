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
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestAppointmentEditBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class DirectGuestAppointmentEditBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectGuestAppointmentEditBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    private val vm: DirectGuestViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDirectGuestAppointmentEditBottomDialogBinding.inflate(inflater, container, false)
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

                        }

                        is UiState.Success -> {
                            val dto = state.data
                            val rawTime = dto.meetingTime
                            val place = dto.meetingPlace

                            binding.tvTitleDate.text = formatTitleDate(rawTime)
                            binding.tvAppointmentDatetime.text = formatCardDateTime(rawTime)
                            binding.tvAppointmentPlace.text = place ?: "-"
                        }

                        is UiState.Error -> {
                            binding.tvTitleDate.text = ""
                            binding.tvAppointmentDatetime.text = "-"
                            binding.tvAppointmentPlace.text = "-"
                        }
                    }
                }
            }
        }

        binding.btnEditMeet.setOnClickListener {
            DirectGuestAppointmentEditDialogFragment
                .newInstance(groupId)
                .show(parentFragmentManager, DirectGuestAppointmentEditDialogFragment.TAG)
        }

        binding.btnGoComment.setOnClickListener {
            // TODO: 댓글/채팅 화면 이동
        }
    }

    private fun formatTitleDate(raw: String?): String {
        val dt = parseAnyDateTime(raw) ?: return ""
        val time = dt.format(DateTimeFormatter.ofPattern("HH:mm"))
        return "${dt.monthValue}월 ${dt.dayOfMonth}일 $time "
    }

    private fun formatCardDateTime(raw: String?): String {
        val dt = parseAnyDateTime(raw) ?: return "-"
        return dt.format(DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm "))
    }

    private fun parseAnyDateTime(raw: String?): LocalDateTime? {
        if (raw.isNullOrBlank()) return null
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
        const val TAG = "DirectGuestAppointmentEditBottomDialogFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectGuestAppointmentEditBottomDialogFragment().apply {
                arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
            }
    }

    override fun getTheme(): Int = R.style.Theme_Bookii_BottomSheet_NoDim
}
