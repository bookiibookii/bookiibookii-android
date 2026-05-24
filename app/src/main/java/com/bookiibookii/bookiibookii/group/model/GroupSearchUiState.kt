package com.bookiibookii.bookiibookii.group.model

import com.bookiibookii.bookiibookii.data.model.group.GroupItem

// 그룹 탐색(목록) 화면 UI 상태 — GET /api/groups
data class GroupSearchUiState(
    val items: List<GroupItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0,
    val hasNext: Boolean = false,
    // 필터 (비어 있으면 전체). 화면 진입 시 기본값은 모두 비움 = 전체 목록
    val tradeTypes: List<String> = emptyList(),  // TradeType: DIRECT / DELIVERY
    val regions: List<String> = emptyList(),     // 시+구 문자열, 예: "인천시 미추홀구"
    val categories: List<String> = emptyList(),  // CustomCategory, 예: ECON_BIZ
    val sort: String = "LATEST",                 // GroupSortType
)
