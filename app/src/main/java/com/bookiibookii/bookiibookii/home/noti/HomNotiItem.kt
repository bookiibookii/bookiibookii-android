package com.bookiibookii.bookiibookii.home.noti

data class HomNotiItem(
    val title: String,
    val body: String,
    val timeText: String,
    val bookTitle: String = "",
    var isUnread: Boolean = false
)