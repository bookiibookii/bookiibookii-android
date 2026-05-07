package com.bookiibookii.bookiibookii.home.notification.data

import com.bookiibookii.bookiibookii.data.api.ApiService
import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.notification.NotificationItem
import com.bookiibookii.bookiibookii.data.model.notification.NotificationListResult
import retrofit2.Response

class NotificationRepository(
    private val api: ApiService
) {
    // 카테고리(SYSTEM/KEYWORD) 공용 조회
    suspend fun fetchNotifications(
        category: String,
        cursor: String?,
        size: Int
    ): Response<ApiResponse<NotificationListResult>> {
        return api.getNotifications(
            category = category,
            cursor = cursor,
            size = size
        )
    }

    // 읽음 처리
    suspend fun read(notificationId: Long): Response<ApiResponse<NotificationItem>> {
        return api.readNotification(notificationId)
    }
}
