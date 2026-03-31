package com.bookiibookii.bookiibookii.trkHost

enum class ExchangeType {
    DELIVERY,
    DIRECT,
    NONE
}

enum class ExchangeRole { HOST, GUEST }

data class TrackerData(
    val id: Long,
    val groupId: Long,
    val bookTitle: String,
    val bookAuthor: String,
    val bookCategory: String?,
    val coverImageUrl: String?,
    val exchangeType: ExchangeType,
    val role: ExchangeRole,

    val withUserName: String?,
    val stepDates: List<String?>,
    val currentStatus: TrackerStatus,
    val hostProfileImageUrl: String?,
    val guestProfileImageUrl: String?,

    val myReadingRate: Int? = null,
    val groupReadingRate: Int? = null
)
