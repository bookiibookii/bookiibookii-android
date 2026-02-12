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
    private val groupStatus: String,
    private val onActionClick: (String) -> Unit
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

        setupMenuByRole()
        setupClickListeners()
    }

    /**
     * 호스트 여부 및 그룹 상태에 따른 메뉴 활성화/비활성화 설정
     */
    private fun setupMenuByRole() {
        if (isHost) {
            // [호스트 권한] 수정, 삭제 노출 / 신고 숨김
            binding.grpBottomSheetEditTv.visibility = View.VISIBLE
            binding.grpBottomSheetMoreDeleteTv.visibility = View.VISIBLE
            binding.grpBottomSheetReportTv.visibility = View.GONE

            // 모집 중이 아닐 때(진행 중/종료 등) 수정 제한
            if (groupStatus != "RECRUITING") {
                binding.grpBottomSheetWarningLayout.visibility = View.VISIBLE
                binding.grpBottomSheetMenuDivider.visibility = View.VISIBLE

                // 수정 버튼 비활성화 스타일 적용
                binding.grpBottomSheetEditTv.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_400))
                    isEnabled = false
                }
            }
        } else {
            // [게스트 권한] 신고만 노출 / 수정, 삭제 숨김
            binding.grpBottomSheetEditTv.visibility = View.GONE
            binding.grpBottomSheetMoreDeleteTv.visibility = View.GONE
            binding.grpBottomSheetReportTv.visibility = View.VISIBLE
        }
    }


     //각 메뉴 아이템의 클릭 이벤트 처리
    private fun setupClickListeners() {
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