package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.databinding.FragmentLibGroupDeleteBottomBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LibraryGroupDeleteBottomSheet(
    private val onDeleteClick: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentLibGroupDeleteBottomBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLibGroupDeleteBottomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.libDialogDeleteTv.setOnClickListener {
            dismiss() // 바텀시트 닫고
            onDeleteClick() // 삭제 다이얼로그 호출 콜백 실행
        }
    }
}