package com.bookiibookii.bookiibookii.data.model.location

import com.google.gson.annotations.SerializedName

// 희망 교환 장소 (직접 교환)
data class ExchangeAddress(
    @SerializedName("Id") val id: Long,
    val placeName: String,
    val address: String,
    val zipCode: String,
    val x: Double,
    val y: Double,
    val addressDetail: String?,   // null 가능
    val isDefault: Boolean,
)

// POST/PUT 공용 요청 body
data class ExchangeAddressRequest(
    val placeName: String,
    val address: String,
    val zipCode: String,
    val x: Double,
    val y: Double,
    val addressDetail: String?,
)
