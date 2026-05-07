package com.bookiibookii.bookiibookii.data.model.user

data class NicknameValidationResult(
    val isAvailable: Boolean,
    val code: String,
    val message: String
)
