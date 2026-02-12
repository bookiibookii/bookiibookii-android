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
    val userId: Int,
    val onboardingDone: Boolean
)

//data class UserUpdateRequest(
//    val nickname: String,
//    val receiverName: String = "",
//    val phone: String = "",
//    val zipCode: String = "",
//    val address: String = "",
//    val addressDetail: String = "",
//    val meetPlace: String = ""
//)
//
//data class UserUpdateResponse(
//    val isSuccess: Boolean,
//    val code: String,
//    val message: String,
//    val result: String
//)

data class LogoutResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String
)

data class WithdrawResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String
)

data class TokenRefreshRequest(
    val refreshToken: String
)

data class TokenRefreshResult(
    val accessToken: String,
    val refreshToken: String,
    val userId: Int
)

data class TokenRefreshResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: TokenRefreshResult?
)