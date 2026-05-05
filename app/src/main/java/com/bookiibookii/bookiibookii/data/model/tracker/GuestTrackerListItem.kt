package com.bookiibookii.bookiibookii.data.model.tracker

import com.google.gson.annotations.SerializedName

data class GuestTrackerListItem(
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
    val relayDetail: GuestTrackerRelayDetail?,
    val togetherDetail: GuestTrackerTogetherDetail?
)

data class GuestTrackerRelayDetail(
    val partnerNickname: String?,
    @SerializedName("hostProfileImageUrl")
    val hostProfileImage: String?,
    @SerializedName("guestProfileImageUrls")
    val guestProfileImages: List<String>?,
    val trackerStatus: String?,
    val stepDates: List<String?>?
)

data class GuestTrackerTogetherDetail(
    val hostNickname: String?,
    val participantCount: Int?,
    val myReadingRate: Int?,
    val groupReadingRate: Int?
)
