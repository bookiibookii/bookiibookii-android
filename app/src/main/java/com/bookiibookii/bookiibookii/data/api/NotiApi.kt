package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.notification.NotificationItem
import com.bookiibookii.bookiibookii.data.model.notification.NotificationListResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotiApi {

    // 알림 목록 조회
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("category") category: String,
        @Query("cursor") cursor: String?,
        @Query("size") size: Int
    ): Response<ApiResponse<NotificationListResult>>

    // 알림 읽음 처리
    @PATCH("api/notifications/{notificationId}/read")
    suspend fun readNotification(
        @Path("notificationId") notificationId: Long
    ): Response<ApiResponse<NotificationItem>>
}