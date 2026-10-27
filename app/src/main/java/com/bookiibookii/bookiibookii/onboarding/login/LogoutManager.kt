package com.bookiibookii.bookiibookii.onboarding.login

import android.content.Context
import android.content.Intent
import android.util.Log
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.notification.fcm.FcmTokenRegistrar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 사용자 로그아웃.
 * FCM 토큰 해제 → 서버 로그아웃 → 로컬 토큰 삭제 → 로그인 화면 이동 순서로 진행한다.
 * 두 API 모두 access token이 필요하므로 로컬 토큰은 마지막에 지운다.
 * 서버 요청이 실패해도 로컬 로그아웃은 항상 진행한다.
 */
object LogoutManager {

    private const val TAG = "LOGOUT"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun logout(context: Context) {
        val appContext = context.applicationContext
        FcmTokenRegistrar.deactivateCurrentToken {
            scope.launch {
                try {
                    val res = RetrofitClient.authApi().logout()
                    Log.d(TAG, "서버 로그아웃 결과: ${res.code()}")
                } catch (e: Exception) {
                    Log.w(TAG, "서버 로그아웃 실패", e)
                } finally {
                    withContext(Dispatchers.Main) {
                        TokenManager.clear(appContext)
                        val intent = Intent(appContext, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        appContext.startActivity(intent)
                    }
                }
            }
        }
    }
}
