package com.bookiibookii.bookiibookii.notification.model

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

// 알림 센터 탭 — 시스템 알림 / 키워드 알림
enum class NotificationTab { SYSTEM, KEYWORD }

// 알림 카드 좌측 아이콘 스타일 — 배경/아이콘 색 분기
enum class NotificationIconStyle { HIGHLIGHT, NORMAL }

// 키워드 알림 설정 — 등록된 키워드 1건 (UI 표시용 모델)
data class KeywordUiModel(
    val id: Long,
    val keyword: String,
)

// 알림 카드 1건 (UI 표시용 모델)
data class NotificationUiModel(
    val id: Long,
    val title: String,
    val body: AnnotatedString,
    val timeText: String,
    val bookTitle: String,
    val isUnread: Boolean,
    val iconStyle: NotificationIconStyle,
)

// 본문 빌더
fun notificationBody(
    nickname: String,
    bookTitle: String,
    tail: String,
): AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(nickname) }
    append(" 님이 ")
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(bookTitle) }
    append(tail)
}
