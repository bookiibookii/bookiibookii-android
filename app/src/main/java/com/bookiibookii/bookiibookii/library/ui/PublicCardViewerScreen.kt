package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
fun PublicCardViewerScreen(
    card: ReadingCard,
    cardVersion: Int = 2,
    onGoMain: () -> Unit = {},
    onSaveImage: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    PublicViewerScaffold(
        onGoMain = onGoMain,
        onSaveImage = onSaveImage,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 320.dp)
                .fillMaxWidth(0.82f)
                .aspectRatio(320f / 520f)
                .shareableCardShadow()
                .clip(BookiiBookiiTheme.shape.round20),
        ) {
            ShareableCard(card = card, cardVersion = cardVersion, modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * 공유 링크로 연 화면 공통 틀 (Figma LIB-03-04): 로고 헤더 + 본문 + 하단 "메인으로 / 이미지 저장" 버튼.
 * 독서카드·프로필 공개 화면이 함께 쓴다.
 */
@Composable
internal fun PublicViewerScaffold(
    onGoMain: () -> Unit,
    onSaveImage: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = BookiiBookiiTheme.colors
    val background = if (isDark) colors.grey900 else colors.white
    val lineColor = if (isDark) colors.grey800 else colors.grey200

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            // ic_logo_wordmark는 좌우 여백 15씩 포함(234x22) → 실제 로고 204x22 (Figma와 동일)
            Icon(
                painter = painterResource(R.drawable.ic_logo_wordmark),
                contentDescription = null,
                tint = colors.uiMain,
                modifier = Modifier.height(22.dp),
            )
        }
        HorizontalDivider(color = lineColor, thickness = 1.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            content = content,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FooterButton(
                text = "메인으로",
                textColor = if (isDark) colors.white else colors.grey900,
                backgroundColor = background,
                borderColor = if (isDark) colors.grey700 else colors.grey200,
                onClick = onGoMain,
                modifier = Modifier.weight(1f),
            )
            FooterButton(
                text = "이미지 저장",
                textColor = colors.white,
                backgroundColor = colors.uiMain,
                borderColor = null,
                onClick = onSaveImage,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FooterButton(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    borderColor: Color?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(BookiiBookiiTheme.shape.round20)
            .background(backgroundColor)
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, BookiiBookiiTheme.shape.round20)
                else Modifier,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.medium16,
            color = textColor,
        )
    }
}

@Preview(name = "공개 뷰어 - 인용구", widthDp = 412, heightDp = 917, showBackground = true)
@Composable
private fun PublicCardViewerQuotePreview() {
    BookiiPreview {
        PublicCardViewerScreen(
            card = ReadingCard(
                username = "foryxxng",
                content = "책을 쓰지 않고 한 우물만 팠다면, 나는 그들이 원하는 자리에 앉아 행복했을까. 나는 오히려 우물을 나와서 많이 느낀다. 세상의 다양성을, 내가 보고 느낄 수 있는 것들의 가치를.",
                page = "123",
                type = ReadingCardType.QUOTE,
                bookTitle = "나는 당신을 편애합니다",
                quotation = "새는 알에서 나오려고 싸운다. 알은 세상이다. 태어나려는 자는 한 세계를 파괴해야 한다.",
            ),
        )
    }
}

@Preview(name = "공개 뷰어 - 이미지", widthDp = 412, heightDp = 917, showBackground = true)
@Composable
private fun PublicCardViewerPhotoPreview() {
    BookiiPreview {
        PublicCardViewerScreen(
            card = ReadingCard(
                username = "foryxxng",
                content = "이 장면이 특히 인상 깊었어요.",
                page = "45",
                type = ReadingCardType.PHOTO,
                bookTitle = "나는 당신을 편애합니다",
            ),
        )
    }
}
