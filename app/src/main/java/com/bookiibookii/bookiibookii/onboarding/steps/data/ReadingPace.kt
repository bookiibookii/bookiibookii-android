package com.bookiibookii.bookiibookii.onboarding.steps.data

enum class ReadingPace(
    val badge: String,
    val displayName: String,
    val serverValue: String
) {
    FAST("약 3일", "앉은 자리에서 뚝딱!", "FAST"),
    NORMAL("약 1주", "매일매일 꾸준히", "NORMAL"),
    SLOW("약 1개월", "틈날 때마다 여유롭게", "SLOW"),
    UNKNOWN("", "아직 내 속도를 모르겠어요", "UNKNOWN");
}