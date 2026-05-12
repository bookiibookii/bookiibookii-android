package com.bookiibookii.bookiibookii.data.model.user

data class OnboardingRequest(
    val name: String,
    val tags: List<OnboardingTag>,
    val s3Key: String?
)

data class OnboardingTag(
    val type: String,
    val value: List<String>
)