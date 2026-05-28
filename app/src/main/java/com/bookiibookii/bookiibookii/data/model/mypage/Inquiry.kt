package com.bookiibookii.bookiibookii.data.model.mypage

data class InquiryListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<InquirySummary>,
)

data class InquirySummary(
    val inquiryId: Long,
    val userId: Long,
    val nickname: String,
    val createdAt: String,
    val title: String,
    val content: String,
    val supportStatus: String,
    val adminReply: String?,
    val resolvedAt: String?,
)

data class InquiryRequest(
    val title: String,
    val content: String,
)

data class InquiryCreateResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String,
)
