package com.bookiibookii.bookiibookii.common

import android.view.View
import com.google.android.material.chip.Chip

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

    /**
     * 칩 리스트와 태그 데이터를 받아서 화면에 표시해주는 헬퍼 함수
     */
    fun bindTags(chipList: List<Chip>, tags: List<String>?) {
        // 1. 모든 칩 초기화 (숨김)
        chipList.forEach { it.visibility = View.GONE }

        // 2. 데이터가 있는 만큼만 변환하여 표시
        tags?.forEachIndexed { index, tagCode ->
            if (index < chipList.size) {
                chipList[index].text = toKoreanTag(tagCode)
                chipList[index].visibility = View.VISIBLE
            }
        }
    }
}