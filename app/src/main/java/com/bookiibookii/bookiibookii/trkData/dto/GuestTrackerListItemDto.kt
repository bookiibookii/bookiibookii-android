package com.bookiibookii.bookiibookii.trkData.dto

import com.google.gson.annotations.SerializedName

data class GuestTrackerListItemDto(
    val groupId: Long,
    val groupType: String,
    val bookTitle: String,

    @SerializedName("bookImage")
    val image: String?,
    @SerializedName("bookAuthor")
    val author: String?,
    @SerializedName("bookCategory")
    val category: String?,

    val tradeType: String?,
    val relayDetail: HostTrackerRelayDetailDto?,
    val togetherDetail: HostTrackerTogetherDetailDto?
)


data class GuestTrackerRelayDetailDto(
    val partnerNickname: String?,
    val hostProfileImage: String?,
    val guestProfileImages: List<String>?,
    val trackerStatus: String?,
    val stepDates: List<String?>?
)

data class GuestTrackerTogetherDetailDto(
    val hostNickname: String?,
    val participantCount: Int?,
    val myReadingRate: Int?,
    val groupReadingRate: Int?
)



