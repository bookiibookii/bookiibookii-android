package com.bookiibookii.bookiibookii.data.model.location

import com.google.gson.annotations.SerializedName

// 배송지 (택배 교환)
data class DeliveryAddress(
    @SerializedName("Id") val id: Long,
    val placeName: String,
    val address: String,
    val zipCode: String,
    val addressDetail: String,
    val receiverName: String,
    val phone: String,
    val isDefault: Boolean,
)

// POST/PUT 공용 요청 body
// 백엔드 UserDeliveryReqDTO.AddReqDTO 기준. addressDetail 제외 모두 필수
data class DeliveryAddressRequest(
    val placeName: String,
    val address: String,
    val zipCode: String,
    val addressDetail: String?,
    val receiverName: String,
    val phone: String,          // 형식: 02-123-4567 / 010-1234-5678 (^\d{2,3}-\d{3,4}-\d{4}$)
)
