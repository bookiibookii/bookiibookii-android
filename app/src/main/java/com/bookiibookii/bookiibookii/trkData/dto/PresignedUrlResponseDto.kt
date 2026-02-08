package com.bookiibookii.bookiibookii.trkData.dto

data class PresignedUrlResponseDto(
    val s3Key: String,
    val presignedPutUrl: String
)

data class TrackerReceiveRequestDto(
    val s3Key: String
)

data class TrackerReceiveResponseDto(
    val bookTitle: String?,
    val partnerNickname: String?,
    val trackerStatus: String?,

    val startDate: String?,
    val endDate: String?,

    val extensionCount: Int?,
    val extensionDays: Int?,

    val trackerId: Long?,

    val deliveryInfo: TrackerDeliveryInfoDto?,
    val meetingInfo: TrackerMeetingInfoDto?
)

data class TrackerDeliveryInfoDto(
    val receiverName: String?,
    val receiverPhone: String?,
    val receiverAddress: String?,
    val deliveryCompany: String?,
    val trackingNumber: String?
)

data class TrackerMeetingInfoDto(
    val meetingTime: String?,
    val meetingPlace: String?
)