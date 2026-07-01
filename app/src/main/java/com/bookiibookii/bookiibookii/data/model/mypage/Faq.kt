package com.bookiibookii.bookiibookii.data.model.mypage

data class FaqListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<FaqItem>,
)

data class FaqItem(
    val id: Long,
    val question: String,
    val answer: String,
)
