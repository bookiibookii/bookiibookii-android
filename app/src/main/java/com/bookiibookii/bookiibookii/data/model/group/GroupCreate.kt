package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

data class GroupCreateRequest(
    @SerializedName("isbn13") val isbn13: String,
    @SerializedName("groupName") val groupName: String,
    @SerializedName("tradeType") val tradeType: String,         // DIRECT / DELIVERY
    // tradeType에 따라 하나만 채우고 나머지는 null
    @SerializedName("userDeliveryId") val userDeliveryId: Long?, // DELIVERY일 때만 (선택한 배송지 id)
    @SerializedName("userExchangeId") val userExchangeId: Long?, // DIRECT일 때만 (선택한 희망교환장소 id)
    @SerializedName("readingPeriod") val readingPeriod: Int,    // 3, 7, 14, 21, 28
    @SerializedName("groupComment") val groupComment: String?,  // 선택, 최대 500자
    @SerializedName("rules") val rules: List<GroupRuleRequest>  // 1~5개
)

data class GroupRuleRequest(
    // MEMO / POSTIT / PHOTO / All_ROUNDER / NO_IDEA / CUSTOM
    @SerializedName("tag") val tag: String,
    // CUSTOM 일 때만 텍스트, 프리셋 태그는 null
    @SerializedName("content") val content: String?
)

// 응답 result = CreateResultDTO (message는 ApiResponse 래퍼에 있음)
data class GroupCreateResponse(
    @SerializedName("groupId") val groupId: Long?,
    @SerializedName("groupStatus") val groupStatus: String?,
    @SerializedName("createdAt") val createdAt: String?,
)
