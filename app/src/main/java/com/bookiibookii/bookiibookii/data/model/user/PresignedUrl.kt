package com.bookiibookii.bookiibookii.data.model.user

data class PresignedUrlResult(
    val s3Key: String,
    val presignedPutUrl: String
)