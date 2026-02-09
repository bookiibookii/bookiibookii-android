package com.bookiibookii.bookiibookii.trkHost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentReadingBottomSheetDialogBinding
import com.bookiibookii.bookiibookii.trkHost.TrackerDateUtil.prettyDate
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HostReadingBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentReadingBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    private val vm: HostViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReadingBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.resetDoneState()

        binding.btnWriteCard.setOnClickListener{

        }

        binding.btnExtendPeriod.setOnClickListener{
            if (parentFragmentManager.findFragmentByTag(HostExtendPeriodDialogFragment.TAG) != null) {
                return@setOnClickListener
            }

            HostExtendPeriodDialogFragment
                .newInstance(groupId)
                .show(parentFragmentManager, HostExtendPeriodDialogFragment.TAG)
        }

        binding.btnFinish.setOnClickListener{
            vm.patchTrackerDone(groupId)
        }

        // extensionCount 1이상이면 버튼 비활성화
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collectLatest { state ->

                    val dto = state.data

                    binding.tvStartDate.text = prettyDate(dto?.startDate)
                    binding.tvEndDate.text = prettyDate(dto?.endDate)

                    val extensionCount = state.data?.extensionCount ?: 0
                    val status = state.data?.trackerStatus?.uppercase()

                    val canExtend =
                        extensionCount < 1 &&
                                (status == "HOST_READING" || status == "HOST_EXTENSION")

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
                        }
                        is UiState.Success -> {
                            parentFragmentManager.setFragmentResult(
                                RESULT_KEY,
                                Bundle().apply { putString(BUNDLE_ACTION, "HOST_SHIPPING_READY") }
                            )

                            dismiss()

                            HostShippingBottomDialogFragment
                                .newInstance(groupId)
                                .show(parentFragmentManager, HostShippingBottomDialogFragment.TAG)
                        }
                        is UiState.Error -> {
                            binding.btnFinish.isEnabled = true
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

    companion object{
        const val TAG = "ReadingBottomSheetDialogFragment"
        const val RESULT_KEY = "host_action"
        const val BUNDLE_ACTION = "action"
        private const val ARG_GROUP_ID = "arg_group_id"
        fun newInstance(groupId: Long) = HostReadingBottomDialogFragment().apply {
            arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
        }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}