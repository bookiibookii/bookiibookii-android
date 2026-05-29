package com.bookiibookii.bookiibookii.data.model.tracker

// POST /api/groups/{groupId}/meetings - 직접 교환 약속 등록 요청
data class MeetingRegisterReqDTO(
    val locationId: Long,
    val addressDetail: String?,
    val scheduledAt: String, // ISO date-time (예: "2026-05-20T14:30:00")
)

// 약속 등록 성공(201) 응답
data class MeetingRegisterResDTO(
    val meetingId: Long?,
    val exchangeRound: String?, // FIRST_EXCHANGE | RETURN_EXCHANGE
    val location: MeetingLocationDTO?,
    val addressDetail: String?,
    val scheduledAt: String?,
    val createdBy: MeetingCreatedByDTO?,
)

data class MeetingLocationDTO(
    val locationId: Long?,
    val placeName: String?,
    val address: String?,
    val zipCode: String?,
)

data class MeetingCreatedByDTO(
    val matchedMemberId: Long?,
    val userId: Long?,
    val nickname: String?,
    val role: String?, // HOST | GUEST
)
