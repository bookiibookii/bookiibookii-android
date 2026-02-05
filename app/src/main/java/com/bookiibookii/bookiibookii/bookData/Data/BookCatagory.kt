package com.bookiibookii.bookiibookii.bookData.Data

enum class BookCategory(
    val displayName: String,
    val serverValue: String
) {
    // 부키부키 앱 내부 분류 -> 추가 분류 필요 시 추가
    ECONOMY_MANAGEMENT("경제/경영", "ECONOMY"),
    TECHNOLOGY_SCIENCE("기술/과학", "TECH"),
    NOVEL("소설", "NOVEL"),
    POETRY("시", "POETRY"),
    ESSAY("에세이", "ESSAY"),
    ART_CULTURE("예술/문화", "ART"),
    HUMANITIES("인문학", "HUMANITIES"),
    SELF_HELP("자기계발", "SELF_HELP"),
    POLITICS_SOCIETY("정치/사회", "POLITICS"),
    ETC("기타", "ETC");

    companion object {
        fun fromDisplayName(name: String): BookCategory? {
            return BookCategory.entries.firstOrNull { it.displayName == name }
        }

        fun fromServerValue(value: String): BookCategory? {
            return BookCategory.entries.firstOrNull { it.serverValue == value }
        }
    }
}
