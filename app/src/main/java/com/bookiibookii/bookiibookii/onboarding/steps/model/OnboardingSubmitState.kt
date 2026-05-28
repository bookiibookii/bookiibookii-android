package com.bookiibookii.bookiibookii.onboarding.steps.model

sealed class OnboardingSubmitState {
    data object Idle : OnboardingSubmitState()
    data object Loading : OnboardingSubmitState()
    data object Success : OnboardingSubmitState()
    data class Error(val message: String) : OnboardingSubmitState()
}
