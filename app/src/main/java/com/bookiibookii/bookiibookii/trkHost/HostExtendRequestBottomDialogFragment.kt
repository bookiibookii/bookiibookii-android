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
import com.bookiibookii.bookiibookii.databinding.FragmentHostExtendRequestBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HostExtendRequestBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentHostExtendRequestBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val vm: HostViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHostExtendRequestBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.loadTracker(groupId)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collectLatest { state ->
                    val data = state.data

                    val endRaw = data?.endDate
                    val extDays = data?.extensionDays ?: 0

                    binding.tvOldEndDate.text = TrackerDateUtil.prettyDate(endRaw)

                    val newEndRaw = addDaysToIsoDateTime(endRaw, extDays)
                    binding.tvNewEndDate.text = TrackerDateUtil.prettyDate(newEndRaw)
                }
            }
        }

        binding.btnConfirm.setOnClickListener { dismiss() }
    }

    private fun addDaysToIsoDateTime(raw: String?, days: Int): String? {
        if (raw.isNullOrBlank()) return raw
        if (days == 0) return raw

        return try {
            val datePart = raw.substring(0, 10)
            val timePart = raw.substring(10)

            val d = LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE)
            val newDate = d.plusDays(days.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)

            newDate + timePart
        } catch (_: Exception) {
            raw
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ExtendRequestFragment"
        const val RESULT_KEY = "host_action"
        const val BUNDLE_ACTION = "action"

        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) = HostExtendRequestBottomDialogFragment().apply {
            arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
        }
    }

    override fun getTheme(): Int = R.style.Theme_Bookii_BottomSheet_NoDim
}
