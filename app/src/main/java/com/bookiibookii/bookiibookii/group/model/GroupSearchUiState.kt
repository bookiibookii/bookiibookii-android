package com.bookiibookii.bookiibookii.group.model

import com.bookiibookii.bookiibookii.data.model.group.GroupItem

// 그룹 탐색/검색 화면 UI 상태 (한 화면, 모드 전환)
// - searchKeyword 비어 있음 = 필터 모드 (GET /api/groups)
// - searchKeyword 있음     = 검색 모드 (GET /api/groups/search)
data class GroupSearchUiState(
    val items: List<GroupItem> = emptyList(),
    val loading: Boolean = false,        // 첫 페이지 로딩(진입/필터변경/검색/재시도)
    val loadingMore: Boolean = false,    // 다음 페이지 이어붙이는 중(무한 스크롤)
    val error: String? = null,
    val currentPage: Int = 0,
    val hasNext: Boolean = false,
    // 검색
    val query: String = "",          // 검색바 입력값(제출 전 포함)
    val searchKeyword: String = "",  // 제출되어 현재 결과를 만든 검색어
    val totalCount: Int? = null,     // 검색 결과 개수(검색 모드에서만)
    // 필터 (검색 모드에선 비움). 비어 있으면 전체
    val tradeTypes: List<String> = emptyList(),  // TradeType: DIRECT / DELIVERY
    val regions: List<String> = emptyList(),     // 시+구 문자열, 예: "인천시 미추홀구"
    val categories: List<String> = emptyList(),  // CustomCategory, 예: KOREAN_NOVEL
    val sort: String = "LATEST",                 // GroupSortType
) {
    val isSearchMode: Boolean get() = searchKeyword.isNotBlank()
}
