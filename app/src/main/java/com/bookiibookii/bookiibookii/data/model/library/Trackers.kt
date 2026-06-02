package com.bookiibookii.bookiibookii.data.model.library

// 나의 트래커 목록 조회 응답 (GET /api/me/trackers)
data class TrackerResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<TrackerListItemResDTO>?
)

data class TrackerListItemResDTO(
    val groupId: Int,
    val groupName: String,
    val tradeType: String,
    val displayStatus: String,
    val remainingDays: Int,
    val myCurrentBook: BookInfo?,
    val partnerCurrentBook: BookInfo?
)

data class BookInfo(
    val title: String,
    val image: String?,
    val totalPages: Int,
    val currentPage: Int,
    val isOwnerBook: Boolean,
    val currentReaderNickname: String,
    val currentReaderProfileImageUrl: String?,
    val currentReadingRate: Int
)
