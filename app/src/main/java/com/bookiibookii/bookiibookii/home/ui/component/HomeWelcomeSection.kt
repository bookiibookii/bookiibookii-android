package com.bookiibookii.bookiibookii.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import java.util.Calendar

// ─── 상단 인삿말 목록 ──────────────────────────────────────────────────────────
private val topGreetings = listOf("안녕하세요,", "반가워요,", "어서오세요,", "환영해요,")

// ─── 시간대별 하단 멘트 목록 ───────────────────────────────────────────────────
private val nightMessages = listOf(    // 22~05시
    "잠들기 전 독서 어때요?",
    "늦은 밤에도 책과 함께 해요",
)
private val morningMessages = listOf(  // 05~11시
    "오늘 하루도 책으로 활기차게 시작해요",
    "책 읽기 좋은 오전이네요",
)
private val afternoonMessages = listOf(// 11~17시
    "잠깐의 여유를 책과 함께 해요",
    "책과 함께 하는 편안한 오후 되세요",
)
private val eveningMessages = listOf(  // 17~22시
    "책 읽기 좋은 저녁이네요",
    "하루의 마무리로 책 한 권 어떠세요?",
)
private const val FALLBACK_MESSAGE = "함께 독서할 파트너를 찾아보세요"

private fun pickTopGreeting(): String = topGreetings.random()

private fun pickSubtitle(): String = try {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    when (hour) {
        in 22..23, in 0..4 -> nightMessages.random()
        in 5..10 -> morningMessages.random()
        in 11..16 -> afternoonMessages.random()
        in 17..21 -> eveningMessages.random()
        else -> FALLBACK_MESSAGE
    }
} catch (_: Exception) {
    FALLBACK_MESSAGE
}

@Composable
internal fun HomeWelcomeSection(
    nickname: String,
    modifier: Modifier = Modifier,
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography

    // 세션 동안 고정 — remember 키 없음: 탭 이동해도 recompose 시 값 유지
    val topGreeting = remember { pickTopGreeting() }
    val subtitle = remember { pickSubtitle() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.white)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
    ) {
        Text(
            text = buildAnnotatedString {
                append(topGreeting)
                append(" ")
                withStyle(SpanStyle(color = colors.uiMain)) { append(nickname) }
                append("\n")
                append(subtitle)
            },
            style = typography.regular24,
            color = colors.grey900,
        )
    }
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "HomeWelcomeSection")
@Composable
private fun HomeWelcomeSectionPreview() {
    BookiiPreview {
        HomeWelcomeSection(nickname = "sayo")
    }
}
