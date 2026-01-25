package com.bookiibookii.bookiibookii.group

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.bookiibookii.bookiibookii.databinding.DialogGroupJoinBinding
import com.bookiibookii.bookiibookii.databinding.FragmentGrpBottomSheetMoreBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GroupMoreBottomSheet(
    private val isHost: Boolean, // 호스트 여부 확인
    private val itemClickListener: (String) -> Unit // 클릭 이벤트 넘겨줄 함수
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentGrpBottomSheetMoreBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGrpBottomSheetMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isHost) {
            // 호스트: 수정, 삭제 보이기 / 신고 숨기기
            binding.grpBottomSheetEditTv.visibility = View.VISIBLE
            binding.grpBottomSheetMoreDeleteTv.visibility = View.VISIBLE
            binding.grpBottomSheetReportTv.visibility = View.GONE
        } else {
            // 게스트: 신고 보이기 / 수정, 삭제 숨기기
            binding.grpBottomSheetEditTv.visibility = View.GONE
            binding.grpBottomSheetMoreDeleteTv.visibility = View.GONE
            binding.grpBottomSheetReportTv.visibility = View.VISIBLE
        }

        //  클릭 리스너 연결
        binding.grpBottomSheetEditTv.setOnClickListener {
            itemClickListener("EDIT")
            dismiss() // 창 닫기
        }

        binding.grpBottomSheetMoreDeleteTv.setOnClickListener {
            itemClickListener("DELETE")
        }

        binding.grpBottomSheetReportTv.setOnClickListener {
            itemClickListener("REPORT")
            dismiss()
        }
    }

}