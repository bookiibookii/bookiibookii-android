package com.bookiibookii.bookiibookii.data.model.auth

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("socialType") val socialType: String,
    @SerializedName("token") val token: String
)

data class LoginResult(
    val accessToken: String,
    val refreshToken: String,
    val userId: Long,
    val onboardingStatus: String,
    val role: String
)
