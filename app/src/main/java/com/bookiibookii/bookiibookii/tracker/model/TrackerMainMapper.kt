package com.bookiibookii.bookiibookii.tracker.model

import com.bookiibookii.bookiibookii.data.model.tracker.BookInfo
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerListItemResDTO

// 제목이 maxChars(공백 포함 글자수)를 넘으면 그만큼 자르고 "..." 부착
internal fun ellipsizeTitle(title: String, maxChars: Int): String =
    if (title.length > maxChars) title.take(maxChars) + "..." else title

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
        displayBookTitle = displayBookTitle.orEmpty(),
        bookTitle = myCurrentBook?.title.orEmpty(),
        progressLabel = displayStatusLabel.orEmpty(),
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
