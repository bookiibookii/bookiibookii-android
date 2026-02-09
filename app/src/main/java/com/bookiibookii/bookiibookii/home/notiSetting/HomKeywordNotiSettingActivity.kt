package com.bookiibookii.bookiibookii.home.notiSetting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bookiibookii.bookiibookii.R
import com.google.android.material.appbar.MaterialToolbar

class HomKeywordNotiSettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hom_keyword_noti_setting)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        // 뒤로 가기 버튼 클릭 시 이전 화면으로
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}