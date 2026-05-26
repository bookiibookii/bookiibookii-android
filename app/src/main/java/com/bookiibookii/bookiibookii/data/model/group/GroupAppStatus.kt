package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

// PATCH /api/groups/apply/{applyId} 요청 (UpdateStatusDTO)
// 호스트가 게스트의 참여 신청을 수락/거절할 때 사용
data class GroupAppStatusRequest(
    // 허용 값: "ACCEPTED" / "REJECTED" (스펙 enum에는 "PENDING"도 있지만 클라이언트에서 보내지 않음)
    @SerializedName("status") val status: String,
)

// PATCH 성공 응답 result = UpdateResultDTO (스펙 required=[])
// ⚠️ ApiResponse<String>으로 두면 객체→문자열 파싱 예외 → catch 빠짐 (modify와 동일 버그 패턴)
data class GroupAppStatusResponse(
    @SerializedName("groupId") val groupId: Long?,
    @SerializedName("updatedAt") val updatedAt: String?,
)
