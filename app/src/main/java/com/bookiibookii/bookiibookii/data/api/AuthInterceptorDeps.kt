package com.bookiibookii.bookiibookii.data.api

import android.content.Context
import android.content.Intent
import android.util.Log
import com.bookiibookii.bookiibookii.error.ErrorActivity
import com.bookiibookii.bookiibookii.error.model.ErrorType
import com.bookiibookii.bookiibookii.onboarding.login.LoginActivity
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager

// AuthInterceptor의 토큰 저장소 의존성. 테스트에서 페이크로 대체한다.
interface AuthTokenStore {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun saveTokens(access: String, refresh: String, userId: Long)
    fun clear()
}

// AuthInterceptor의 화면 라우팅 의존성 (쿨다운 포함). 테스트에서 페이크로 대체한다.
interface AuthRouter {
    fun routeLogout()
    fun routeComError(type: ErrorType)
}

class TokenManagerStore(context: Context) : AuthTokenStore {
    private val appContext = context.applicationContext
    override fun getAccessToken(): String? = TokenManager.getAccessToken(appContext)
    override fun getRefreshToken(): String? = TokenManager.getRefreshToken(appContext)
    override fun saveTokens(access: String, refresh: String, userId: Long) =
        TokenManager.saveTokens(appContext, access, refresh, userId)
    override fun clear() = TokenManager.clear(appContext)
}

// 네트워크 계층에서 로그인/에러 화면으로 라우팅. 3초 쿨다운으로 중복 라우팅 방지.
class ActivityAuthRouter(context: Context) : AuthRouter {
    private val appContext = context.applicationContext

    override fun routeLogout() {
        Log.e("AUTH_ROUTE", "[LOGOUT] routeLogout called")

        if (!AuthInterceptor.tryClaimRoute()) {
            Log.w("AUTH_ROUTE", "[SKIP] already routing in progress")
            return
        }

        TokenManager.clear(appContext)

        val intent = Intent(appContext, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        android.os.Handler(android.os.Looper.getMainLooper()).post {
            appContext.startActivity(intent)
        }
    }

    override fun routeComError(type: ErrorType) {
        Log.e("AUTH_ROUTE", "[COM_ERROR] type=$type called")

        if (!AuthInterceptor.tryClaimRoute()) return

        val intent = ErrorActivity.newIntent(appContext, type).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        android.os.Handler(android.os.Looper.getMainLooper()).post {
            appContext.startActivity(intent)
        }
    }
}
