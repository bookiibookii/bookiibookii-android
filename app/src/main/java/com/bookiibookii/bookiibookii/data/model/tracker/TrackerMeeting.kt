package com.bookiibookii.bookiibookii.data.model.tracker

data class TrackerMeetingRequest(
    val meetingTime: String,
    val meetingPlace: String
)

data class TrackerMeetingResponse(
    val meetingTime: String?,
    val meetingPlace: String?
)
