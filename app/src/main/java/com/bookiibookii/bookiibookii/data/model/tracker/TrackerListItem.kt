package com.bookiibookii.bookiibookii.data.model.tracker

data class TrackerListResDTO(
    val nickname: String?,
    val summary: TrackerSummaryDTO,
    val topBanners: List<TrackerTopBannerResDTO>?,
    val items: List<TrackerListItemResDTO>
)

data class TrackerSummaryDTO(
    val totalCount: Int,
    val readingCount: Int,
    val exchangingCount: Int,
    val reviewCount: Int,
)

data class TrackerTopBannerResDTO(
    val bannerType: String?,
    val groupId: Long?,
    val matchedMemberId: Long?,
    val groupName: String?,
    val title: String?,
    val subtitle: String?,
    val dDayLabel: String?,
    val targetAt: String?,
    val remainingSeconds: Long?,
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
