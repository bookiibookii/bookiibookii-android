package com.bookiibookii.bookiibookii.common // 적절한 패키지 경로에 생성하세요

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.bookiibookii.bookiibookii.R

class LoadingDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 타이틀바 제거
        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_loading)

        window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#80000000")))
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        // 로딩 중에 뒤로가기나 바깥 화면 터치로 로딩창이 꺼지지 않도록 막음
        setCancelable(false)
    }
}