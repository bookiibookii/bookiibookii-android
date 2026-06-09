package com.bookiibookii.bookiibookii.data.model.tracker

data class TrackerListResDTO(
    val summary: TrackerSummaryDTO,
    val items: List<TrackerListItemResDTO>
)

data class TrackerSummaryDTO(
    val totalCount: Int,
    val readingCount: Int,
    val exchangingCount: Int,
    val reviewCount: Int,
)

data class TrackerListItemResDTO(
    val groupId: Long,
    val groupName: String?,
    val tradeType: String?,
    val myRole: String?,
    val displayStatus: String?,
    val displayBookTitle: String?,
    val displayStatusLabel: String?,
    val remainingDays: Int?,
    val myCurrentBook: BookInfo?,
    val partnerCurrentBook: BookInfo?
)

data class BookInfo(
    val title: String?,
    val image: String?,
    val totalPages: Int?,
    val currentPage: Int?,
    val isMyOriginalBook: Boolean?,
    val currentReaderNickname: String?,
    val currentReaderProfileImageUrl: String?,
    val currentReadingRate: Int?
)
