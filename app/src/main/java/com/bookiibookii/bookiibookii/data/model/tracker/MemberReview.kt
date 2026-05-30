package com.bookiibookii.bookiibookii.data.model.tracker

// POST /api/groups/{groupId}/member-reviews - 파트너 후기 등록 요청
data class MemberReviewCreateReqDTO(
    val reaction: String?, // BOOM_UP | BOOM_DOWN | null(선택)
    val comment: String,   // 필수, 최대 20자
)

// 파트너 후기 등록 성공(201) 응답
data class MemberReviewResDTO(
    val reviewId: Long?,
    val groupCompleted: Boolean?,
)
