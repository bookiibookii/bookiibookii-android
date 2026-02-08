package com.bookiibookii.bookiibookii.trkData.dto

data class TrackerShippingStartRequestDto(
    val deliveryCompany: String,
    val trackingNumber: String,
    val s3Key: String
)

data class TrackerShippingStartResponseDto(
    val bookTitle: String?,
    val partnerNickname: String?,
    val trackerStatus: String?,
    val startDate: String?,
    val endDate: String?,
    val extensionCount: Int?,
    val extensionDays: Int?,
    val trackerId: Long?,
    val deliveryInfo: DeliveryInfoDto?,
    val meetingInfo: MeetingInfoDto?
)



