package com.bookiibookii.bookiibookii.group

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.databinding.FragmentGrpManageBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentGrpManageBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrpManageBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 리사이클러뷰 설정 (아직 어댑터 없음 - 레이아웃 매니저만 설정)
        binding.grpMgBottomSheetInfoRv.layoutManager = LinearLayoutManager(context)

        // 2. 전송 버튼 클릭 이벤트
        binding.grpMgBottomSheetSendIv.setOnClickListener {
            val inputContent = binding.grpMgBottomSheetInputEt.text.toString()

            if (inputContent.isNotBlank()) {
                // TODO: 댓글 등록 로직 구현 (API 통신 or 리스트 추가)
                Toast.makeText(context, "댓글 전송: $inputContent", Toast.LENGTH_SHORT).show()

                // 입력창 초기화 및 키보드 내리기
                binding.grpMgBottomSheetInputEt.text.clear()
                hideKeyboard()
            }
        }

        // 3. 새로고침 버튼
        binding.grpMgBottomSheetReloadIv.setOnClickListener {
            Toast.makeText(context, "새로고침", Toast.LENGTH_SHORT).show()
        }
    }

    // 🌟 [중요] 다이얼로그 스타일 설정 (꽉 찬 화면 + 키보드 대응)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        // 화면이 떴을 때 동작 설정
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let { sheet ->
                // 1. 배경을 투명하게 (XML에서 설정한 둥근 배경이 보이도록)
                sheet.background = ColorDrawable(Color.TRANSPARENT)

                // 2. 높이를 화면 전체로 확장 가능하게 설정 (선택사항)
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }

        // 🌟 [핵심] 키보드가 올라올 때 입력창이 따라 올라오도록 설정
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return dialog
    }

    // 키보드 숨기기 유틸 함수
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.grpMgBottomSheetInputEt.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}