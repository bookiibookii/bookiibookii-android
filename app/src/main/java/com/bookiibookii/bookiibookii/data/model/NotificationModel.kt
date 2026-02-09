package com.bookiibookii.bookiibookii.data.model

// 알림 카테고리 (일단 SYSTEM만 사용, KEYWORD는 나중에 붙이면 됨)
enum class NotificationCategory {
    SYSTEM,
    KEYWORD
}

// 알림 아이템
data class NotificationItemDto(
    val id: Long,
    val type: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String, // 서버가 ISO String 주는 형태라 일단 String으로 받고 필요 시 파싱
    val readAt: String?,   // null 가능
    val payload: Map<String, Any?>? // payload 구조가 아직 유동이면 일단 Map으로
)

// 목록 결과
data class NotificationListResultDto(
    val items: List<NotificationItemDto>,
    val nextCursor: String?,
    val hasNext: Boolean
)