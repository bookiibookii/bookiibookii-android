package com.bookiibookii.bookiibookii.home.notification.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bookiibookii.bookiibookii.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class HomNotificationActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnSystem: MaterialButton
    private lateinit var btnKeyword: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hom_notification)

        toolbar = findViewById(R.id.toolbar)

        // 뒤로가기
        toolbar.setNavigationOnClickListener { finish() }

        // + 버튼(메뉴) 클릭
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_plus -> {
                    val intent = Intent(this, HomKeywordNotiSettingActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        btnSystem = findViewById(R.id.include_btn_system)
        btnKeyword = findViewById(R.id.include_btn_keyword)

        // 텍스트 세팅
        btnSystem.text = "시스템 알림"
        btnKeyword.text = "키워드 알림"

        btnSystem.setOnClickListener { showSystem() }
        btnKeyword.setOnClickListener { showKeyword() }

        // 첫 화면
        showSystem()
    }

    private fun showSystem() {
        setTabSelected(isSystem = true)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_noti, HomSystemNotiFragment())
            .commit()
    }

    private fun showKeyword() {
        setTabSelected(isSystem = false)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_noti, HomKeywordNotiFragment())
            .commit()
    }

    private fun setTabSelected(isSystem: Boolean) {
        btnSystem.isSelected = isSystem
        btnKeyword.isSelected = !isSystem
    }
}