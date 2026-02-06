package com.bookiibookii.bookiibookii.data.model

// 1) 닉네임 중복 확인
data class NicknameValidationResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: NicknameValidationResult?
)

data class NicknameValidationResult(
    val isAvailable: Boolean
)

// 2) Presigned URL
data class PresignedUrlResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: PresignedUrlResult?
)

data class PresignedUrlResult(
    val s3Key: String,
    val presignedPutUrl: String
)