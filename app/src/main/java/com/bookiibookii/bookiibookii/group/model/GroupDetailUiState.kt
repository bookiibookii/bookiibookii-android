package com.bookiibookii.bookiibookii.group.model

import com.bookiibookii.bookiibookii.data.model.group.GroupDetailResponse

// 그룹 상세 화면 UI 상태
// - detail: GET /api/groups/{groupId}
// - actionButton: buttonStatus + waitingCount를 VM에서 미리 매핑한 결과 (null이면 버튼 미표시)
// - loading: 첫 진입/재시도 로딩
// - error: 실패 메시지 (성공 시 null)
data class GroupDetailUiState(
    val detail: GroupDetailResponse? = null,
    val actionButton: GroupDetailActionButton? = null,
    val loading: Boolean = false,
    val error: String? = null,
)
