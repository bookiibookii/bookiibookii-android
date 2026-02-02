package com.bookiibookii.bookiibookii.data.model

// 로그인 요청
data class LoginRequest(
    val socialType: String, // "GOOGLE" or "KAKAO"
    val token: String
)

// 로그인 응답 (성공 시)
data class LoginResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: LoginResult?
)

data class LoginResult(
    val accessToken: String,
    val refreshToken: String,
    val userId: Int
)