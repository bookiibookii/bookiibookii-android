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
import com.bookiibookii.bookiibookii.databinding.FragmentDirectHostExtendRequestBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class DirectHostExtendRequestBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectHostExtendRequestBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectHostExtendRequestBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val vm: DirectHostViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.loadTracker(groupId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.trackerState.collectLatest { state ->
                    when (state) {
                        UiState.Idle -> Unit
                        UiState.Loading -> {
                            binding.tvOldEndDate.text = "-"
                            binding.tvNewEndDate.text = "-"
                        }
                        is UiState.Success -> {
                            val dto = state.data
                            val endDate = dto.endDate
                            val addDays = dto.extensionDays ?: 0

                            binding.tvOldEndDate.text = formatDate(endDate)
                            binding.tvNewEndDate.text = formatDate(addDays(endDate, addDays))
                        }
                        is UiState.Error -> {
                            binding.tvOldEndDate.text = "-"
                            binding.tvNewEndDate.text = "-"
                        }
                    }
                }
            }
        }

        binding.btnConfirm.setOnClickListener{
            dismiss()
        }
    }

    private fun formatDate(raw: String?): String {
        if (raw.isNullOrBlank()) return "-"

        val dt = parseAnyDateTime(raw) ?: return "-"

        return dt.format(DateTimeFormatter.ofPattern("yyyy. MM. dd"))
    }

    private fun addDays(raw: String?, days: Int): String? {
        if (raw.isNullOrBlank()) return null
        val dt = parseAnyDateTime(raw) ?: return null
        return dt.plusDays(days.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    private fun parseAnyDateTime(raw: String): LocalDateTime? {
        return try {
            if (raw.endsWith("Z")) {
                OffsetDateTime.parse(raw).toLocalDateTime()
            } else {
                LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectExtendRequestFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectHostExtendRequestBottomDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GROUP_ID, groupId)
                }
            }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}