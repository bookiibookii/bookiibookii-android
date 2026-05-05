package com.bookiibookii.bookiibookii.data.model.tracker

data class TrackerCompletionResponse(
    val bookTitle: String?,
    val partnerNickname: String?,
    val trackerStatus: String?,
    val startDate: String?,
    val endDate: String?,
    val extensionCount: Int?,
    val extensionDays: Int?,
    val trackerId: Long?,
    val deliveryInfo: DeliveryInfo?,
    val meetingInfo: MeetingInfo?
)
