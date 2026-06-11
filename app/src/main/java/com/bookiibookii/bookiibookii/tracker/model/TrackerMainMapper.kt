package com.bookiibookii.bookiibookii.tracker.model

import com.bookiibookii.bookiibookii.data.model.tracker.BookInfo
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerListItemResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerTopBannerResDTO

// 제목이 maxChars(공백 포함 글자수)를 넘으면 그만큼 자르고 "..." 부착
internal fun ellipsizeTitle(title: String, maxChars: Int): String =
    if (title.length > maxChars) title.take(maxChars) + "..." else title

// 상단 배너(topBanners)
fun TrackerTopBannerResDTO.toNotificationItem(): TrackerNotificationItem = TrackerNotificationItem(
    groupId = groupId ?: 0L,
    dDay = dDayLabel.orEmpty(),
    template = titleTemplate ?: title.orEmpty(),
    nickname = partnerNickname.orEmpty(),
    bookTitle = bookTitle.orEmpty(),
    remainingSeconds = remainingSeconds ?: 0L,
    subText = subtitle.orEmpty(),
)

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
        left = myCurrentBook.toProfile()
            .copy(progressLabelOverride = progressTextOverride(displayStatus, isMine = true)),
        right = partnerCurrentBook.toProfile()
            .copy(progressLabelOverride = progressTextOverride(displayStatus, isMine = false)),
        primaryAction = primary,
        secondaryAction = secondary,
        primaryEnabled = !isPrimaryActionDisabled(displayStatus),
        secondaryEnabled = !isSecondaryActionDisabled(displayStatus),
        showReadingProgress = !isReadingProgressHidden(displayStatus),
        isHost = myRole == "HOST",
    )
}
