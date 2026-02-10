package com.bookiibookii.bookiibookii.trkDirectGuest

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestExtendPeriodDialogBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DirectGuestExtendPeriodDialogFragment : DialogFragment() {

    private var _binding: FragmentDirectGuestExtendPeriodDialogBinding? = null
    private val binding get() = _binding!!

    private val vm: DirectGuestViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    private var originEndDateRaw: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDirectGuestExtendPeriodDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        vm.loadTracker(groupId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.trackerState.collectLatest { state ->
                    when (state) {
                        UiState.Idle -> Unit

                        UiState.Loading -> {
                            binding.tvOriginDate.text = "-"
                            binding.tvExtendedDate.text = "-"
                            binding.tvExtendedDate.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.grey_600)
                            )
                        }

                        is UiState.Success -> {
                            val dto = state.data
                            originEndDateRaw = dto.endDate

                            val originDisplay = displayDate(dto.endDate)
                            binding.tvOriginDate.text = originDisplay

                            val currentDays = binding.etDays.text?.toString()?.toIntOrNull() ?: 0
                            binding.tvExtendedDate.text = calcExtendedDate(dto.endDate, currentDays)

                            if (currentDays <= 0) {
                                binding.tvExtendedDate.setTextColor(
                                    ContextCompat.getColor(requireContext(), R.color.grey_600)
                                )
                            }
                        }

                        is UiState.Error -> {
                            binding.tvOriginDate.text = "-"
                            binding.tvExtendedDate.text = "-"
                            binding.tvExtendedDate.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.grey_600)
                            )
                        }
                    }
                }
            }
        }

        binding.etDays.doAfterTextChanged { text ->
            val hasInput = !text.isNullOrEmpty()
            val context = binding.root.context

            binding.btnApply.apply {
                isEnabled = hasInput
                val bg = if (hasInput) R.color.grey_900 else R.color.grey_100
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, bg))

                val tc = if (hasInput) R.color.grey_100 else R.color.grey_600
                setTextColor(ContextCompat.getColor(context, tc))
            }

            val boxColor = if (hasInput) R.color.white else R.color.grey_100
            binding.boxDays.setCardBackgroundColor(ContextCompat.getColor(context, boxColor))

            val days = text?.toString()?.toIntOrNull() ?: 0
            binding.tvExtendedDate.text = calcExtendedDate(originEndDateRaw, days)

            val colorRes = if (hasInput) android.R.color.holo_orange_dark else R.color.grey_600
            binding.tvExtendedDate.setTextColor(ContextCompat.getColor(context, colorRes))
        }

        binding.btnApply.setOnClickListener {
            val days = binding.etDays.text?.toString()?.toIntOrNull()
            if (days == null || days <= 0) return@setOnClickListener

            binding.btnApply.isEnabled = false
            vm.extendPeriod(groupId, days)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.event.collect { ev ->
                    when (ev) {
                        is DirectGuestEvent.ExtensionSuccess -> {
                            dismissAllDialogsInActivity()
                            vm.loadTracker(groupId)
                        }

                        is DirectGuestEvent.ExtensionFail -> {
                            binding.btnApply.isEnabled = true
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun dismissAllDialogsInActivity() {
        val fm = requireActivity().supportFragmentManager
        fm.fragments.forEach { f ->
            if (f is DialogFragment) f.dismissAllowingStateLoss()
        }
        parentFragmentManager.fragments.forEach { f ->
            if (f is DialogFragment) f.dismissAllowingStateLoss()
        }
    }

    private fun displayDate(raw: String?): String {
        if (raw.isNullOrBlank()) return "-"
        return raw.take(10)
    }

    private fun calcExtendedDate(endDateRaw: String?, days: Int): String {
        val base = displayDate(endDateRaw)
        if (base == "-" || days <= 0) return base

        return try {
            val d = LocalDate.parse(base, DateTimeFormatter.ISO_LOCAL_DATE)
            d.plusDays(days.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            base
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectGuestExtendPeriodDialogFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectGuestExtendPeriodDialogFragment().apply {
                arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
            }
    }
}
