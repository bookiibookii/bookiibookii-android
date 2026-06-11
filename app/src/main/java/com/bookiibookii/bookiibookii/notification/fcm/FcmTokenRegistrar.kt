package com.bookiibookii.bookiibookii.notification.fcm

import android.content.Context
import android.util.Log
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.notification.DeviceTokenDeactivateRequest
import com.bookiibookii.bookiibookii.data.model.notification.DeviceTokenRegisterRequest
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * FCM 토큰을 서버에 등록/해제하는 헬퍼.
 * - registerCurrentToken: 로그인 상태에서 현재 토큰 발급 → 서버 등록 (메인 진입 시)
 * - registerToken: 이미 확보한 토큰을 서버 등록 (onNewToken 갱신 시)
 * - deactivateCurrentToken: 로그아웃 시 서버에서 토큰 해제
 */
object FcmTokenRegistrar {

    private const val TAG = "FCM"
    private val scope = CoroutineScope(Dispatchers.IO)

    // FCM 토큰을 발급받아 서버에 등록. 로그인 안 됐으면 아무것도 안 함.
    fun registerCurrentToken(context: Context) {
        if (!TokenManager.hasAccessToken(context)) return
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "토큰 발급 실패", task.exception)
                return@addOnCompleteListener
            }
            registerToken(task.result)
        }
    }

    // 이미 확보한 토큰을 서버에 등록 (onNewToken에서 호출).
    fun registerToken(token: String) {
        scope.launch {
            try {
                val res = RetrofitClient.notiApi()
                    .registerDeviceToken(DeviceTokenRegisterRequest(token = token))
                Log.d(TAG, "토큰 등록 결과: ${res.code()}")
            } catch (e: Exception) {
                Log.w(TAG, "토큰 등록 실패", e)
            }
        }
    }

    // 로그아웃 시 서버에서 토큰 해제. 성공/실패 무관하게 onComplete를 메인 스레드에서 호출.
    fun deactivateCurrentToken(onComplete: () -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val token = task.result
            if (!task.isSuccessful || token.isNullOrEmpty()) {
                onComplete()
                return@addOnCompleteListener
            }
            scope.launch {
                try {
                    RetrofitClient.notiApi()
                        .deactivateDeviceToken(DeviceTokenDeactivateRequest(token = token))
                } catch (e: Exception) {
                    Log.w(TAG, "토큰 해제 실패", e)
                } finally {
                    withContext(Dispatchers.Main) { onComplete() }
                }
            }
        }
    }
}
