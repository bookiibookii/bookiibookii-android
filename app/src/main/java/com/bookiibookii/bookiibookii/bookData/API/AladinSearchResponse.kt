package com.bookiibookii.bookiibookii.bookData.API

import com.bookiibookii.bookiibookii.bookData.Data.Book

data class AladinSearchResponse(
    val version: String,
    val title: String,
    val totalResults: Int,
    val item: List<Book>?   //nullable
)

