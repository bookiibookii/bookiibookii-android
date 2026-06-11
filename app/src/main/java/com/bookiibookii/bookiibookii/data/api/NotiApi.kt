package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.notification.DeviceTokenDeactivateRequest
import com.bookiibookii.bookiibookii.data.model.notification.DeviceTokenRegisterRequest
import com.bookiibookii.bookiibookii.data.model.notification.NotificationListResult
import com.bookiibookii.bookiibookii.data.model.notification.NotificationReadResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
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
    ): Response<ApiResponse<NotificationReadResult>>

    // FCM 기기 토큰 등록
    @POST("api/device-tokens")
    suspend fun registerDeviceToken(
        @Body request: DeviceTokenRegisterRequest
    ): Response<ApiResponse<String>>

    // FCM 기기 토큰 등록 해제 (DELETE + body → @HTTP 사용)
    @HTTP(method = "DELETE", path = "api/device-tokens", hasBody = true)
    suspend fun deactivateDeviceToken(
        @Body request: DeviceTokenDeactivateRequest
    ): Response<ApiResponse<String>>
}