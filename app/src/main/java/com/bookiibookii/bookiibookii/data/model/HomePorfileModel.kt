package com.bookiibookii.bookiibookii.data.model

import com.google.gson.annotations.SerializedName

data class OtherProfileResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: OtherProfileApiResult?
)

data class OtherProfileApiResult(
    val userId: Int,
    val profileImageUrl: String?,
    val nickname: String,
    val manner: Double,
    val topTags: List<String>?,
    val completeBook: Int,

    @SerializedName("relayGroup")
    val readingGroup: Int,   // ✅ 앱 내부 네이밍

    val togetherGroup: Int
)