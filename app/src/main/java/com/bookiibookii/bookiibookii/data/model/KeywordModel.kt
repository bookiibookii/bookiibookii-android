package com.bookiibookii.bookiibookii.data.model

// 정렬 옵션 (스웨거: LATEST, ALPHABETICAL)
enum class KeywordSort {
    LATEST,
    ALPHABETICAL
}

// 키워드 단건
data class KeywordItemDto(
    val keywordId: Long,
    val content: String
)

// 키워드 목록 result
data class KeywordListResultDto(
    val keywordSort: String,
    val keywordNumber: Int,
    val keywordList: List<KeywordItemDto>
)

// 키워드 등록 request
data class KeywordCreateRequest(
    val content: String
)

// 키워드 등록 result
data class KeywordCreateResultDto(
    val keywordId: Long,
    val content: String
)