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

// 이번 교환 배송지 변경 - 기존(마이페이지 등록) 배송지 선택
data class DeliveryAddressSavedUpdateReqDTO(
    val userDeliveryId: Long,
)

// 이번 교환 배송지 변경 - 직접 입력 (주소만 변경)
data class DeliveryAddressDirectUpdateReqDTO(
    val zipCode: String,
    val address: String,
    val addressDetail: String,
)
