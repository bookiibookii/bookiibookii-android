package com.bookiibookii.bookiibookii.data.model.user

data class OnboardingRequest(
    val name: String,
    val gender: String?,
    val birth: String?,
    val tags: List<String>,
    val s3Key: String?,
    val userBooks: List<OnboardingBook>,
    val introduction: String
)

data class OnboardingBook(
    val isbn13: String
)
