package com.bookiibookii.bookiibookii.onboarding.steps.data

enum class ReadingPreference(
    val displayName: String,
    val serverValue: String
) {
    ECON_BIZ("경제/경영", "ECON_BIZ"),
    SCI_IT("과학/IT", "SCI_IT"),
    NOVEL_GENRE("소설/장르", "NOVEL_GENRE"),
    POEM_ESSAY("시/에세이", "POEM_ESSAY"),
    HOME_HOBBY("가정/취미", "HOME_HOBBY"),
    ART_CULTURE("예술/문화", "ART_CULTURE"),
    HUMAN_HISTORY("인문/역사", "HUMAN_HISTORY"),
    SELF_DEV("자기계발", "SELF_DEV"),
    POL_SOC("정치/사회", "POL_SOC"),
    ESC("기타", "ESC");
}