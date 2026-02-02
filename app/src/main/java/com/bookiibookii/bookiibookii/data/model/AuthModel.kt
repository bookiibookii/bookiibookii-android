package com.bookiibookii.bookiibookii.data.model

import com.google.gson.annotations.SerializedName

// 로그인 요청 데이터
data class LoginRequest(
    @SerializedName("socialType")
    val socialType: String,

    @SerializedName("token")
    val token: String
)

// 로그인 응답 데이터
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