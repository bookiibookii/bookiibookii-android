package com.bookiibookii.bookiibookii.data.model.library

data class TrackerResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<TrackerResult>?
)

data class TrackerResult(
    val groupId: Int,
    val groupType: String,
    val tradeType: String?,
    val bookTitle: String?,
    val bookImage: String?,
    val bookAuthor: String?,
    val bookCategory: String?,
    val togetherDetail: TogetherDetail?,
    val relayDetail: RelayDetail?
)

data class TogetherDetail(
    val hostNickname: String?,
    val participantCount: Int,
    val myReadingRate: Int,    // ★ 내 독서율
    val groupReadingRate: Int  // ★ 그룹 평균 독서율
)

data class RelayDetail(
    val trackerStatus: String?,
    val partnerNickname: String?,
    val hostProfileImageUrl: String?,
    val guestProfileImageUrls: List<String>?,
    val stepDates: List<String>?
)

data class CompleteReadingResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: CompleteReadingResult?
)

data class CompleteReadingResult(
    val matchedMemberId: Int,
    val currentReadingRate: Int,
    val completedAt: String
)
