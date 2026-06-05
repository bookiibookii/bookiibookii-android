package com.bookiibookii.bookiibookii.tracker.model

import com.bookiibookii.bookiibookii.data.model.library.BookResult

// 트래커 "독서카드 작성" → LibraryDetailFragment 진입에 필요한 책 식별/표시 정보.
// 트래커는 groupId만 제공, getLibraryBooks로 groupId에 해당하는 memberBookId 등을 해석
data class ReadingCardTarget(
    val groupId: Int,
    val memberBookId: Int,
    val groupName: String,
    val bookTitle: String,
    val author: String,
    val coverUrl: String,
    val startDate: String,
    val endDate: String,
    val rating: Double,
    val isDone: Boolean,
    val progressRate: Int,
)

// 서재 목록 응답 항목 → 진입 인자. isDone 판정은 서재와 동일
fun BookResult.toReadingCardTarget() = ReadingCardTarget(
    groupId = groupId,
    memberBookId = memberBookId,
    groupName = groupName,
    bookTitle = title,
    author = author,
    coverUrl = image.orEmpty(),
    startDate = startDate,
    endDate = endDate.orEmpty(),
    rating = rating,
    isDone = progressRate >= 100,
    progressRate = progressRate,
)
