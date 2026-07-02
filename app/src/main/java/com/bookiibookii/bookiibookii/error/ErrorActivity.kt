package com.bookiibookii.bookiibookii.error

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.common.ComRetryBus
import com.bookiibookii.bookiibookii.data.api.AuthInterceptor
import com.bookiibookii.bookiibookii.error.model.ErrorType
import com.bookiibookii.bookiibookii.error.ui.ErrorScreen
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

/**
 * 공통 에러 화면 진입점
 * 종류는 [ErrorType]으로 전달받고, 버튼 동작은 기존 ComErrorActivity와 동일하게 유지
 */

// TODO: 접근 권한이 없는 페이지(NO_PERMISSION) 진입점 없음
class ErrorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.readErrorType()

        setContent {
            BookiiBookiiTheme {
                ErrorScreen(
                    type = type,
                    onRetry = ::handleRetry,
                    onBack = ::handleBack,
                    onGoMain = ::handleGoMain,
                )
            }
        }
    }

    // 다시 시도: 라우팅 잠금 해제 + 재시도 신호 전달 후 이전 화면 복귀(루트면 메인으로)
    private fun handleRetry() {
        AuthInterceptor.unlockRouting()
        ComRetryBus.emitRetry()
        if (isTaskRoot) {
            startActivity(mainIntent())
        }
        finish()
    }

    // 이전: 라우팅 잠금만 풀고 뒤로
    private fun handleBack() {
        AuthInterceptor.unlockRouting()
        finish()
    }

    // 메인으로 이동
    private fun handleGoMain() {
        AuthInterceptor.unlockRouting()
        startActivity(mainIntent())
        finish()
    }

    private fun mainIntent(): Intent =
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

    companion object {
        private const val EXTRA_ERROR_TYPE = "extra_error_type"

        fun newIntent(context: Context, type: ErrorType): Intent =
            Intent(context, ErrorActivity::class.java).apply {
                putExtra(EXTRA_ERROR_TYPE, type.name)
            }

        // 잘못된/누락된 값이면 네트워크 오류로 폴백
        private fun Intent.readErrorType(): ErrorType =
            runCatching { ErrorType.valueOf(getStringExtra(EXTRA_ERROR_TYPE) ?: "") }
                .getOrDefault(ErrorType.NETWORK)
    }
}
