package com.bookiibookii.bookiibookii.tracker.model

import com.bookiibookii.bookiibookii.data.model.tracker.BookInfo
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerListItemResDTO

// 제목이 maxChars(공백 포함 글자수)를 넘으면 그만큼 자르고 "..." 부착
internal fun ellipsizeTitle(title: String, maxChars: Int): String =
    if (title.length > maxChars) title.take(maxChars) + "..." else title

// displayStatus -> 한글 라벨
private fun displayStatusToLabel(status: String?): String = when (status) {
    "READING" -> "읽는 중"
    "REVIEW_WRITING" -> "후기 작성"
    "REVIEW_WAITING_PARTNER" -> "후기 수정"
    "TRACKING_REQUIRED" -> "운송장 등록"
    "SHIPPING" -> "수령 전"
    "RETURN_TRACKING_REQUIRED" -> "수령 완료"
    "RETURNING" -> "수령 전"
    "MEETING_REGISTER_REQUIRED" -> "약속 등록"
    "WAITING_HOST_MEETING_REGISTER" -> "약속 등록"
    "EXCHANGING" -> "교환 진행"
    "WAITING_PARTNER_MEETING_COMPLETE" -> "교환 완료"
    "EXCHANGE_REVIEW_WRITING" -> "후기 작성"
    else -> ""
}

internal fun BookInfo?.toProfile(): TrackerProfileItem = TrackerProfileItem(
    nickname = this?.currentReaderNickname.orEmpty(),
    bookTitle = this?.title.orEmpty(),
    bookCoverUrl = this?.image,
    profileImageUrl = this?.currentReaderProfileImageUrl,
    progressPercent = this?.currentReadingRate ?: 0,
    isOwnerBook = this?.isMyOriginalBook ?: false,
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
        primaryEnabled = !isPrimaryActionDisabled(displayStatus),
        secondaryEnabled = !isSecondaryActionDisabled(displayStatus),
        showReadingProgress = !isReadingProgressHidden(displayStatus),
        isHost = myRole == "HOST",
    )
}
