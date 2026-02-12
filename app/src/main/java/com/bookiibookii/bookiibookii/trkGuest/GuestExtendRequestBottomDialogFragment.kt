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
import com.bookiibookii.bookiibookii.databinding.FragmentGuestExtendRequestBottomDialogBinding
import com.bookiibookii.bookiibookii.trkHost.TrackerDateUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class GuestExtendRequestBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentGuestExtendRequestBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val vm: GuestViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuestExtendRequestBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.loadTracker(groupId)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collectLatest { state ->
                    val data = state.data

                    val oldEndRaw = data?.endDate
                    val extensionDays = data?.extensionDays ?: 0

                    binding.tvOldEndDate.text = TrackerDateUtil.prettyDate(oldEndRaw)

                    val newEndRaw = addDaysToEndDate(oldEndRaw, extensionDays)
                    binding.tvNewEndDate.text = TrackerDateUtil.prettyDate(newEndRaw)
                }
            }
        }

        binding.btnConfirm.setOnClickListener {
            dismiss()
        }
    }

    private fun addDaysToEndDate(raw: String?, days: Int): String? {
        if (raw.isNullOrBlank()) return raw
        if (days == 0) return raw

        return try {
            if (raw.endsWith("Z")) {
                OffsetDateTime.parse(raw).plusDays(days.toLong()).toString()
            } else {
                val datePart = raw.substring(0, 10)
                val d = LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE)
                val newDate = d.plusDays(days.toLong())

                newDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + raw.substring(10)
            }
        } catch (_: Exception) {
            raw
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "GuestExtendRequestFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            GuestExtendRequestBottomDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GROUP_ID, groupId)
                }
            }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}
