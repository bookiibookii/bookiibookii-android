package com.bookiibookii.bookiibookii.data.model.mypage

// 문의하기 리스트 조회 응답
data class InquiryListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<InquirySummary>
)

data class InquirySummary(
    val inquiryId: Int,
    val nickname: String,
    val createdAt: String,     // 질문 작성일
    val title: String,
    val content: String,
    val supportStatus: String, // "PENDING" or "RESOLVED" (예상)
    val adminReply: String?,   // 답변 내용 (없으면 null일 수 있음)
    val resolvedAt: String?    // 답변 작성일
)

// 문의하기 작성 요청
data class InquiryRequest(
    val title: String,
    val content: String
)

// 문의하기 작성 응답 (성공 시 result가 문자열)
data class InquiryCreateResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String
)
