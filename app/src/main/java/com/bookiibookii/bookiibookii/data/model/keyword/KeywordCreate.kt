package com.bookiibookii.bookiibookii.data.model.keyword

data class KeywordCreateRequest(
    val content: String
)

data class KeywordCreateResult(
    val keywordId: Long,
    val content: String
)
