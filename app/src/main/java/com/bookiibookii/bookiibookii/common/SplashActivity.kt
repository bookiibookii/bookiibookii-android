package com.bookiibookii.bookiibookii.common

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.databinding.ActivitySplashBinding
import com.bookiibookii.bookiibookii.onboarding.login.LoginActivity
import com.bookiibookii.bookiibookii.onboarding.login.LoginIntroAnimActivity
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.bookiibookii.bookiibookii.onboarding.profile.OnbProfileActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.java

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(2000L)
            routeNext()
        }
    }

    private fun routeNext() {
        val hasToken = TokenManager.hasAccessToken(this)

        // (기존 - 자동 로그인) 스플래쉬 -> 메인
        if (hasToken) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // (신규) 또는 (기존 - 자동 로그인 안됨)
        // 스플래쉬 -> 애니메이션 -> 로그인
        moveToIntro()
    }

    private fun moveToIntro() {
        val intent = Intent(this, LoginIntroAnimActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}