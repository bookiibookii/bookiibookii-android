package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.location.DeliveryAddress
import com.bookiibookii.bookiibookii.data.model.location.DeliveryAddressRequest
import com.bookiibookii.bookiibookii.data.model.location.ExchangeAddress
import com.bookiibookii.bookiibookii.data.model.location.ExchangeAddressRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface LocationApi {

    // === 희망 교환 장소 (직접 교환) ===

    // 희망 교환 장소 목록 조회
    @GET("api/mypage/addresses/exchanges")
    suspend fun getExchanges(): Response<ApiResponse<List<ExchangeAddress>>>

    // 희망 교환 장소 추가 (최대 2개)
    @POST("api/mypage/addresses/exchanges")
    suspend fun addExchange(
        @Body request: ExchangeAddressRequest,
    ): Response<ApiResponse<String>>

    // 희망 교환 장소 수정
    @PUT("api/mypage/addresses/exchanges/{userExchangeId}")
    suspend fun updateExchange(
        @Path("userExchangeId") userExchangeId: Long,
        @Body request: ExchangeAddressRequest,
    ): Response<ApiResponse<String>>

    // 희망 교환 장소 삭제
    @DELETE("api/mypage/addresses/exchanges/{userExchangeId}")
    suspend fun deleteExchange(
        @Path("userExchangeId") userExchangeId: Long,
    ): Response<ApiResponse<String>>

    // 대표 희망 교환 장소 설정
    @PATCH("api/mypage/addresses/exchanges/{userExchangeId}/default")
    suspend fun setDefaultExchange(
        @Path("userExchangeId") userExchangeId: Long,
    ): Response<ApiResponse<String>>

    // === 배송지 (택배 교환) ===

    // 배송지 목록 조회
    @GET("api/mypage/addresses/deliveries")
    suspend fun getDeliveries(): Response<ApiResponse<List<DeliveryAddress>>>

    // 배송지 추가 (최대 2개)
    @POST("api/mypage/addresses/deliveries")
    suspend fun addDelivery(
        @Body request: DeliveryAddressRequest,
    ): Response<ApiResponse<String>>

    // 배송지 수정
    @PUT("api/mypage/addresses/deliveries/{userDeliveryId}")
    suspend fun updateDelivery(
        @Path("userDeliveryId") userDeliveryId: Long,
        @Body request: DeliveryAddressRequest,
    ): Response<ApiResponse<String>>

    // 배송지 삭제
    @DELETE("api/mypage/addresses/deliveries/{userDeliveryId}")
    suspend fun deleteDelivery(
        @Path("userDeliveryId") userDeliveryId: Long,
    ): Response<ApiResponse<String>>

    // 대표 배송지 설정
    @PATCH("api/mypage/addresses/deliveries/{userDeliveryId}/default")
    suspend fun setDefaultDelivery(
        @Path("userDeliveryId") userDeliveryId: Long,
    ): Response<ApiResponse<String>>
}
