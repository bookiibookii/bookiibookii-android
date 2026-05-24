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
data class DeliveryAddressRequest(
    val placeName: String,
    val address: String,
    val zipCode: String,
    val addressDetail: String,
)
