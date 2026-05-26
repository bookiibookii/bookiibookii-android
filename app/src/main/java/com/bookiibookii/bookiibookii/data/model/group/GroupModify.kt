package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

// PATCH /api/groups/{groupId} 요청 (UpdateDTO)
// 방장이 모집 중인 그룹 정보를 수정 (그룹명/독서 기간/소개글/규칙)
data class GroupModifyRequest(
    @SerializedName("readingPeriod") val readingPeriod: Int,
    @SerializedName("groupComment") val groupComment: String?,
    @SerializedName("groupName") val groupName: String,
    @SerializedName("rules") val rules: List<GroupRuleRequest>,  // 1~5개
)

// PATCH 성공 응답 result = UpdateResultDTO
data class GroupModifyResponse(
    @SerializedName("groupId") val groupId: Long?,
    @SerializedName("updatedAt") val updatedAt: String?,
)
