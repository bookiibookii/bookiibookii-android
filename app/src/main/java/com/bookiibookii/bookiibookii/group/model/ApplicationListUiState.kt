package com.bookiibookii.bookiibookii.group.model

import com.bookiibookii.bookiibookii.data.model.group.GroupAppItem

// 신청자 명단 화면 UI 상태 (호스트가 자신의 그룹 신청자 목록을 조회)
// - items/totalCount: GET /api/groups/{groupId}/applylist 결과
// - loading: 첫 진입/재시도 로딩
// - error: 실패 메시지 (성공 시 null)
data class ApplicationListUiState(
    val items: List<GroupAppItem> = emptyList(),
    val totalCount: Int = 0,
    val loading: Boolean = false,
    val error: String? = null,
)
