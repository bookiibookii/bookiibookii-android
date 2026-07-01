package com.bookiibookii.bookiibookii.data.model.mypage

data class NoticeListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<NoticeSummary>,
)

data class NoticeSummary(
    val id: Long,
    val updatedAt: String,
    val title: String,
    val summary: String,
    val isRead: Boolean,
    val authorNickname: String,
    val authorProfileImageUrl: String?,
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
    val summary: String,
    val content: String,
    val authorNickname: String,
    val authorProfileImageUrl: String?,
    val createdAt: String,
    val updatedAt: String,
)
