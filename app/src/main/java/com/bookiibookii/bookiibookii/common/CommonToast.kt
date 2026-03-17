package com.bookiibookii.bookiibookii.common // 프로젝트 패키지명에 맞게 수정하세요

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bookiibookii.bookiibookii.R // R 클래스 임포트 필수

fun Context.showCustomToast(message: String, isSuccess: Boolean) {
    val layout = LayoutInflater.from(this).inflate(R.layout.toast_custom, null)
    layout.findViewById<TextView>(R.id.toast_message_tv).text = message
    val iconRes = if (isSuccess) R.drawable.ic_check else R.drawable.ic_info
    layout.findViewById<ImageView>(R.id.toast_icon_iv).setImageResource(iconRes)

    with(Toast(this.applicationContext)) {
        setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
        duration = Toast.LENGTH_SHORT
        view = layout
        show()
    }
}