package com.bookiibookii.bookiibookii.tracker.model

import com.bookiibookii.bookiibookii.data.model.tracker.BookInfo
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerListItemResDTO

// displayStatus -> 한글 라벨
private fun displayStatusToLabel(status: String?): String = when (status) {
    "READING" -> "읽는 중"
    "REVIEW_WRITING" -> "후기 작성"
    "TRACKING_REQUIRED" -> "운송장 등록"
    "SHIPPING" -> "수령 전"
    "RETURN_TRACKING_REQUIRED" -> "수령 완료"
    "RETURNING" -> "수령 전"
    "MEETING_REQUIRED" -> "약속 등록"
    "EXCHANGING" -> "교환 진행"
    "EXCHANGE_REVIEW_WRITING" -> "후기 작성"
    else -> ""
}

internal fun BookInfo?.toProfile(): TrackerProfileItem = TrackerProfileItem(
    nickname = this?.currentReaderNickname.orEmpty(),
    bookTitle = this?.title.orEmpty(),
    bookCoverUrl = this?.image,
    profileImageUrl = this?.currentReaderProfileImageUrl,
    progressPercent = this?.currentReadingRate ?: 0,
    isOwnerBook = this?.isOwnerBook ?: false,
    totalPages = this?.totalPages ?: 0,
)

fun TrackerListItemResDTO.toCardModel(): TrackerCardModel {
    val (primary, secondary) = actionsForStatus(displayStatus)
    return TrackerCardModel(
        groupId = groupId,
        groupName = groupName.orEmpty(),
        bookTitle = myCurrentBook?.title.orEmpty(),
        progressLabel = displayStatusToLabel(displayStatus),
        dDay = "D-${(remainingDays ?: 0).coerceAtLeast(0)}",
        left = myCurrentBook.toProfile(),
        right = partnerCurrentBook.toProfile(),
        primaryAction = primary,
        secondaryAction = secondary,
    )
}
