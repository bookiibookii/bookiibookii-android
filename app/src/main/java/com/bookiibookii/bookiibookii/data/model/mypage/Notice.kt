package com.bookiibookii.bookiibookii.data.model.mypage

data class NoticeListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<NoticeSummary>,
)

data class NoticeSummary(
    val id: Long,
    val createdAt: String,
    val title: String,
    val summary: String,
)

data class NoticeDetailResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: NoticeDetail,
)

data class NoticeDetail(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: String,
)
