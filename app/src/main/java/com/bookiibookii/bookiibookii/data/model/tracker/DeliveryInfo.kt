package com.bookiibookii.bookiibookii.data.model.tracker

data class DeliveryInfo(
    val receiverName: String?,
    val receiverPhone: String?,
    val receiverAddress: String?,
    val deliveryCompany: String?,
    val trackingNumber: String?,
    val isVerified: Boolean?
)
