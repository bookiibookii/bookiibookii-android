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
                vm.doneState.collectLatest { state ->
                    when (state) {
                        is UiState.Idle -> {
                            binding.btnFinish.isEnabled = true
                        }

                        is UiState.Loading -> {
                            binding.btnFinish.isEnabled = false
                            binding.btnExtendPeriod.isEnabled = false
                        }

                        is UiState.Success -> {
                            dismiss()
                            GuestShippingBottomDialogFragment
                                .newInstance(groupId)
                                .show(parentFragmentManager, GuestShippingBottomDialogFragment.TAG)
                        }

                        is UiState.Error -> {
                            binding.btnFinish.isEnabled = true
                            binding.btnExtendPeriod.isEnabled = true
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
        const val TAG = "GuestReadingBottomSheetDialogFragment"

        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            GuestReadingBottomDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GROUP_ID, groupId)
                }
            }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}