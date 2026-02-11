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
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestStartBottomDialogBinding
import com.bookiibookii.bookiibookii.trkHost.TrackerDateUtil.prettyDate
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class DirectGuestStartBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectGuestStartBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val vm: DirectGuestViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDirectGuestStartBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.loadTracker(groupId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.trackerState.collectLatest { state ->
                    when (state) {
                        UiState.Idle -> Unit

                        UiState.Loading -> {
                            binding.tvStartDate.text = "-"
                            binding.tvEndDate.text = "-"
                        }

                        is UiState.Success -> {
                            val dto = state.data
                            val startRaw = dto?.startDate
                            val periodDays = dto?.readingPeriod ?: 0

                            binding.tvStartDate.text = prettyDate(dto?.startDate)

                            val endRaw = addDaysFromStartDate(startRaw, periodDays)
                            binding.tvEndDate.text = prettyDate(endRaw)
                        }

                        is UiState.Error -> {
                            binding.tvStartDate.text = "-"
                            binding.tvEndDate.text = "-"
                        }
                    }
                }
            }
        }

        binding.btnStart.setOnClickListener {
            binding.btnStart.isEnabled = false
            vm.startReading(groupId)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.event.collect { ev ->
                    when (ev) {
                        DirectGuestEvent.ReadingStartSuccess -> {
                            vm.loadTracker(groupId)
                            binding.btnStart.isEnabled = true
                            dismissAllowingStateLoss()
                        }

                        is DirectGuestEvent.ReadingStartFail -> {
                            binding.btnStart.isEnabled = true
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun addDaysFromStartDate(raw: String?, days: Int): String? {
        if (raw.isNullOrBlank()) return raw
        if (days <= 0) return raw

        return try {
            if (raw.endsWith("Z")) {
                OffsetDateTime.parse(raw).plusDays(days.toLong()).toString()
            } else {
                val datePart = raw.substring(0, 10)
                val d = LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE)
                val newDate = d.plusDays(days.toLong())
                newDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + raw.substring(10)
            }
        } catch (_: Exception) {
            raw
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectGuestStartBottomSheetFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectGuestStartBottomDialogFragment().apply {
                arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
            }
    }

    override fun getTheme(): Int = R.style.Theme_Bookii_BottomSheet_NoDim
}
