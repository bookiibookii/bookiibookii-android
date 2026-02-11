package com.bookiibookii.bookiibookii.home.notification.model

import com.bookiibookii.bookiibookii.data.model.NotificationCategory

enum class NotificationType(
    val category: NotificationCategory
) {
    // GROUP / MATCHING (SYSTEM)
    GROUP_JOIN_REQUEST(NotificationCategory.SYSTEM),          // GRP-030 (요청관리)
    GROUP_MATCH_SUCCESS(NotificationCategory.SYSTEM),         // TRK-010 (트래커)
    GROUP_MATCH_REJECTED(NotificationCategory.SYSTEM),        // GRP-001 (리스트)
    GROUP_MATCH_AUTO_REJECTED(NotificationCategory.SYSTEM),   // GRP-001 (리스트)
    GROUP_COMMENT_CREATED(NotificationCategory.SYSTEM),       // GRP-010 (그룹 상세/댓글)
    GROUP_DELETED(NotificationCategory.SYSTEM),               // 문의하기 or GRP-001
    GROUP_MATCH_FAILED_BY_EXPIRE(NotificationCategory.SYSTEM),// GRP-001 (리스트)
    GROUP_MATCH_FAILED_BY_CAPACITY(NotificationCategory.SYSTEM), // GRP-001 (리스트)

    // TRACKER (SYSTEM)
    TRACKER_READING_STARTED(NotificationCategory.SYSTEM),     // TRK-010
    TRACKER_PERIOD_EXTENDED(NotificationCategory.SYSTEM),     // TRK-010
    TRACKER_READING_FINISHED(NotificationCategory.SYSTEM),    // TRK-010
    TRACKER_SHIPMENT_REGISTERED(NotificationCategory.SYSTEM), // TRK-010
    TRACKER_DELIVERY_CONFIRMED(NotificationCategory.SYSTEM),  // TRK-010
    TRACKER_RETURN_SHIPMENT_REGISTERED(NotificationCategory.SYSTEM), // TRK-010 (반납 출발)
    TRACKER_EXCHANGE_COMPLETED(NotificationCategory.SYSTEM),  // TRK-030 (후기작성)

    // KEYWORD
    KEYWORD_GROUP_CREATED(NotificationCategory.KEYWORD),      // GRP-010 (해당그룹)

    UNKNOWN(NotificationCategory.SYSTEM);

    companion object {
        fun from(value: String?): NotificationType {
            return values().firstOrNull { it.name == value } ?: UNKNOWN
        }
    }
}