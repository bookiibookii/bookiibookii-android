package com.bookiibookii.bookiibookii.trkGuest

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentGuestExtendPeriodDialogBinding

class GuestExtendPeriodDialogFragment : DialogFragment() {

    private var _binding: FragmentGuestExtendPeriodDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuestExtendPeriodDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.90).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener{dismiss()}
        binding.btnCancel.setOnClickListener{dismiss()}
        binding.btnApply.setOnClickListener{
            // 독서 기간 연장 로직 추가
            // 버튼 비활성화도
        }

        binding.etDays.doAfterTextChanged { text ->
            val hasInput = !text.isNullOrEmpty()
            val context = binding.root.context

            binding.btnApply.apply {
                isEnabled = hasInput

                val btBgColor = if (hasInput) R.color.grey_900 else R.color.grey_100
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, btBgColor))

                val txtColor = if (hasInput) R.color.grey_100 else R.color.grey_600
                setTextColor(ContextCompat.getColor(context, txtColor))
            }

            val boxColor = if (hasInput) R.color.white else R.color.grey_100
            binding.boxDays.setCardBackgroundColor(ContextCompat.getColor(context, boxColor))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "GuestExtendPeriodDialogFragment"
    }
}