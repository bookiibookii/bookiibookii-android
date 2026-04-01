package com.bookiibookii.bookiibookii.trkHost

import com.bookiibookii.bookiibookii.trkData.dto.GuestTrackerListItemDto
import com.bookiibookii.bookiibookii.trkData.dto.HostTrackerListItemDto

fun HostTrackerListItemDto.toTrackerData(): TrackerData {
    val base = buildBase(groupId, bookTitle, author, category, image, tradeType, ExchangeRole.HOST)
    return when (base.exchangeType) {
        ExchangeType.DELIVERY, ExchangeType.DIRECT ->
            buildRelayData(base, relayDetail?.partnerNickname, relayDetail?.hostProfileImage,
                relayDetail?.guestProfileImages, relayDetail?.trackerStatus, relayDetail?.stepDates)
        ExchangeType.NONE ->
            buildTogetherData(base, togetherDetail?.hostNickname, togetherDetail?.participantCount,
                togetherDetail?.myReadingRate, togetherDetail?.groupReadingRate, TrackerStatus.HOST_READING)
    }
}

fun GuestTrackerListItemDto.toTrackerData(): TrackerData {
    val base = buildBase(groupId, bookTitle, author, category, image, tradeType, ExchangeRole.GUEST)
    return when (base.exchangeType) {
        ExchangeType.DELIVERY, ExchangeType.DIRECT ->
            buildRelayData(base, relayDetail?.partnerNickname, relayDetail?.hostProfileImage,
                relayDetail?.guestProfileImages, relayDetail?.trackerStatus, relayDetail?.stepDates)
        ExchangeType.NONE ->
            buildTogetherData(base, togetherDetail?.hostNickname, togetherDetail?.participantCount,
                togetherDetail?.myReadingRate, togetherDetail?.groupReadingRate, TrackerStatus.GUEST_READING)
    }
}

private fun buildBase(
    groupId: Long, bookTitle: String, author: String?, category: String?,
    image: String?, tradeType: String?, role: ExchangeRole
) = TrackerData(
    id = groupId,
    groupId = groupId,
    bookTitle = bookTitle,
    bookAuthor = author.orEmpty(),
    bookCategory = category,
    coverImageUrl = image,
    exchangeType = mapExchangeType(tradeType),
    role = role,
    withUserName = null,
    stepDates = emptyList(),
    currentStatus = TrackerStatus.UNKNOWN,
    hostProfileImageUrl = null,
    guestProfileImageUrl = null,
    myReadingRate = null,
    groupReadingRate = null
)

private fun buildRelayData(
    base: TrackerData,
    partnerNickname: String?,
    hostProfileImage: String?,
    guestProfileImages: List<String>?,
    trackerStatusStr: String?,
    rawStepDates: List<String?>?
): TrackerData {
    val stepDates = normalizeStepDates(rawStepDates)
    val status = trackerStatusStr?.let { TrackerStatus.from(it) } ?: calculateRelayStatus(stepDates)
    return base.copy(
        withUserName = partnerNickname,
        hostProfileImageUrl = hostProfileImage,
        guestProfileImageUrl = guestProfileImages?.firstOrNull(),
        stepDates = stepDates,
        currentStatus = status
    )
}

private fun buildTogetherData(
    base: TrackerData,
    hostNickname: String?,
    participantCount: Int?,
    myReadingRate: Int?,
    groupReadingRate: Int?,
    noneStatus: TrackerStatus
): TrackerData {
    val withText = buildString {
        if (!hostNickname.isNullOrBlank()) append(hostNickname)
        if (participantCount != null && participantCount > 0) {
            if (isNotEmpty()) append("  +$participantCount") else append("+$participantCount")
        }
    }.ifBlank { null }
    return base.copy(
        withUserName = withText,
        currentStatus = noneStatus,
        myReadingRate = myReadingRate?.coerceIn(0, 100),
        groupReadingRate = groupReadingRate?.coerceIn(0, 100)
    )
}

private fun normalizeStepDates(raw: List<String?>?): List<String?> =
    List(4) { idx -> raw.orEmpty().getOrNull(idx) }

private fun calculateRelayStatus(stepDates: List<String?>): TrackerStatus {
    return when (stepDates.indexOfLast { !it.isNullOrBlank() }) {
        0 -> TrackerStatus.HOST_READING
        1 -> TrackerStatus.SHIPPING_TO_GUEST
        2 -> TrackerStatus.GUEST_READING
        3 -> TrackerStatus.SHIPPING_TO_HOST
        else -> TrackerStatus.READY
    }
}

private fun mapExchangeType(typeString: String?): ExchangeType {
    return when (typeString?.uppercase()) {
        "DELIVERY", "SHIPPING" -> ExchangeType.DELIVERY
        "DIRECT" -> ExchangeType.DIRECT
        else -> ExchangeType.NONE
    }
}
