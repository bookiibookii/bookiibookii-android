package com.bookiibookii.bookiibookii.trkData.dto

data class HostTrackerListItemDto(
    val groupId: Long,
    val groupType: String,
    val bookTitle: String,
    val image: String?,
    val author: String?,
    val category: String?,
    val relayDetail: HostTrackerRelayDetailDto?,
    val togetherDetail: HostTrackerTogetherDetailDto?
)

data class HostTrackerRelayDetailDto(
    val partnerNickname: String?,
    val hostProfileImage: String?,
    val guestProfileImages: List<String>?,
    val stepDates: List<String?>?
)

data class HostTrackerTogetherDetailDto(
    val hostNickname: String?,
    val participantCount: Int?,
    val myReadingRate: Int?,
    val groupReadingRate: Int?
)