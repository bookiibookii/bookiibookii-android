package com.bookiibookii.bookiibookii.onboarding.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.databinding.BottomsheetOnbProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OnbProfileBottomSheet (private val listener: Listener) : BottomSheetDialogFragment() {

    interface Listener {
        fun onClickCamera()
        fun onClickGallery()
    }

    private var _binding: BottomsheetOnbProfileBinding? = null
    private val binding: BottomsheetOnbProfileBinding
        get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetOnbProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 사진 촬영
        binding.btnCamera.setOnClickListener {
            listener.onClickCamera()
            dismiss()
        }

        // 앨범에서 선택
        binding.btnGallery.setOnClickListener {
            listener.onClickGallery()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}