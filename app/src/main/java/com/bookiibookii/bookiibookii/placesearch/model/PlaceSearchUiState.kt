package com.bookiibookii.bookiibookii.placesearch.model

import com.bookiibookii.bookiibookii.data.model.location.PlaceSearchResult

// 장소 검색 화면 UI 상태 (카카오 키워드 검색, 단일 검색 모드)
data class PlaceSearchUiState(
    val query: String = "",
    val searchKeyword: String = "",
    val results: List<PlaceSearchResult> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasNext: Boolean = false,
    val totalCount: Int? = null,
)
