package com.bookiibookii.bookiibookii.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentGrpBottomSheetMoreBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GroupMoreBottomSheet(
private val isHost: Boolean,
private val groupStatus: String, // ★ 추가됨
private val onActionClick: (String) -> Unit) : BottomSheetDialogFragment() {

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

        // 1. 권한 분기 (호스트 vs 게스트)
        if (isHost) {
            binding.grpBottomSheetEditTv.visibility = View.VISIBLE
            binding.grpBottomSheetMoreDeleteTv.visibility = View.VISIBLE
            binding.grpBottomSheetReportTv.visibility = View.GONE

            // 2. 상태 분기 (모집 중이 아닐 때)
            if (groupStatus != "RECRUITING") {
                binding.grpBottomSheetWarningLayout.visibility = View.VISIBLE
                binding.grpBottomSheetMenuDivider.visibility = View.VISIBLE

                // 수정 버튼 비활성화
                binding.grpBottomSheetEditTv.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_400))
                    isEnabled = false
                }
            }
        } else {
            // 게스트일 때
            binding.grpBottomSheetEditTv.visibility = View.GONE
            binding.grpBottomSheetMoreDeleteTv.visibility = View.GONE
            binding.grpBottomSheetReportTv.visibility = View.VISIBLE
        }

        // 3. 클릭 리스너 설정
        binding.grpBottomSheetEditTv.setOnClickListener {
            onActionClick("EDIT")
            dismiss()
        }

        binding.grpBottomSheetMoreDeleteTv.setOnClickListener {
            onActionClick("DELETE")
            dismiss()
        }

        binding.grpBottomSheetReportTv.setOnClickListener {
            onActionClick("REPORT")
            dismiss()
        }
    }
}