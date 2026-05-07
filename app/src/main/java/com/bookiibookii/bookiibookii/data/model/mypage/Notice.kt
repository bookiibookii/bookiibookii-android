package com.bookiibookii.bookiibookii.data.model.mypage

// 공지사항 리스트 조회 응답
data class NoticeListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<NoticeSummary>
)

data class NoticeSummary(
    val id: Int,
    val createdAt: String, // "2026-02-03T..."
    val title: String,
    val summary: String
)

// 공지사항 상세 조회 응답
data class NoticeDetailResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: NoticeDetail
)

data class NoticeDetail(
    val id: Int,
    val title: String,
    val content: String,
    val createdAt: String
)
