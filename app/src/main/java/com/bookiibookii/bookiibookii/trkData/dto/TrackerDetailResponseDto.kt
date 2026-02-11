package com.bookiibookii.bookiibookii.trkData.dto

data class TrackerDetailResponseDto(
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

data class DeliveryInfoDto(
    val receiverName: String?,
    val receiverPhone: String?,
    val receiverAddress: String?,
    val deliveryCompany: String?,
    val trackingNumber: String?,
    val isVerified: Boolean?
)

data class MeetingInfoDto(
    val meetingTime: String?,
    val meetingPlace: String?
)
