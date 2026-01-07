package com.bookiibookii.bookiibookii.bookData.Data

enum class BookCategory(
    val displayName: String
) {
    // 부키부키 앱 내부 분류 -> 추가 분류 필요 시 추가
    ECONOMY_MANAGEMENT("경제/경영"),
    TECHNOLOGY_SCIENCE("기술/과학"),
    NOVEL("소설"),
    POETRY("시"),
    ESSAY("에세이"),
    ART_CULTURE("예술/문화"),
    HUMANITIES("인문학"),
    SELF_HELP("자기계발"),
    POLITICS_SOCIETY("정치/사회"),
    ETC("기타");

    companion object {
        fun fromDisplayName(name: String): BookCategory? {
            return values().firstOrNull { it.displayName == name }
        }
    }
}
