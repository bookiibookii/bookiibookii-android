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
import com.bookiibookii.bookiibookii.databinding.FragmentDirectHostStartBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DirectHostStartBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectHostStartBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val vm: DirectHostViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectHostStartBottomDialogBinding.inflate(inflater, container, false)
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

                            binding.tvStartDate.text = dto.startDate ?: "-"
                            binding.tvEndDate.text = dto.endDate ?: "-"
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
                        DirectHostEvent.ReadingStartSuccess -> {
                            vm.loadTracker(groupId)
                            binding.btnStart.isEnabled = true
                            dismissAllowingStateLoss()
                        }

                        is DirectHostEvent.ReadingStartFail -> {
                            binding.btnStart.isEnabled = true
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }

    companion object {
        const val TAG = "DirectBookStartBottomSheetFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectHostStartBottomDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GROUP_ID, groupId)
                }
            }
    }
}