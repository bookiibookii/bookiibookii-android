package com.bookiibookii.bookiibookii.tracker.model

import com.bookiibookii.bookiibookii.data.model.tracker.BookInfo
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerListItemResDTO

// displayStatus -> 한글 라벨
private fun displayStatusToLabel(status: String?): String = when (status) {
    "READING" -> "읽는 중"
    "REVIEW_WRITING" -> "후기 작성"
    "REVIEW_WAITING_PARTNER" -> "후기 수정"
    "TRACKING_REQUIRED" -> "운송장 등록"
    "SHIPPING" -> "수령 전"
    "RETURN_TRACKING_REQUIRED" -> "수령 완료"
    "RETURNING" -> "수령 전"
    "MEETING_REQUIRED" -> "약속 등록"
    "EXCHANGING" -> "교환 진행"
    "EXCHANGE_REVIEW_WRITING" -> "후기 작성"
    else -> ""
}

// → 교환 전엔 각자 제 책을 읽어 둘 다 true, 1차 교환 후엔 서로 바꿔 읽어 둘 다 false.
// 화면의 "내 책" 배지는 '내가 소유한 책'을 가리켜야 하므로 교환 단계로 재계산
// - 교환 전: 내 소유 책 = 내가 읽는 책(myBook 쪽)
// - 교환 후: 내 소유 책 = 파트너가 읽는 책(partnerBook 쪽)
internal fun ownerBookBadges(myBook: BookInfo?, partnerBook: BookInfo?): Pair<Boolean, Boolean> {
    val exchanged = myBook?.isOwnerBook == false
    val mineIsOwner = myBook != null && !exchanged
    val partnerIsOwner = partnerBook != null && exchanged
    return mineIsOwner to partnerIsOwner
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
    val (leftIsOwner, rightIsOwner) = ownerBookBadges(myCurrentBook, partnerCurrentBook)
    return TrackerCardModel(
        groupId = groupId,
        groupName = groupName.orEmpty(),
        bookTitle = myCurrentBook?.title.orEmpty(),
        progressLabel = displayStatusToLabel(displayStatus),
        dDay = "D-${(remainingDays ?: 0).coerceAtLeast(0)}",
        left = myCurrentBook.toProfile().copy(isOwnerBook = leftIsOwner),
        right = partnerCurrentBook.toProfile().copy(isOwnerBook = rightIsOwner),
        primaryAction = primary,
        secondaryAction = secondary,
        isHost = myRole == "HOST",
    )
}
