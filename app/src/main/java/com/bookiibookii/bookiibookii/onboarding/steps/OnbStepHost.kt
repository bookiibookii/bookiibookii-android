package com.bookiibookii.bookiibookii.onboarding.steps

interface OnbStepHost {

    /**
     * 하단 CTA 버튼 활성/비활성 제어
     * Fragment → Activity 호출용
     */
    fun setNextEnabled(enabled: Boolean)
}