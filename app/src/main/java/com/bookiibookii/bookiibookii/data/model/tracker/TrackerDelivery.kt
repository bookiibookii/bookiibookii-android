package com.bookiibookii.bookiibookii.data.model.tracker

data class TrackerDeliveryRequest(
    val deliveryCompany: String,
    val trackingNumber: String,
    val s3Key: String
)

data class TrackerDeliveryResponse(
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
