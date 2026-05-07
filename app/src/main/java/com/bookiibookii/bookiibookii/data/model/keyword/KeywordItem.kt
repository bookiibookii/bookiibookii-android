package com.bookiibookii.bookiibookii.data.model.keyword

enum class KeywordSort {
    LATEST,
    ALPHABETICAL
}

data class KeywordItem(
    val keywordId: Long,
    val content: String
)
