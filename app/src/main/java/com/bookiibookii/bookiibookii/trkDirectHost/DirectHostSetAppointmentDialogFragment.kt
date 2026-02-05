package com.bookiibookii.bookiibookii.trkDirectHost

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentDirectHostSetAppointmentBinding
import com.bookiibookii.bookiibookii.trkDirectGuest.DirectGuestAppointmentBottomDialogFragment

class DirectHostSetAppointmentDialogFragment : DialogFragment() {

    private var _binding: FragmentDirectHostSetAppointmentBinding? = null
    private val binding get() = _binding!!

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

        updateRegisterState()

        binding.etDate.doAfterTextChanged { updateRegisterState() }
        binding.etPlace.doAfterTextChanged { updateRegisterState() }

        binding.btnRegister.setOnClickListener {
            if (!binding.btnRegister.isEnabled) return@setOnClickListener

            val date = binding.etDate.text?.toString()?.trim().orEmpty()
            val place = binding.etPlace.text?.toString()?.trim().orEmpty()

            (parentFragmentManager.findFragmentByTag(DirectGuestAppointmentBottomDialogFragment.TAG) as? DialogFragment)
                ?.dismissAllowingStateLoss()

            dismissAllowingStateLoss()

            DirectHostAppointmentEditBottomDialogFragment()
                .show(parentFragmentManager, DirectHostAppointmentEditBottomDialogFragment.TAG)
        }
    }


    private fun updateRegisterState() {
        val context = binding.root.context

        val dateOk = !binding.etDate.text?.toString()?.trim().isNullOrEmpty()
        val placeOk = !binding.etPlace.text?.toString()?.trim().isNullOrEmpty()
        val enabled = dateOk && placeOk

        binding.btnRegister.apply {
            isEnabled = enabled

            val bgColorRes = if (enabled) R.color.grey_900 else R.color.grey_200
            backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, bgColorRes))

            val textColorRes = if (enabled) R.color.grey_100 else R.color.grey_500
            setTextColor(ContextCompat.getColor(context, textColorRes))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectSetAppointmentDialogFragment"
    }
}
