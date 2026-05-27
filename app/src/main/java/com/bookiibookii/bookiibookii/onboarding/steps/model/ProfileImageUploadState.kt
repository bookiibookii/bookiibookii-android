package com.bookiibookii.bookiibookii.onboarding.steps.model

sealed class ProfileImageUploadState {
    data object Idle : ProfileImageUploadState()
    data object Loading : ProfileImageUploadState()
    data class Success(val s3Key: String) : ProfileImageUploadState()
    data class Error(val message: String) : ProfileImageUploadState()
}
