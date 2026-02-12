package com.bookiibookii.bookiibookii.onboarding.login

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bookiibookii.bookiibookii.R
import com.google.android.material.button.MaterialButton

class LoginIntroAnimActivity : AppCompatActivity() {

    private var next: String = NEXT_LOGIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_intro_anim)

        // 뒤로가기 막기
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 아무것도 안 함 (뒤로가기 무시)
            }
        })

        next = intent.getStringExtra(EXTRA_NEXT) ?: NEXT_LOGIN

        val iv = findViewById<ImageView>(R.id.iv_intro_anim)
        val btnStart = findViewById<MaterialButton>(R.id.btn_start)

        btnStart.visibility = View.GONE
        btnStart.setOnClickListener { route(next) }

        val drawable = iv.drawable
        if (drawable is AnimationDrawable) {
            drawable.stop()
            drawable.start()

            val totalDuration = (0 until drawable.numberOfFrames)
                .sumOf { drawable.getDuration(it) }

            // 애니 끝나면 자동 이동하지 말고 버튼만 보여주기
            iv.postDelayed({
                drawable.stop() // 마지막 프레임에서 멈춤
                btnStart.visibility = View.VISIBLE
            }, totalDuration.toLong())
        } else {
            // 안전 fallback: 잠깐 후 버튼 노출
            iv.postDelayed({
                btnStart.visibility = View.VISIBLE
            }, 800L)
        }
    }

    private fun route(next: String) {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_NEXT = "extra_next"
        const val NEXT_LOGIN = "next_login"
        const val NEXT_ONBOARDING = "next_onboarding"
    }
}