package com.bookiibookii.bookiibookii.trkGuest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentGuestReadingBottomDialogBinding
import com.bookiibookii.bookiibookii.trkHost.TrackerDateUtil.prettyDate
import com.bookiibookii.bookiibookii.trkHost.UiState
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GuestReadingBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentGuestReadingBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val vm: GuestViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuestReadingBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.resetDoneState()
        vm.loadTracker(groupId)

        binding.btnExtendPeriod.setOnClickListener {
            if (parentFragmentManager.findFragmentByTag(GuestExtendPeriodDialogFragment.TAG) != null) {
                return@setOnClickListener
            }

            GuestExtendPeriodDialogFragment
                .newInstance(groupId)
                .show(parentFragmentManager, GuestExtendPeriodDialogFragment.TAG)
        }

        binding.btnFinish.setOnClickListener{
            vm.patchTrackerDone(groupId)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collectLatest { state ->
                    val dto = state.data

                    binding.tvStartDate.text = prettyDate(dto?.startDate)
                    binding.tvEndDate.text = prettyDate(dto?.endDate)

                    val extensionCount = dto?.extensionCount ?: 0
                    val status = dto?.trackerStatus?.uppercase()

                    val canExtend =
                        extensionCount < 1 &&
                                (status == "GUEST_READING" || status == "GUEST_EXTENSION")

                    binding.btnExtendPeriod.isEnabled = canExtend
                    binding.btnExtendPeriod.alpha = if (canExtend) 1.0f else 0.4f
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.doneState.collectLatest { state ->
                    when (state) {
                        is UiState.Idle -> {
                            binding.btnFinish.isEnabled = true
                        }

                        is UiState.Loading -> {
                            binding.btnFinish.isEnabled = false
                            binding.btnExtendPeriod.isEnabled = false
                            binding.btnExtendPeriod.alpha = 0.4f
                        }

                        is UiState.Success -> {
                            parentFragmentManager.setFragmentResult(
                                RESULT_KEY,
                                Bundle().apply { putString(BUNDLE_ACTION, "GUEST_SHIPPING_READY") }
                            )

                            dismiss()

                            GuestShippingBottomDialogFragment
                                .newInstance(groupId)
                                .show(parentFragmentManager, GuestShippingBottomDialogFragment.TAG)
                        }

                        is UiState.Error -> {
                            binding.btnFinish.isEnabled = true
                            binding.btnExtendPeriod.isEnabled = true
                            binding.btnExtendPeriod.alpha = 1.0f
                        }
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
        const val TAG = "GuestReadingBottomSheetDialogFragment"

        const val RESULT_KEY = "guest_action"
        const val BUNDLE_ACTION = "action"

        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) = GuestReadingBottomDialogFragment().apply {
            arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
        }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}