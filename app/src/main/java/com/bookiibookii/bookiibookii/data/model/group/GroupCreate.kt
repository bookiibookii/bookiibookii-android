package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

data class GroupCreateRequest(
    @SerializedName("isbn13") val isbn13: String,
    @SerializedName("groupName") val groupName: String,
    @SerializedName("tradeType") val tradeType: String,         // DIRECT / DELIVERY
    @SerializedName("preferRegion") val preferRegion: String,
    @SerializedName("readingPeriod") val readingPeriod: Int,    // 3, 7, 14, 21, 28
    @SerializedName("groupComment") val groupComment: String?,  // 선택, 최대 500자
    @SerializedName("rules") val rules: List<GroupRuleRequest>  // 1~5개
)

data class GroupRuleRequest(
    // MEMO / POSTIT / PHOTO / ALL_ROUNDER / CUSTOM
    @SerializedName("tag") val tag: String,
    // CUSTOM 일 때만 텍스트, 프리셋 태그는 null
    @SerializedName("content") val content: String?
)

data class GroupCreateResponse(
    @SerializedName("groupId") val groupId: Long?,
    @SerializedName("message") val message: String?
)
