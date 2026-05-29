package com.bookiibookii.bookiibookii.data.model.tracker

data class DeliveryAddressResDTO(
    val myAddress: DeliveryAddressItemDTO?,
    val partnerAddress: DeliveryAddressItemDTO?,
    val canEditMyAddress: Boolean?,
)

data class DeliveryAddressItemDTO(
    val receiverName: String?,
    val phoneNumber: String?,
    val address: String?,
    val addressDetail: String?,
    val zipCode: String?,
)

data class DeliveryAddressUpdateReqDTO(
    val receiverName: String,
    val phoneNumber: String,
    val address: String,
    val addressDetail: String,
    val zipCode: String,
)
