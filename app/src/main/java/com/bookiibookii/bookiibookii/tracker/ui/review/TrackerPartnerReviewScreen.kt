package com.bookiibookii.bookiibookii.tracker.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.FooterButton
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
fun TrackerPartnerReviewScreen(
    onBackClick: () -> Unit = {},
    onSubmit: () -> Unit = {},
) {
    Scaffold(
        topBar = { TrackerPartnerReviewHeader(onBackClick = onBackClick) },
        bottomBar = {
            TrackerPartnerReviewFooter(
                enabled = false,
                onSubmit = onSubmit,
            )
        },
        containerColor = BookiiBookiiTheme.colors.uiBg,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TrackerCard()
            ReviewCard(partnerNickname = "noshel")
        }
    }
}

private enum class PartnerRating { NONE, GOOD, BAD }

@Composable
private fun RatingButton(
    text: String,
    iconRes: Int,
    isSelected: Boolean,
    isPositive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = BookiiBookiiTheme.shape.round20
    val bgColor = if (isSelected) {
        BookiiBookiiTheme.colors.uiMainPale
    } else {
        BookiiBookiiTheme.colors.white
    }
    val borderColor = if (isSelected) {
        BookiiBookiiTheme.colors.uiMain150
    } else {
        BookiiBookiiTheme.colors.grey200
    }
    val contentColor = when {
        isSelected -> BookiiBookiiTheme.colors.uiMain
        isPositive -> BookiiBookiiTheme.colors.grey900
        else -> BookiiBookiiTheme.colors.grey500
    }
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.regular16,
            color = contentColor,
        )
    }
}

@Composable
private fun ReviewCard(
    partnerNickname: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round20,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // 파트너 프로필 — VM 연결 시 imageUrl 와이어링
            ProfilePlaceholder(modifier = Modifier.size(20.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) {
                        append(partnerNickname)
                    }
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey900)) {
                        append("님과의 교환독서는 어떠셨나요?")
                    }
                },
                style = BookiiBookiiTheme.typography.medium16,
            )
        }
        var rating by remember { mutableStateOf(PartnerRating.NONE) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RatingButton(
                text = "좋았어요",
                iconRes = R.drawable.ic_hand_thumbs_up,
                isSelected = rating == PartnerRating.GOOD,
                isPositive = true,
                onClick = { rating = PartnerRating.GOOD },
                modifier = Modifier.weight(1f),
            )
            RatingButton(
                text = "별로였어요",
                iconRes = R.drawable.ic_hand_thumbs_down,
                isSelected = rating == PartnerRating.BAD,
                isPositive = false,
                onClick = { rating = PartnerRating.BAD },
                modifier = Modifier.weight(1f),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = BookiiBookiiTheme.colors.grey100,
                    shape = BookiiBookiiTheme.shape.round20,
                )
                .padding(20.dp),
            contentAlignment = Alignment.TopStart,
        ) {
            Text(
                text = "후기를 자유롭게 남겨주세요.",
                style = BookiiBookiiTheme.typography.regular16,
                color = BookiiBookiiTheme.colors.grey500,
            )
        }
    }
}

@Composable
private fun TrackerCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round20,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        ) {
            Text(
                text = "김영하 도장깨기 하실 분",
                style = BookiiBookiiTheme.typography.medium16,
                color = BookiiBookiiTheme.colors.grey800,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(0.8.dp)
                    .background(BookiiBookiiTheme.colors.grey100),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookColumn(
                nickname = "나",
                bookTitle = "살인자의 기억법",
                isMyBook = true,
                modifier = Modifier.weight(1f),
            )
            BookColumn(
                nickname = "noshel",
                bookTitle = "작별인사",
                isMyBook = false,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BookColumn(
    nickname: String,
    bookTitle: String,
    isMyBook: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(width = 100.dp, height = 132.dp)
                    .background(
                        color = BookiiBookiiTheme.colors.grey200,
                        shape = BookiiBookiiTheme.shape.round8,
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.BottomEnd,
            ) {
                if (isMyBook) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xBFFEFEFE),
                                shape = BookiiBookiiTheme.shape.round4,
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "내 책",
                            style = BookiiBookiiTheme.typography.regular10,
                            color = BookiiBookiiTheme.colors.grey900,
                        )
                    }
                }
            }
            // TrackerCard 위 파트너 오버레이 — BookCover와 겹치므로 innerStroke=true. URL은 VM 연결 후속
            ProfilePlaceholder(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 17.dp)
                    .size(44.dp),
                innerStroke = true,
            )
        }
        Text(
            text = nickname,
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey700,
        )
        Text(
            text = bookTitle,
            style = BookiiBookiiTheme.typography.medium16,
            color = BookiiBookiiTheme.colors.grey800,
        )
    }
}

@Composable
private fun TrackerPartnerReviewHeader(
    onBackClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .background(BookiiBookiiTheme.colors.white)
                .padding(horizontal = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "뒤로",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = "교환독서 후기",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BookiiBookiiTheme.colors.grey200),
        )
    }
}

@Composable
private fun TrackerPartnerReviewFooter(
    enabled: Boolean,
    onSubmit: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        FooterButton(
            text = "등록하기",
            onClick = onSubmit,
            enabled = enabled,
        )
    }
}

@Preview(widthDp = 412, heightDp = 900, showBackground = true)
@Composable
private fun TrackerPartnerReviewScreenPreview() {
    BookiiPreview {
        TrackerPartnerReviewScreen()
    }
}
