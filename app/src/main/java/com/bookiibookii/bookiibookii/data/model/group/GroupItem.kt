package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

data class GroupItem(
    @SerializedName("groupId") val groupId: Long,
    @SerializedName("groupName") val groupName: String,
    @SerializedName(value = "title", alternate = ["bookTitle"]) val title: String?,
    @SerializedName("author") val author: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName(value = "bookImage", alternate = ["bookCoverImageUrl"]) val bookImage: String?,
    @SerializedName("hostNickname") val hostNickname: String?,
    @SerializedName("hostProfileImageUrl") val hostProfileImageUrl: String?,
    @SerializedName("groupStatus") val groupStatus: String,
    @SerializedName("currentCount") val currentCount: Int,
    @SerializedName("maxCapacity") val maxCapacity: Int,
    @SerializedName("waitingCount") val waitingCount: Int,
    val isHot: Boolean,
    val tradeType: String?,
    @SerializedName("readingPeriod") val readingPeriod: Int,
    @SerializedName("pictureBadge") val pictureBadge: String?,
    @SerializedName("groupType") val groupType: String? = null,
    @SerializedName("displayStatus") val displayStatus: String? = null,
)
