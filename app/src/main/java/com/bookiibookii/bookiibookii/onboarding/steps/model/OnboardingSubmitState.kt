package com.bookiibookii.bookiibookii.onboarding.steps.model

sealed class OnboardingSubmitState {
    data object Idle : OnboardingSubmitState()
    data object Loading : OnboardingSubmitState()
    data object Success : OnboardingSubmitState()

    /**
     * 제출 실패. [message]는 사용자에게 토스트로 보여줄 실패 사유.
     * - 400/404 비즈니스 에러: 서버 응답 body의 사용자용 메시지
     */
    data class Error(val message: String) : OnboardingSubmitState()
}
