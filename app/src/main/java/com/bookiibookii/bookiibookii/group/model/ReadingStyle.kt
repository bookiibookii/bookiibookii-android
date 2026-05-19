package com.bookiibookii.bookiibookii.group.model

import com.bookiibookii.bookiibookii.R

// 그룹 규칙 프리셋
enum class ReadingStyle(
    val label: String,
    val iconRes: Int,
    val apiTag: String,
) {
    COMMENT("책에 직접 코멘트를 남겨요!", R.drawable.ic_edit, "MEMO"),
    POSTIT("직접 메모 대신 포스트잇이나 인덱스를 활용해요!", R.drawable.ic_bookmark, "POSTIT"),
    PHOTO("직접 메모 대신 사진으로 기록해요!", R.drawable.ic_camera, "PHOTO"),
    ANY("어떤 방식이든 좋아요!", R.drawable.ic_book, "ALL_ROUNDER"),
}
