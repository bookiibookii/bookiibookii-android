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

private fun BookInfo?.toProfile(): TrackerProfileItem = TrackerProfileItem(
    nickname = this?.currentReaderNickname.orEmpty(),
    bookTitle = this?.title.orEmpty(),
    bookCoverUrl = this?.image,
    profileImageUrl = this?.currentReaderProfileImageUrl,
    progressPercent = this?.currentReadingRate ?: 0,
    isOwnerBook = this?.isOwnerBook ?: false,
)

fun TrackerListItemResDTO.toCardModel(): TrackerCardModel = TrackerCardModel(
    groupId = groupId,
    groupName = groupName.orEmpty(),
    bookTitle = myCurrentBook?.title.orEmpty(),
    progressLabel = displayStatusToLabel(displayStatus),
    dDay = "D-${(remainingDays ?: 0).coerceAtLeast(0)}",
    left = myCurrentBook.toProfile(),
    right = partnerCurrentBook.toProfile(),
    primaryActionLabel = "진행률 기록",
    secondaryActionLabel = "독서카드 작성",
)

// Count board 카운트 규칙
// 전체   = items.size
// 읽는 중 = READING
// 후기   = REVIEW_WRITING, EXCHANGE_REVIEW_WRITING
// 교환 중 = 나머지 전부
data class TrackerCounts(
    val total: Int,
    val reading: Int,
    val exchanging: Int,
    val review: Int,
)

fun List<TrackerListItemResDTO>.toCounts(): TrackerCounts {
    val reading = count { it.displayStatus == "READING" }
    val review = count { it.displayStatus == "REVIEW_WRITING" || it.displayStatus == "EXCHANGE_REVIEW_WRITING" }
    return TrackerCounts(
        total = size,
        reading = reading,
        review = review,
        exchanging = size - reading - review,
    )
}
