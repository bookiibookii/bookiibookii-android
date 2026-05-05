package com.bookiibookii.bookiibookii.data.model.tracker

data class TrackerImagePresignedUrlResponse(
    val s3Key: String,
    val presignedPutUrl: String
)
