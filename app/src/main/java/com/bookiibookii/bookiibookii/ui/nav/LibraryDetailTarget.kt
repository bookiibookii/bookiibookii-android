package com.bookiibookii.bookiibookii.ui.nav

import com.bookiibookii.bookiibookii.data.model.library.BookResult
import com.bookiibookii.bookiibookii.data.model.mypage.CompletedBook

// 서재 상세 진입 인자. 트래커·마이페이지가 공유하므로 도메인 중립 위치에 둔다.
// 필드는 LibraryDestinations.detail(...) 파라미터와 1:1 대응.
data class LibraryDetailTarget(
    val groupId: Int,
    val memberBookId: Int,
    val groupName: String,
    val bookTitle: String,
    val author: String,
    val genre: String,
    val coverUrl: String,
    val startDate: String,
    val endDate: String,
    val completedAt: String,
    val rating: Double,
    val isDone: Boolean,
    val progressRate: Int,
    val totalPages: Int?,
)

// 서재 목록 응답 항목 → 진입 인자. isDone 판정은 서재와 동일
fun BookResult.toLibraryDetailTarget() = LibraryDetailTarget(
    groupId = groupId,
    memberBookId = memberBookId,
    groupName = groupName,
    bookTitle = title,
    author = author,
    genre = genre.orEmpty(),
    coverUrl = image.orEmpty(),
    startDate = startDate.orEmpty(),
    endDate = endDate.orEmpty(),
    completedAt = completedAt.orEmpty(),
    rating = rating,
    isDone = progressRate >= 100,
    progressRate = progressRate,
    totalPages = totalPages,
)

// 마이페이지 책장 항목 → 진입 인자. 통합 전 MypageFragment가 하던 매핑.
// 완독 책장이라 isDone은 항상 true, 나머지 미제공 필드는 기본값.
fun CompletedBook.toLibraryDetailTarget() = LibraryDetailTarget(
    groupId = groupId.toInt(),
    memberBookId = memberBookId.toInt(),
    groupName = "",
    bookTitle = title,
    author = author.orEmpty(),
    genre = category?.trim('(', ')').orEmpty(),
    coverUrl = image.orEmpty(),
    startDate = "",
    endDate = "",
    completedAt = completedAt.orEmpty(),
    rating = rating,
    isDone = true,
    progressRate = 0,
    totalPages = 0,
)
