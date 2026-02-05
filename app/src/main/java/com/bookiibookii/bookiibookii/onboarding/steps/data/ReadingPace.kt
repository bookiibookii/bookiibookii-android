package com.bookiibookii.bookiibookii.onboarding.steps.data

enum class ReadingPace(
    val badge: String,
    val displayName: String,
    val serverValue: String
) {
    FAST("약 3일", "앉은 자리에서 뚝딱!", "3_7_DAYS"),
    WEEKLY("약 1주", "매일매일 꾸준히", "7_14_DAYS"),
    MONTHLY("약 1개월", "틈날 때마다 여유롭게", "14_30_DAYS"),
    UNKNOWN("", "아직 내 속도를 모르겠어요", "UNKNOWN");
}