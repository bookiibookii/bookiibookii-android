package com.bookiibookii.bookiibookii.trkData.dto

data class TrackerDetailDto(
    val bookTitle: String,
    val partnerNickname: String,
    val trackerStatus: String,
    val startDate: String?,
    val endDate: String?,
    val extensionCount: Int,
    val extensionDays: Int,
    val trackerId: Long,
    val deliveryInfo: DeliveryInfo?,
    val meetingInfo: MeetingInfo?
)

data class DeliveryInfo(
    val receiverName: String?,
    val receiverPhone: String?,
    val receiverAddress: String?,
    val deliveryCompany: String?,
    val trackingNumber: String?,
    val isVerified: Boolean?
)

data class MeetingInfo(
    val meetingTime: String?,
    val meetingPlace: String?
)

data class MakeMeetingRequest(
    val meetingTime: String,
    val meetingPlace: String
)