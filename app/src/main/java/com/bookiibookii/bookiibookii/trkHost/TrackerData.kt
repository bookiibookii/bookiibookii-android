package com.bookiibookii.bookiibookii.trkHost

data class TrackerData(
    val id: Long,
    val bookTitle: String,
    val bookAuthor: String,
    val withUserName: String?,
    val coverImageUrl: String?,
    val currentStep: TrackerStep,
    val exchangeType: ExchangeType
)

enum class TrackerStep {
    READING,
    DELIVERY,
    GUEST_READING,
    RETURN
}

enum class ExchangeType {
    SHIPPING,
    DIRECT
}



