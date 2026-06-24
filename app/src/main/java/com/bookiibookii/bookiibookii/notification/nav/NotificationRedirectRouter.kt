package com.bookiibookii.bookiibookii.notification.nav

import android.content.Intent

// 백엔드 RedirectType enum(8종) 기준이며, 모든 라우팅은 groupId 로 식별.
data class NotificationRedirect(
    val redirectType: String,
    val groupId: Long? = null,
    val cardId: Long? = null,
    val title: String? = null,
)

/**
 * 알림 라우팅 정보를 추출하는 공용 라우터.
 *
 * 같은 키 체계(redirectType/groupId/...)를 세 경로에서 공유한다:
 * - FCM 포그라운드: BookiiMessagingService 가 message.data 를 그대로 Intent extra(String)로 실어줌
 * - FCM 백그라운드/종료: OS 가 트레이 클릭 시 data 를 String extra 로 자동 주입 (위와 동일 키)
 * - 인앱 알림: NotificationItem.payload
 *
 * String/Number 를 모두 Long 으로 변환
 */
object NotificationRedirectRouter {

    const val KEY_REDIRECT_TYPE = "redirectType"
    const val KEY_GROUP_ID = "groupId"
    const val KEY_CARD_ID = "cardId"
    const val KEY_TITLE = "title"

    private val KEYS = listOf(KEY_REDIRECT_TYPE, KEY_GROUP_ID, KEY_CARD_ID, KEY_TITLE)

    // 인앱 알림 payload (Map) 용
    fun fromPayload(data: Map<String, *>?): NotificationRedirect? {
        val redirectType = data?.get(KEY_REDIRECT_TYPE)
            ?.toString()?.takeIf { it.isNotBlank() } ?: return null
        return NotificationRedirect(
            redirectType = redirectType,
            groupId = asLong(data[KEY_GROUP_ID]),
            cardId = asLong(data[KEY_CARD_ID]),
            title = data[KEY_TITLE]?.toString()?.takeIf { it.isNotBlank() },
        )
    }

    // FCM 포그라운드/백그라운드 Intent extra (모두 String) 용
    fun fromIntent(intent: Intent?): NotificationRedirect? {
        intent ?: return null
        val data = KEYS.mapNotNull { key -> intent.getStringExtra(key)?.let { key to it } }.toMap()
        return fromPayload(data)
    }

    // FCM(String) / 인앱 payload(Number) 둘 다 Long 으로 변환
    private fun asLong(value: Any?): Long? = when (value) {
        is Number -> value.toLong()
        is String -> value.toLongOrNull()
        else -> null
    }
}
