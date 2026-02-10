package com.bookiibookii.bookiibookii.trkData.dto

data class TrackerDoneResponseDto(
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
