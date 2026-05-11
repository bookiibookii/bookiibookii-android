package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

data class GroupCreateRequest(
    @SerializedName("isbn13") val isbn13: String,
    @SerializedName("maxCapacity") val maxCapacity: Int,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("readingPeriod") val readingPeriod: Int,
    @SerializedName("groupComment") val groupComment: String,
    @SerializedName("customTag") val customTag: String,
    @SerializedName("groupType") val groupType: String,
    @SerializedName("tradeType") val tradeType: String,
    @SerializedName("preferRegion") val preferRegion: String,
    @SerializedName("meetPlace") val meetPlace: String,
    @SerializedName("tags") val tags: List<GroupTagRequest>
)

data class GroupCreateResponse(
    @SerializedName("groupId") val groupId: Long?,
    @SerializedName("message") val message: String?
)
