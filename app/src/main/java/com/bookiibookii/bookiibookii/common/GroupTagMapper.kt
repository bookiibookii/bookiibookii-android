package com.bookiibookii.bookiibookii.common

object GroupTagMapper {
    fun toKoreanTag(tag: String): String {
        return when(tag) {
            // --- 이미지 1: METHOD (방식) ---
            "MEMO" -> "#메모환영"
            "POSTIT" -> "#포스트잇"
            "CLEAN" -> "#깔끔하게"

            // --- 이미지 1: VIBE (분위기) ---
            "SERIOUS" -> "#진지함"
            "LIGHT_FUN" -> "#재미있게"
            "INSIGHT" -> "#인사이트"

            // --- 이미지 1: SPEED (속도) ---
            "FAST" -> "#약 3일"
            "NORMAL" -> "#약 1주"
            "SLOW" -> "#약 1개월"
            "UNKNOWN" -> "#속도모름"

            // --- 이미지 1: GENRE (장르) ---
            "ECON_BIZ" -> "#경제/경영"
            "SCI_IT" -> "#과학/IT"
            "NOVEL_GENRE" -> "#소설/장르"
            "POEM_ESSAY" -> "#시/에세이"
            "HOME_HOBBY" -> "#가정/취미"
            "ART_CULTURE" -> "#예술/문화"
            "HUMAN_HISTORY" -> "#인문/역사"
            "SELF_DEV" -> "#자기계발"
            "POL_SOC" -> "#정치/사회"
            "ESC" -> "#기타"

            // --- 책장 카테고리 (백엔드 실제 enum 값) ---
            "ALL" -> "#전체"
            "LITERATURE_ALL" -> "#문학"
            "NON_LITERATURE_ALL" -> "#비문학"
            // 문학
            "KOREAN_NOVEL" -> "#한국소설"
            "WORLD_NOVEL" -> "#세계소설"
            "GENRE_NOVEL" -> "#장르소설"
            "ROMANCE" -> "#로맨스"
            "HISTORICAL_NOVEL" -> "#역사소설"
            "POETRY_ESSAY" -> "#시/에세이"
            "PLAY_LITERATURE" -> "#희곡/문학"
            "LITERATURE_ETC" -> "#기타"
            // 비문학
            "ECONOMY_BUSINESS" -> "#경제/경영"
            "SCIENCE_IT" -> "#과학/IT"
            "HUMANITIES_HISTORY" -> "#인문/역사"
            "SELF_DEVELOPMENT" -> "#자기계발"
            "POLITICS_SOCIETY" -> "#정치/사회"
            "NON_LITERATURE_ETC" -> "#기타"

            // --- 이미지 2: REVIEW (사용자 리뷰 태그) ---
            "KINDNESS" -> "#친절매너"
            "GOOD_HANDWRITING" -> "#예쁜글씨"
            "SWEET_COMMENT" -> "#다정한코멘트"
            "INSIGHTFUL" -> "#인사이트넘침"
            "FAST_SHIPPING" -> "#빠른배송"
            "FUNNY" -> "#재미있는코멘트"
            "CLEAN_CONDITION" -> "#깔끔한상태"

            // 매칭되는 게 없을 경우 (사용자가 직접 입력한 커스텀 태그 등)
            else -> if (tag.startsWith("#")) tag else "#$tag"
        }
    }
}