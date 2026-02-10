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
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestAppointmentEditDialogBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DirectGuestAppointmentEditDialogFragment : DialogFragment() {

    private var _binding: FragmentDirectGuestAppointmentEditDialogBinding? = null
    private val binding get() = _binding!!

    private val vm: DirectGuestViewModel by activityViewModels()

    private var didInitInputs = false

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDirectGuestAppointmentEditDialogBinding.inflate(inflater, container, false)
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

        updateRegisterState()
        binding.etDate.doAfterTextChanged { updateRegisterState() }
        binding.etPlace.doAfterTextChanged { updateRegisterState() }

        vm.loadMeeting(groupId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.meetingState.collectLatest { state ->
                    when (state) {
                        UiState.Idle -> Unit
                        UiState.Loading -> Unit

                        is UiState.Success -> {
                            if (didInitInputs) return@collectLatest
                            didInitInputs = true

                            val dto = state.data
                            binding.etDate.setText(dto.meetingTime.orEmpty())
                            binding.etPlace.setText(dto.meetingPlace.orEmpty())

                            updateRegisterState()
                        }

                        is UiState.Error -> {

                        }
                    }
                }
            }
        }

        binding.btnRegister.setOnClickListener {
            if (!binding.btnRegister.isEnabled) return@setOnClickListener

            val date = binding.etDate.text?.toString()?.trim().orEmpty()
            val place = binding.etPlace.text?.toString()?.trim().orEmpty()

            binding.btnRegister.isEnabled = false
            vm.makeMeeting(groupId, date, place)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.event.collect { ev ->
                    when (ev) {
                        is DirectGuestEvent.MeetingSuccess -> {
                            dismissAllDialogsAndSheets()
                            vm.loadTracker(groupId)
                        }

                        is DirectGuestEvent.MeetingFail -> {
                            binding.btnRegister.isEnabled = true
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun dismissAllDialogsAndSheets() {
        val fm = requireActivity().supportFragmentManager

        fm.fragments.forEach { f ->
            if (f is DialogFragment) f.dismissAllowingStateLoss()
        }

        parentFragmentManager.fragments.forEach { f ->
            if (f is DialogFragment) f.dismissAllowingStateLoss()
        }
    }

    private fun updateRegisterState() {
        val context = binding.root.context

        val dateOk = !binding.etDate.text?.toString()?.trim().isNullOrEmpty()
        val placeOk = !binding.etPlace.text?.toString()?.trim().isNullOrEmpty()
        val enabled = dateOk && placeOk

        binding.btnRegister.apply {
            isEnabled = enabled

            val bg = if (enabled) R.color.grey_900 else R.color.grey_200
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, bg))

            val tc = if (enabled) R.color.grey_100 else R.color.grey_500
            setTextColor(ContextCompat.getColor(context, tc))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectGuestAppointmentEditDialogFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectGuestAppointmentEditDialogFragment().apply {
                arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
            }
    }
}
