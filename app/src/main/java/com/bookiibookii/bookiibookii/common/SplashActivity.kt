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

        // 1) 토큰 없음 → 인트로 애니 → 로그인
        if (!hasToken) {
            moveToIntro(LoginIntroAnimActivity.NEXT_LOGIN)
            return
        }

        val isOnboardingDone = TokenManager.isOnboardingDone(this)

        // 2) 토큰 있음 + 온보딩 미완료 → 인트로 애니 → 온보딩
        if (!isOnboardingDone) {
            moveToIntro(LoginIntroAnimActivity.NEXT_ONBOARDING)
            return
        }

        // 3) 토큰 있음 + 온보딩 완료 → 메인
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun moveToIntro(next: String) {
        val intent = Intent(this, LoginIntroAnimActivity::class.java).apply {
            putExtra(LoginIntroAnimActivity.EXTRA_NEXT, next)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}