package com.bookiibookii.bookiibookii.data.model

data class CommonResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: T?
)