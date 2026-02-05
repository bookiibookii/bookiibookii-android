package com.bookiibookii.bookiibookii.trkHost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.databinding.FragmentTrackerContainerBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class TrackerContainerBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentTrackerContainerBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val vm: HostTrackerDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackerContainerBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { state ->
                    when (state) {
                        is TrackerDetailUiState.Success -> render(state.data)
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun render(ui: TrackerDetailUiModel) {
        binding.contentContainer.removeAllViews()
        val content = layoutInflater.inflate(ui.layoutRes, binding.contentContainer, false)
        binding.contentContainer.addView(content)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "TrackerContainerBottomSheet"
    }
}