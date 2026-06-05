package com.bookiibookii.bookiibookii.data.model.tracker

// POST /api/groups/{groupId}/meetings - 직접 교환 약속 등록
data class MeetingRegisterReqDTO(
    val placeName: String,       // 장소명 [0, 100]
    val address: String,         // 주소 [0, 200]
    val zipCode: String?,        // 우편번호(선택) [0, 10]
    val x: Double,               // X 좌표(경도) [-180, 180]
    val y: Double,               // Y 좌표(위도) [-90, 90]
    val addressDetail: String?,  // 약속별 상세 주소/설명 [0, 200]
    val scheduledAt: String,     // ISO date-time
)

// MeetingResponseDTO - 약속 등록(POST 201) / 조회(GET 200) 공용 응답
data class MeetingResDTO(
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
    val addressDetail: String?,
)

data class MeetingCreatedByDTO(
    val matchedMemberId: Long?,
    val userId: Long?,
    val nickname: String?,
    val role: String?, // HOST | GUEST
)
