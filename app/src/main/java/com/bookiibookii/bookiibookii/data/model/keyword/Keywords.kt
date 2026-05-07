package com.bookiibookii.bookiibookii.data.model.keyword

data class KeywordListResult(
    val keywordSort: String,
    val keywordNumber: Int,
    val keywordList: List<KeywordItem>
)
