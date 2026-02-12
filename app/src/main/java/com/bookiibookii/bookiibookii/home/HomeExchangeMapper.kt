package com.bookiibookii.bookiibookii.home

import com.bookiibookii.bookiibookii.trkData.dto.GuestTrackerListItemDto
import com.bookiibookii.bookiibookii.trkData.dto.HostTrackerListItemDto

fun GuestTrackerListItemDto.toHomeExchangeItem(): HomeExchangeItem {
    val withName = relayDetail?.partnerNickname ?: togetherDetail?.hostNickname ?: ""

    val profileUrl = relayDetail?.hostProfileImage
        ?: relayDetail?.guestProfileImages?.firstOrNull()

    return HomeExchangeItem(
        groupId = groupId,
        bookTitle = bookTitle,
        author = author,
        image = image,
        withNickname = withName,
        profileUrl = profileUrl,
        trackerStatus = relayDetail?.trackerStatus,
        stepDates = relayDetail?.stepDates,
        role = ExchangeRole.GUEST
    )
}

fun HostTrackerListItemDto.toHomeExchangeItem(): HomeExchangeItem {
    val withName = relayDetail?.partnerNickname ?: togetherDetail?.hostNickname ?: ""

    val profileUrl = relayDetail?.guestProfileImages?.firstOrNull()
        ?: relayDetail?.hostProfileImage

    return HomeExchangeItem(
        groupId = groupId,
        bookTitle = bookTitle,
        author = author,
        image = image,
        withNickname = withName,
        profileUrl = profileUrl,
        trackerStatus = relayDetail?.trackerStatus,
        stepDates = relayDetail?.stepDates,
        role = ExchangeRole.HOST
    )
}