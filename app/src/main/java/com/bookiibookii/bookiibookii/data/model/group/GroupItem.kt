package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

data class GroupItem(
    @SerializedName("groupId") val groupId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("bookImage") val bookImage: String?,
    @SerializedName("hostProfileImageUrl") val hostProfileImageUrl: String?,
    @SerializedName("hostNickname") val hostNickname: String?,
    @SerializedName("tags") val tags: List<String>?,
    @SerializedName("groupStatus") val groupStatus: String,
    @SerializedName("currentCount") val currentCount: Int,
    @SerializedName("maxCapacity") val maxCapacity: Int,
    @SerializedName("readingPeriod") val readingPeriod: Int,
    @SerializedName("customTag") val customTag: String?,
    val groupType: String,
    val tradeType: String?,
    val startDate: String?,
    val isHot: Boolean,
    @SerializedName("pictureBadge") val pictureBadge: String?
)
