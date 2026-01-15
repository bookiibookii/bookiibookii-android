package com.bookiibookii.bookiibookii.bookData.Data

data class LibBook(
    val id: Long = 0,
    val title: String,
    val author: String,
    val coverUrl: String? = null,

    val readStatus: ReadStatus,
    val progress: String? = null,
    val rating: Int? = null
)

enum class ReadStatus {
    READING, // 진행 중
    DONE     // 종료
}