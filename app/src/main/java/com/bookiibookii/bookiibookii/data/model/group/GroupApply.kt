package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

// POST /api/groups/{groupId}/apply 요청
// 게스트가 특정 그룹에 참여 신청을 보낼 때 사용
data class GroupApplyRequest(
    @SerializedName("isbn13") val isbn13: String,   // 교환할 책 ISBN13 (13자리)
    @SerializedName("applyMsg") val applyMsg: String, // 신청 한 마디 (0~50자)
)

// 응답 result = JoinResultDTO
data class GroupApplyResponse(
    @SerializedName("applicationId") val applicationId: Long?,
    @SerializedName("status") val status: String?,
    @SerializedName("createdAt") val createdAt: String?,
)
