package com.bookiibookii.bookiibookii.trkData.dto

data class LibraryBooksResponseDto(
    val groupId: Long,
    val userBookId: Long,
    val bookId: Long,

    val title: String,
    val author: String,

    val image: String?,               // null 가능
    val hostId: Long,
    val hostNickName: String?,
    val hostProfileImageUrl: String?,

    val groupType: String,            // "TOGETHER" / "RELAY"
    val groupStatus: String,          // "RECRUITING" 등

    val startDate: String,            // "YYYY-MM-DD"
    val endDate: String?,             // null 가능(스펙에선 string이지만 실데이터 nullable일 수 있음)
    val duration: Int,

    val rating: Double,
    val comment: String?
)
