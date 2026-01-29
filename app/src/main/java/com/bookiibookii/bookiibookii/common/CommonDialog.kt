package com.bookiibookii.bookiibookii.common


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.bookiibookii.bookiibookii.databinding.DialogCommonBinding

class CommonDialog(
    context: Context,
    private val title: String,
    private val subtitle: String,
    private val content: String,
    private val confirmBtnText: String,
    @ColorRes private val confirmBtnColor: Int, // 색상 리소스 ID (R.color.xxx)
    private val onConfirmClick: () -> Unit,     // 확인 버튼 람다
    private val onCancelClick: (() -> Unit)? = null // 취소 버튼 람다 (선택)
) : Dialog(context) {

    private lateinit var binding: DialogCommonBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 윈도우 배경 투명 처리
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogCommonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        initView()
        initListener()
    }

    private fun initView() {
        with(binding) {
            // 텍스트 세팅
            dialogTitleTv.text = title
            dialogSubtitleTv.text = subtitle
            dialogContentTv.text = content
            dialogConfirmBtn.text = confirmBtnText

            // 버튼 색상 변경
            dialogConfirmBtn.backgroundTintList =
                ContextCompat.getColorStateList(context, confirmBtnColor)
        }
    }

    private fun initListener() {
        with(binding) {
            // 닫기 (X) 아이콘
            dialogCloseIv.setOnClickListener {
                dismiss()
                onCancelClick?.invoke()
            }

            // 취소 버튼
            dialogCancelBtn.setOnClickListener {
                dismiss()
                onCancelClick?.invoke()
            }

            // 확인 버튼
            dialogConfirmBtn.setOnClickListener {
                dismiss()
                onConfirmClick()
            }
        }
    }
}