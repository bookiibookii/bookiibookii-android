package com.bookiibookii.bookiibookii.data.model.tracker

// 독서 기간(예상 종료일) 수정 요청 — PATCH /api/trackers/{groupId}/reading-period
// newEndDate 형식: "yyyy-MM-dd"
data class ReadingPeriodUpdateReqDTO(
    val newEndDate: String,
)

// 응답 result — {"newEndDate":"2026-06-23","dDay":18}
data class ReadingPeriodUpdateResDTO(
    val newEndDate: String?,
    val dDay: Int?,
)
