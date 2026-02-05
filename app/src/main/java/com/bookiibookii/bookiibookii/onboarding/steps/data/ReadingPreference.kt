package com.bookiibookii.bookiibookii.onboarding.steps.data

enum class ReadingPreference(
    val displayName: String,
    val serverValue: String
) {
    ECONOMY("경제/경영", "ECONOMY"),
    SCIENCE_IT("과학/IT", "SCIENCE_IT"),
    NOVEL_GENRE("소설/장르", "NOVEL_GENRE"),
    POEM_ESSAY("시/에세이", "POEM_ESSAY"),
    HOME_HOBBY("가정/취미", "HOME_HOBBY"),
    ART_CULTURE("예술/문화", "ART_CULTURE"),
    HUMANITIES_HISTORY("인문/역사", "HUMANITIES_HISTORY"),
    SELF_HELP("자기계발", "SELF_HELP"),
    POLITICS_SOCIETY("정치/사회", "POLITICS_SOCIETY"),
    ETC("기타", "ETC");
}