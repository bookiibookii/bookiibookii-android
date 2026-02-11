package com.bookiibookii.bookiibookii.home

data class HomeExchangeItem(
    val groupId: Long,
    val bookTitle: String,
    val author: String?,
    val image: String?,
    val withNickname: String,
    val profileUrl: String?,
    val trackerStatus: String?,
    val stepDates: List<String?>?,
    val role: ExchangeRole
)

enum class ExchangeRole { HOST, GUEST }