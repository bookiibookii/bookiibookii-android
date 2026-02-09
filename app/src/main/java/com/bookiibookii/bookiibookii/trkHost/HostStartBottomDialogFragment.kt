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
import com.bookiibookii.bookiibookii.databinding.FragmentStartBottomSheetDialogBinding
import com.bookiibookii.bookiibookii.trkHost.TrackerDateUtil.prettyDate
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HostStartBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentStartBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    private val vm: HostViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.resetReadingStartState()
        vm.loadTracker(groupId)

        binding.btnStart.setOnClickListener {
            vm.patchTrackerReadingStart(groupId)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { state ->
                    val dto = state.data ?: return@collect

                   binding.tvStartDate.text = prettyDate(dto?.startDate)
                   binding.tvEndDate.text = prettyDate(dto?.endDate)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.readingStartState.collectLatest { state ->
                    when (state) {
                        is UiState.Idle -> {
                            binding.btnStart.isEnabled = true
                        }

                        is UiState.Loading -> {
                            binding.btnStart.isEnabled = false
                        }

                        is UiState.Success -> {
                            parentFragmentManager.setFragmentResult(
                                RESULT_KEY,
                                Bundle().apply { putString(BUNDLE_ACTION, "START_READING") }
                            )

                            dismiss()
                            HostReadingBottomDialogFragment
                                .newInstance(groupId)
                                .show(parentFragmentManager, HostReadingBottomDialogFragment.TAG)
                        }

                        is UiState.Error -> {
                            binding.btnStart.isEnabled = true
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
        const val TAG = "BookStartBottomSheetFragment"
        const val RESULT_KEY = "host_action"
        const val BUNDLE_ACTION = "action"

        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) = HostStartBottomDialogFragment().apply {
            arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
        }
    }

    override fun getTheme(): Int = R.style.Theme_Bookii_BottomSheet_NoDim
}
