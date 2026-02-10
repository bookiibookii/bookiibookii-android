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
import com.bookiibookii.bookiibookii.databinding.FragmentGuestStartBottomDialogBinding
import com.bookiibookii.bookiibookii.trkHost.TrackerDateUtil.prettyDate
import com.bookiibookii.bookiibookii.trkHost.UiState
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class GuestStartBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentGuestStartBottomDialogBinding? = null
    private val binding get() = _binding!!
    private val vm: GuestViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuestStartBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.resetReadingStartState()

        binding.btnStart.setOnClickListener {
            vm.patchTrackerReadingStart(groupId)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collectLatest { state ->
                    val dto = state.data

                    binding.tvStartDate.text = prettyDate(dto?.startDate)
                    binding.tvEndDate.text = prettyDate(dto?.endDate)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.readingStartState.collectLatest { state ->
                    when (state) {
                        is UiState.Idle -> Unit

                        is UiState.Loading -> {
                            binding.btnStart.isEnabled = false
                            binding.btnStart.alpha = 0.45f
                        }

                        is UiState.Success -> {
                            dismissAllowingStateLoss()

                            GuestReadingBottomDialogFragment
                                .newInstance(groupId)
                                .show(parentFragmentManager, GuestReadingBottomDialogFragment.TAG)
                        }

                        is UiState.Error -> {
                            binding.btnStart.isEnabled = true
                            binding.btnStart.alpha = 1.0f
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
        const val TAG = "GuestBookStartBottomSheetFragment"

        const val RESULT_KEY = "guest_action"
        const val BUNDLE_ACTION = "action"

        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) = GuestStartBottomDialogFragment().apply {
            arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
        }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }

}