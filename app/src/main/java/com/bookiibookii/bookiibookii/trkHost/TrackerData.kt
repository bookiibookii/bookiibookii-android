package com.bookiibookii.bookiibookii.trkHost

data class TrackerData(
    val id: Long,
    val bookTitle: String,
    val bookAuthor: String,
    val bookCategory: String?,
    val withUserName: String?,
    val coverImageUrl: String?,
    val exchangeType: ExchangeType,

    val stepDates: List<String?>,
    val currentStep: TrackerStep,

    val hostProfileImageUrl: String?,
    val guestProfileImageUrl: String?
)


enum class TrackerStep {
    HOST_READING,
    SHIPPING,
    GUEST_READING,
    RETURNING
}

enum class ExchangeType {
    SHIPPING,
    DIRECT
}



