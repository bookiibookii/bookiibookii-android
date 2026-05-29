package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

// GET /api/groups/home 응답 result
data class HomeGroupsResponse(
    @SerializedName("newGroups") val newGroups: List<GroupItem> = emptyList(),
    @SerializedName("categorySection") val categorySection: HomeCategorySection? = null,
    @SerializedName("regionSection") val regionSection: HomeRegionSection? = null,
)

// 카테고리 기반 섹션 — category=null이면 추천 불가
data class HomeCategorySection(
    @SerializedName("category") val category: String?,
    @SerializedName("groups") val groups: List<GroupItem> = emptyList(),
)

// 교환 지역 기반 섹션 — region=null이면 교환 장소 미설정
data class HomeRegionSection(
    @SerializedName("region") val region: String?,
    @SerializedName("groups") val groups: List<GroupItem> = emptyList(),
)
