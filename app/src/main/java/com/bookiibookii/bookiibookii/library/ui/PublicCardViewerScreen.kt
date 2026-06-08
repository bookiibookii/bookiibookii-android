package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 공유 토큰으로 진입하는 공개 독서카드 뷰어 (로그인 불필요, stateless)
// - 카드 비주얼은 기존 ShareableCard 재사용 → PHOTO/QUOTE 타입별 디자인이 그대로 분기됨
// - 상단 로고 / 가운데 카드 / 책 정보 / 하단 CTA(앱에서 보기)
@Composable
fun PublicCardViewerScreen(
    card: ReadingCard,
    bookAuthor: String = "",
    onClose: () -> Unit = {},
    onOpenApp: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg)
            .statusBarsPadding(),
    ) {
        // 상단 바 — 닫기 + 가운데 로고
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = "닫기",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(24.dp),
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_bookii_text),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(14.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 카드 — 공유용과 동일 비율(348:464), 타입별 디자인은 ShareableCard가 분기
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(348f / 464f),
            ) {
                ShareableCard(card = card, modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 책 제목
            Text(
                text = card.bookTitle,
                style = BookiiBookiiTheme.typography.semibold18,
                color = BookiiBookiiTheme.colors.grey900,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            // 저자
            if (bookAuthor.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bookAuthor,
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey500,
                    textAlign = TextAlign.Center,
                )
            }
            // 작성자
            if (card.username.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${card.username} 님의 독서카드",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey600,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 하단 CTA — 앱에서 보기
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .clip(BookiiBookiiTheme.shape.round16)
                .background(BookiiBookiiTheme.colors.uiMain)
                .clickable(onClick = onOpenApp)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "부키부키 앱에서 보기",
                style = BookiiBookiiTheme.typography.medium16,
                color = BookiiBookiiTheme.colors.white,
            )
        }
    }
}

@Preview(name = "공개 뷰어 - 인용구", widthDp = 412, heightDp = 917, showBackground = true)
@Composable
private fun PublicCardViewerQuotePreview() {
    BookiiPreview {
        PublicCardViewerScreen(
            card = ReadingCard(
                username = "북이",
                content = "다시 읽어도 마음에 오래 남는 문장이었다.",
                page = "123",
                type = ReadingCardType.QUOTE,
                bookTitle = "데미안",
                quotation = "새는 알에서 나오려고 투쟁한다.",
            ),
            bookAuthor = "헤르만 헤세",
        )
    }
}

@Preview(name = "공개 뷰어 - 이미지", widthDp = 412, heightDp = 917, showBackground = true)
@Composable
private fun PublicCardViewerPhotoPreview() {
    BookiiPreview {
        PublicCardViewerScreen(
            card = ReadingCard(
                username = "부키",
                content = "이 장면이 특히 인상 깊었어요.",
                page = "45",
                type = ReadingCardType.PHOTO,
                bookTitle = "어린 왕자",
            ),
            bookAuthor = "생텍쥐페리",
        )
    }
}
