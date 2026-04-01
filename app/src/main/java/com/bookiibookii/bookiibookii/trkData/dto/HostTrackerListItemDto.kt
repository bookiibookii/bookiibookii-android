package com.bookiibookii.bookiibookii.trkData.dto

import com.google.gson.annotations.SerializedName

data class HostTrackerListItemDto(
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


data class HostTrackerRelayDetailDto(
    val partnerNickname: String?,
    @SerializedName("hostProfileImageUrl")
    val hostProfileImage: String?,
    @SerializedName("guestProfileImageUrls")
    val guestProfileImages: List<String>?,
    val trackerStatus: String?,
    val stepDates: List<String?>?
)

data class HostTrackerTogetherDetailDto(
    val hostNickname: String?,
    val participantCount: Int?,
    val myReadingRate: Int?,
    val groupReadingRate: Int?
)

data class ApiResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: T?
)

