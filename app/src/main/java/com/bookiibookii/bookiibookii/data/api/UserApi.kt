package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.user.NicknameValidationResult
import com.bookiibookii.bookiibookii.data.model.user.OnboardingRequest
import com.bookiibookii.bookiibookii.data.model.user.OtherProfileResult
import com.bookiibookii.bookiibookii.data.model.user.PresignedUrlResult
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface UserApi {

    // 온보딩
    @POST("/api/onboarding")
    suspend fun postOnboarding(
        @Body body: OnboardingRequest
    ): Response<ApiResponse<String>>

    // 닉네임 검증
    @POST("api/users/name-validation")
    suspend fun postNicknameValidation(
        @Query("nickname") nickname: String
    ): Response<ApiResponse<NicknameValidationResult>>

    // 사용자 이미지 업로드용 Presigned URL 발급
    @POST("api/users/me/image/presigned-url")
    suspend fun postPresignedUrl(): Response<ApiResponse<PresignedUrlResult>>

    // S3 이미지 업로드
    @PUT
    suspend fun uploadImageToS3(
        @Url url: String,
        @Body image: RequestBody
    ): Response<Unit>

    // 타 유저 프로필 조회
    @GET("/api/profiles/{nickname}")
    suspend fun getUserProfile(
        @Path("nickname") nickname: String
    ): Response<ApiResponse<OtherProfileResult>>
}
