package com.bookiibookii.bookiibookii.trkDirectGuest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestReadingBottomDialogBinding
import com.bookiibookii.bookiibookii.trkDirectHost.DateTimeUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DirectGuestReadingBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectGuestReadingBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    private val vm: DirectGuestViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDirectGuestReadingBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.loadTracker(groupId)

        binding.btnWriteCard.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("NAV_ACTION", "OPEN_ADD_CARD")
                putExtra("target_group_id", groupId)
            }
            startActivity(intent)
            dismiss()
        }

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
                            binding.tvStartDate.text = DateTimeUtils.formatMeetingTime(dto.startDate)
                            binding.tvEndDate.text = DateTimeUtils.formatMeetingTime(dto.endDate)

                            val alreadyExtended = (dto.extensionCount ?: 0) >= 1
                            binding.btnExtendPeriod.isEnabled = !alreadyExtended
                            binding.btnExtendPeriod.alpha = if (alreadyExtended) 0.5f else 1f
                        }

                        is UiState.Error -> {
                            binding.tvStartDate.text = "-"
                            binding.tvEndDate.text = "-"
                        }
                    }
                }
            }
        }

        binding.btnExtendPeriod.setOnClickListener {
            DirectGuestExtendPeriodDialogFragment
                .newInstance(groupId)
                .show(parentFragmentManager, DirectGuestExtendPeriodDialogFragment.TAG)
        }

        binding.btnFinish.setOnClickListener {
            binding.btnFinish.isEnabled = false
            vm.doneTracker(groupId)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.event.collect { ev ->
                    when (ev) {
                        is DirectGuestEvent.DoneSuccess -> {
                            binding.btnFinish.isEnabled = true
                            vm.loadTracker(groupId)
                            dismissAllowingStateLoss()
                        }

                        is DirectGuestEvent.DoneFail -> {
                            binding.btnFinish.isEnabled = true
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
        const val TAG = "DirectGuestReadingBottomSheetDialogFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectGuestReadingBottomDialogFragment().apply {
                arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
            }
    }

    override fun getTheme(): Int = R.style.Theme_Bookii_BottomSheet_NoDim
}
