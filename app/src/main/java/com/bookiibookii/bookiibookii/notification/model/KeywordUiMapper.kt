package com.bookiibookii.bookiibookii.notification.model

import com.bookiibookii.bookiibookii.data.model.keyword.KeywordItem

// 서버 키워드 DTO → 키워드 설정 UI 모델
fun KeywordItem.toUiModel(): KeywordUiModel = KeywordUiModel(
    id = keywordId,
    keyword = content,
)
