package com.bookiibookii.bookiibookii.notification.fcm

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM 푸시 수신 진입점
 * - onNewToken: 토큰 발급/갱신 시 시스템이 자동 호출
 * - onMessageReceived: 앱이 포그라운드일 때 푸시 수신
 */
class BookiiMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken: $token")
        // 로그인 상태일 때만 서버에 갱신 토큰 등록
        if (TokenManager.hasAccessToken(applicationContext)) {
            FcmTokenRegistrar.registerToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "onMessageReceived from=${message.from}, data=${message.data}")

        // notification 페이로드 기준
        val title = message.notification?.title ?: "부키부키"
        val body = message.notification?.body ?: ""

        showNotification(title, body, message.data)
    }

    // data 는 클릭 시 화면 이동을 위해 Intent extra(String)로 그대로 싣는다.
    // 백그라운드/종료 상태에서 OS 가 트레이 클릭으로 주입하는 extra 와 같은 형식 →
    // MainActivity 가 양쪽을 동일하게 라우팅한다.
    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        // 알림마다 다른 ID — 알림이 덮어쓰지 않고 쌓이며, PendingIntent extra 도 분리된다.
        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_symbol)
            .setColor(ContextCompat.getColor(this, R.color.ui_main))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "POST_NOTIFICATIONS 권한 없음 — 알림 표시 생략")
            return
        }

        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    companion object {
        private const val TAG = "FCM"
        const val CHANNEL_ID = "bookii_default"
    }
}
