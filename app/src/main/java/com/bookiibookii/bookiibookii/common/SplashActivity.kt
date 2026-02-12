package com.bookiibookii.bookiibookii.common

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.databinding.ActivitySplashBinding
import com.bookiibookii.bookiibookii.onboarding.login.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.java

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ 시스템 스플래시 제어
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            val minDisplayMs = 2000L
            delay(minDisplayMs)

            // TODO [v1] 앱 초기 진입 로직 구현
            // 1. 네트워크 연결 상태 확인
            // 2. 저장된 액세스 토큰 존재 여부 확인
            // 3. 토큰 유효성 검사 (만료 여부)
            // 4. 결과에 따라 홈 / 로그인 분기 처리

            routeNext()
        }
    }

    private fun routeNext() {
        // TODO [v1] TokenManager 도입 후 실제 토큰 여부로 교체
        // val hasValidToken = TokenManager.hasValidToken()

        // 현재 단계에서는 항상 로그인화면으로 이동
        startActivity(Intent(this, LoginActivity::class.java))

        // TODO [v1] 토큰 분기 로직 활성화 시 아래 코드 사용
        /*
        if (hasValidToken) {
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        */

        // 뒤로가기로 스플래시 재진입 방지
        finish()
    }
}