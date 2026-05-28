package com.bookiibookii.bookiibookii.data.model.tracker

data class TrackerListItemResDTO(
    val groupId: Long,
    val groupName: String?,
    val tradeType: String?,
    val displayStatus: String?,
    val remainingDays: Int?,
    val myCurrentBook: BookInfo?,
    val partnerCurrentBook: BookInfo?
)

data class BookInfo(
    val title: String?,
    val image: String?,
    val totalPages: Int?,
    val currentPage: Int?,
    val isOwnerBook: Boolean?,
    val currentReaderNickname: String?,
    val currentReaderProfileImageUrl: String?,
    val currentReadingRate: Int?
)
