package com.bookiibookii.bookiibookii.data.model

// 공통 응답 (결과 데이터가 String이거나 없을 때 사용)
data class BaseResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String?
)
