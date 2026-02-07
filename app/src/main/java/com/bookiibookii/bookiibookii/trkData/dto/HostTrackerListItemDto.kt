package com.bookiibookii.bookiibookii.trkData.dto

import com.google.gson.annotations.SerializedName

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
    val trackerStatus: String?,
    val stepDates: List<String?>?
)

data class HostTrackerTogetherDetailDto(
    val hostNickname: String?,
    val participantCount: Int?,
    val myReadingRate: Int?,
    val groupReadingRate: Int?
)

data class TrackerDetailDto(
    val trackerId: Long,
    val trackerStatus: String,
    val currentMatchedMemberId: Long?,

    val endDate: String?,

    @SerializedName("extension_count")
    val extensionCount: Int?,

    @SerializedName("extension_days")
    val extensionDays: Int?
)

data class ApiResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: T?
)

