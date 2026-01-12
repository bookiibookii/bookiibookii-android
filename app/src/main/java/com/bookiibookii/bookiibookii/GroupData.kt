package com.bookiibookii.bookiibookii

data class GroupData(
    val coverImgUrl: String,     // String(URL)
    val date: String,
    val status: String,
    val profileImgUrl: String,   // String (URL)
    val nickname: String,
    val bookTitle: String,
    val bookAuthor: String,
    val memberCount: String,
    val deadline: String,
    val tags: List<String>
)