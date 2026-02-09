package com.bookiibookii.bookiibookii.trkGuest

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentGuestExtendPeriodDialogBinding
import com.bookiibookii.bookiibookii.trkHost.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GuestExtendPeriodDialogFragment : DialogFragment() {

    private var _binding: FragmentGuestExtendPeriodDialogBinding? = null
    private val binding get() = _binding!!

    private val vm: GuestViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuestExtendPeriodDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.90).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.resetExtensionState()

        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnApply.setOnClickListener {
            val daysText = binding.etDays.text?.toString()?.trim().orEmpty()
            val days = daysText.toIntOrNull()

            if (days == null || days <= 0) {
                Toast.makeText(requireContext(), "연장할 일수를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            vm.patchTrackerExtension(groupId, days)
        }

        binding.etDays.doAfterTextChanged { text ->
            val hasInput = !text.isNullOrEmpty()
            val context = binding.root.context

            binding.btnApply.apply {
                isEnabled = hasInput

                val btBgColor = if (hasInput) R.color.grey_900 else R.color.grey_100
                backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, btBgColor))

                val txtColor = if (hasInput) R.color.grey_100 else R.color.grey_600
                setTextColor(ContextCompat.getColor(context, txtColor))
            }

            val boxColor = if (hasInput) R.color.white else R.color.grey_100
            binding.boxDays.setCardBackgroundColor(ContextCompat.getColor(context, boxColor))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.extensionState.collectLatest { state ->
                    when (state) {
                        is UiState.Idle -> Unit

                        is UiState.Loading -> {
                            binding.btnApply.isEnabled = false
                            binding.btnCancel.isEnabled = false
                            binding.btnClose.isEnabled = false
                        }

                        is UiState.Success -> {
                            dismiss()
                        }

                        is UiState.Error -> {
                            binding.btnCancel.isEnabled = true
                            binding.btnClose.isEnabled = true

                            val hasInput = !binding.etDays.text.isNullOrEmpty()
                            binding.btnApply.isEnabled = hasInput

                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
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
        const val TAG = "GuestExtendPeriodDialogFragment"

        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) = GuestExtendPeriodDialogFragment().apply {
            arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
        }
    }
}
