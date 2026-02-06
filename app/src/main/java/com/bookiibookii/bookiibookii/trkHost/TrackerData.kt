package com.bookiibookii.bookiibookii.trkHost

enum class ExchangeType {
    DELIVERY,
    DIRECT,
    NONE
}

data class TrackerData(
    val id: Long,
    val bookTitle: String,
    val bookAuthor: String,
    val bookCategory: String?,
    val withUserName: String?,
    val coverImageUrl: String?,
    val exchangeType: ExchangeType,

    val stepDates: List<String?>,

    val currentStatus: TrackerStatus,

    val hostProfileImageUrl: String?,
    val guestProfileImageUrl: String?
)