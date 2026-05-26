package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

// PATCH /api/groups/apply/{applyId} 요청 (UpdateStatusDTO)
// 호스트가 게스트의 참여 신청을 수락/거절할 때 사용
data class GroupAppStatusRequest(
    // 허용 값: "ACCEPTED" / "REJECTED"
    @SerializedName("status") val status: String,
)

// PATCH 성공 응답 result = UpdateResultDTO
data class GroupAppStatusResponse(
    @SerializedName("groupId") val groupId: Long?,
    @SerializedName("updatedAt") val updatedAt: String?,
)
