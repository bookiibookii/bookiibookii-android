// GroupTagMapper.kt (또는 아무 파일 최상단/최하단)
package com.bookiibookii.bookiibookii.common

import android.view.View
import com.google.android.material.chip.Chip

object GroupTagMapper {
    fun toKoreanTag(tag: String): String {
        return when(tag) {
            "MEMO" -> "#메모환영"
            "POSTIT" -> "#포스트잇"
            "CLEAN" -> "#깨끗하게"
            "SERIOUS" -> "#진지하게"
            "LIGHT_FUN" -> "#재미있게"
            "INSIGHT" -> "#인사이트"
            "DISCUSSION" -> "#토론선호"
            "QUIET" -> "#조용히"
            else -> "#$tag"
        }
    }

    // 칩 리스트와 태그 데이터를 받아서 화면에 표시해주는 헬퍼 함수
    fun bindTags(chipList: List<Chip>, tags: List<String>?) {
        // 1. 일단 다 숨김
        chipList.forEach { it.visibility = View.GONE }

        // 2. 데이터 있는 만큼만 켜고 글자 넣기
        tags?.forEachIndexed { index, tagCode ->
            if (index < chipList.size) {
                chipList[index].text = toKoreanTag(tagCode)
                chipList[index].visibility = View.VISIBLE
            }
        }
    }
}