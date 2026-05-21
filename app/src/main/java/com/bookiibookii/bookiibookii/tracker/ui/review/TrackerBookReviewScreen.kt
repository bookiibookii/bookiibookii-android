package com.bookiibookii.bookiibookii.tracker.ui.review

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.FooterButton
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private enum class StarState { Empty, SubPale, Sub }

// 별 인덱스(0~4)의 현재 시각 상태를 0~10 점수로부터 계산.
private fun starStateFor(score: Int, index: Int): StarState {
    val halfPos = (index + 1) * 2 - 1
    val fullPos = (index + 1) * 2
    return when {
        fullPos <= score -> StarState.Sub
        halfPos == score -> StarState.SubPale
        else -> StarState.Empty
    }
}

// 클릭 시 새 점수 계산.
// Empty   -> SubPale (앞 별 자동 Sub 채움)
// SubPale -> Sub
// Sub     -> SubPale (뒤 별 비워짐)
private fun nextScoreAfterClick(score: Int, index: Int): Int {
    val halfPos = (index + 1) * 2 - 1
    val fullPos = (index + 1) * 2
    return when {
        score < halfPos -> halfPos
        score == halfPos -> fullPos
        else -> halfPos
    }
}

@Composable
fun TrackerBookReviewScreen(
    onBackClick: () -> Unit = {},
    onSubmit: () -> Unit = {},
) {
    var rating by remember { mutableStateOf(0) }

    Scaffold(
        topBar = { TrackerBookReviewHeader(onBackClick = onBackClick) },
        bottomBar = {
            TrackerBookReviewFooter(
                enabled = rating > 0,
                onSubmit = onSubmit,
            )
        },
        containerColor = BookiiBookiiTheme.colors.uiBg,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            TrackerBookReviewCard(
                rating = rating,
                onRatingChange = { rating = it },
            )
        }
    }
}

@Composable
private fun TrackerBookReviewCard(
    rating: Int,
    onRatingChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round20,
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 122.dp, height = 180.dp)
                .background(
                    color = BookiiBookiiTheme.colors.grey200,
                    shape = BookiiBookiiTheme.shape.round10,
                ),
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) {
                    append("살인자의 기억법")
                }
                withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey900)) {
                    append("에 대한 평가를 남겨주세요!")
                }
            },
            style = BookiiBookiiTheme.typography.medium16,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(5) { i ->
                StarItem(
                    state = starStateFor(rating, i),
                    onClick = { onRatingChange(nextScoreAfterClick(rating, i)) },
                )
            }
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
                text = "감상평을 자유롭게 남겨주세요.",
                style = BookiiBookiiTheme.typography.medium16,
                color = BookiiBookiiTheme.colors.grey500,
            )
        }
    }
}

@Composable
private fun StarItem(
    state: StarState,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            StarState.Empty -> {
                Icon(
                    painter = painterResource(R.drawable.ic_star),
                    contentDescription = null,
                    tint = BookiiBookiiTheme.colors.grey300,
                    modifier = Modifier.size(40.dp),
                )
            }
            StarState.SubPale -> {
                Icon(
                    painter = painterResource(R.drawable.ic_star_fill),
                    contentDescription = null,
                    tint = BookiiBookiiTheme.colors.uiMainSubPale,
                    modifier = Modifier.size(40.dp),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_star),
                    contentDescription = null,
                    tint = BookiiBookiiTheme.colors.uiMainSub,
                    modifier = Modifier.size(40.dp),
                )
            }
            StarState.Sub -> {
                Icon(
                    painter = painterResource(R.drawable.ic_star_fill),
                    contentDescription = null,
                    tint = BookiiBookiiTheme.colors.uiMainSub,
                    modifier = Modifier.size(40.dp),
                )
            }
        }
    }
}

@Composable
private fun TrackerBookReviewHeader(
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
                text = "책 리뷰",
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
private fun TrackerBookReviewFooter(
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
private fun TrackerBookReviewScreenPreview() {
    BookiiPreview {
        TrackerBookReviewScreen()
    }
}
