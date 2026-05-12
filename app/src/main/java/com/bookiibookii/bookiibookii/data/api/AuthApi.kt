package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.auth.LoginRequest
import com.bookiibookii.bookiibookii.data.model.auth.LoginResult
import com.bookiibookii.bookiibookii.data.model.auth.TokenRefreshRequest
import com.bookiibookii.bookiibookii.data.model.auth.TokenRefreshResult
import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    // 소셜 로그인
    @POST("api/auth/login")
    suspend fun postLogin(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResult>>

    // Access Token 재발급
    @POST("api/auth/refresh")
    suspend fun postRefresh(
        @Header("Authorization") authorization: String,
        @Body request: TokenRefreshRequest
    ): Response<ApiResponse<TokenRefreshResult>>

    // 로그아웃
    @POST("api/auth/logout")
    suspend fun logout(): Response<ApiResponse<String>>

    // 회원탈퇴
    @DELETE("api/auth/withdraw")
    suspend fun withdraw(): Response<ApiResponse<String>>
}
