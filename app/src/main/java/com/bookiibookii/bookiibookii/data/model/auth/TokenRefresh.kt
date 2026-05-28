package com.bookiibookii.bookiibookii.data.model.auth

data class TokenRefreshRequest(
    val refreshToken: String
)

data class TokenRefreshResult(
    val accessToken: String,
    val refreshToken: String,
    val userId: Long
)