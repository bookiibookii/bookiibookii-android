package com.bookiibookii.bookiibookii.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.databinding.FragmentGrpBottomSheetMoreBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GroupMoreBottomSheet(
    private val isHost: Boolean,        // 호스트 여부
    private val itemClickListener: (String) -> Unit // 클릭 시 동작할 함수 (Activity로 넘겨줌)
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

        // 1. 호스트 vs 게스트 UI 분기 처리
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

        // 2. 클릭 리스너 연결
        binding.grpBottomSheetEditTv.setOnClickListener {
            itemClickListener("EDIT")
            dismiss() // 선택 후 창 닫기
        }

        binding.grpBottomSheetMoreDeleteTv.setOnClickListener {
            itemClickListener("DELETE")
            dismiss() // ★ 중요: 삭제 누르면 바텀시트 닫고 -> 확인 다이얼로그 띄워야 함
        }

        binding.grpBottomSheetReportTv.setOnClickListener {
            itemClickListener("REPORT")
            dismiss() // 선택 후 창 닫기
        }
    }
}