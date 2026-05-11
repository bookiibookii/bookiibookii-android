package com.bookiibookii.bookiibookii.data.model.group

data class BookSearchResponse(
    val books: List<BookItem>,
    val totalPage: Int,
    val totalResults: Int
)

data class BookItem(
    val title: String,
    val author: String,
    val image: String,
    val publisher: String,
    val isbn13: String,
    val category: String,
    val categoryLabel: String,
    val link: String
)
