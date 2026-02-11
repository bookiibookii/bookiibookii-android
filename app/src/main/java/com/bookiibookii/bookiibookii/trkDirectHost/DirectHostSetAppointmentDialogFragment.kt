package com.bookiibookii.bookiibookii.trkDirectHost

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
import com.bookiibookii.bookiibookii.databinding.FragmentDirectHostSetAppointmentBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

class DirectHostSetAppointmentDialogFragment : DialogFragment() {

    private var _binding: FragmentDirectHostSetAppointmentBinding? = null
    private val binding get() = _binding!!
    private var didInitPlace = false

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    private val vm: DirectHostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectHostSetAppointmentBinding.inflate(inflater, container, false)
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

        binding.btnClose.setOnClickListener { dismiss() }

        vm.loadTracker(groupId)

        updateRegisterState()

        binding.etDate.doAfterTextChanged { updateRegisterState() }
        binding.etPlace.doAfterTextChanged { updateRegisterState() }

        binding.btnRegister.setOnClickListener {
            if (!binding.btnRegister.isEnabled) return@setOnClickListener

            val date = binding.etDate.text.toString().trim()
            val rawDate = binding.etDate.text.toString().trim()
            val place = binding.etPlace.text.toString().trim()

            val apiDate = toApiUtcZ(rawDate)
            if (apiDate == null) {
                Toast.makeText(
                    requireContext(),
                    "날짜 형식이 올바르지 않아요. 예: 2026.02.10.14:00",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (place.isBlank()) {
                Toast.makeText(requireContext(), "장소를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnRegister.isEnabled = false
            vm.makeMeeting(groupId, apiDate, place)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.trackerState.collectLatest { state ->
                    val dto = (state as? UiState.Success)?.data ?: return@collectLatest
                    if (didInitPlace) return@collectLatest
                    didInitPlace = true

                    val placeFromApi = dto.meetingInfo?.meetingPlace.orEmpty()
                    if (binding.etPlace.text.isNullOrBlank() && placeFromApi.isNotBlank()) {
                        binding.etPlace.setText(placeFromApi)
                        updateRegisterState()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.event.collect { ev ->
                    when (ev) {
                        is DirectHostEvent.MeetingSuccess -> {
                            dismiss()
                            vm.loadTracker(groupId)
                        }

                        is DirectHostEvent.MeetingFail -> {
                            binding.btnRegister.isEnabled = true
                        }

                        else -> Unit
                    }
                }
            }
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

    private val inputFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("uuuu.MM.dd.HH:mm")
            .withResolverStyle(ResolverStyle.STRICT)

    private val apiFormatterNoShift: DateTimeFormatter =
        DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'")
            .withResolverStyle(ResolverStyle.STRICT)

    private fun toApiUtcZ(rawInput: String): String? {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return null

        return try {
            val local = LocalDateTime.parse(trimmed, inputFormatter)
            apiFormatterNoShift.format(local)
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectSetAppointmentDialogFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectHostSetAppointmentDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GROUP_ID, groupId)
                }
            }
    }
}
