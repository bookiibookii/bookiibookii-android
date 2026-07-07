package com.bookiibookii.bookiibookii.onboarding.login

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.appcompat.app.AppCompatDelegate
import com.bookiibookii.bookiibookii.BuildConfig
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.notification.fcm.BookiiMessagingService
import com.kakao.sdk.common.KakaoSdk

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
        KakaoSdk.init(this, BuildConfig.KAKAO_APP_KEY)

        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )

        createNotificationChannel()
    }

    // FCM 알림 채널 생성 (minSdk 28 → NotificationChannel 항상 사용 가능)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            BookiiMessagingService.CHANNEL_ID,
            "부키부키 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "그룹, 약속, 댓글 등 활동 알림"
        }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}