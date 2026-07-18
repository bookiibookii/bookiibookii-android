package com.bookiibookii.bookiibookii.onboarding.steps.model

sealed class NicknameCheckState {
    data object Idle : NicknameCheckState()
    data object Loading : NicknameCheckState()
    data class Available(val message: String) : NicknameCheckState()
    data class Duplicated(val message: String) : NicknameCheckState()
    data object NetworkError : NicknameCheckState()
    data object SystemError : NicknameCheckState()
}
