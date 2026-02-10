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
import com.bookiibookii.bookiibookii.databinding.FragmentDirectHostAppointmentEditBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DirectHostAppointmentEditBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectHostAppointmentEditBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    private val vm: DirectHostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectHostAppointmentEditBottomDialogBinding.inflate(inflater, container, false)
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
            DirectHostAppointmentEditDialogFragment
                .newInstance(groupId)
                .show(parentFragmentManager, DirectHostAppointmentEditDialogFragment.TAG)
        }

        binding.btnGoComment.setOnClickListener {
            // TODO: 댓글 화면 이동
        }
    }

    private fun formatTitleDate(rawIso: String?): String {
        val dt = parseIso(rawIso) ?: return ""
        val time = dt.format(DateTimeFormatter.ofPattern("HH:mm"))
        return "${dt.monthValue}월 ${dt.dayOfMonth}일 $time "
    }

    private fun formatCardDateTime(rawIso: String?): String {
        val dt = parseIso(rawIso) ?: return "-"
        return dt.format(DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm "))
    }

    private fun parseIso(rawIso: String?): LocalDateTime? {
        if (rawIso.isNullOrBlank()) return null
        return try {
            LocalDateTime.parse(rawIso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val TAG = "DirectAppointmentEditDialogFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectHostAppointmentEditBottomDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GROUP_ID, groupId)
                }
            }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}

