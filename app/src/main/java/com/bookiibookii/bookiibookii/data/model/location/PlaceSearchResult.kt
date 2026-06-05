package com.bookiibookii.bookiibookii.data.model.location

// 카카오 키워드 검색 결과
data class PlaceSearchResult(
    val placeName: String,
    val address: String,
    val x: Double,
    val y: Double,
)

// 검색 결과 한 페이지 (무한스크롤용 페이징 메타 포함)
data class PlaceSearchPage(
    val results: List<PlaceSearchResult>,
    val isEnd: Boolean,
    val totalCount: Int,
)
