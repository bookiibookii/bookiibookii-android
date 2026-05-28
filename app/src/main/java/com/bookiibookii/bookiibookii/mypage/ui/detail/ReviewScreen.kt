package com.bookiibookii.bookiibookii.mypage.ui.detail

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.ExchangeTypeChip
import com.bookiibookii.bookiibookii.ui.component.ReviewTypeChip

enum class ReviewTab { WRITTEN, RECEIVED }

private data class BookReviewItem(
    val title: String,
    val author: String,
    val rating: Int,
    val isDelivery: Boolean,
    val comment: String,
    val date: String,
)

private data class ReceivedReviewItem(
    val nickname: String,
    val isGood: Boolean,
    val comment: String,
    val date: String,
)

private val mockBookReviews = listOf(
    BookReviewItem("채식주의자", "한강", 5, true, "폭력과 욕망, 인간의 원초적 본성에 대한 날카로운 탐구...", "2026. 04. 05."),
    BookReviewItem("프로젝트 헤일메리", "앤디 위어", 4, false, "영화 볼 땐 그레이스랑 스트라트랑 가능?이라고 생각했는데...", "2026. 04. 05."),
)

private val mockReceivedReviews = listOf(
    ReceivedReviewItem("sayo", true, "글씨짱예쁘심..", "2026. 04. 05."),
    ReceivedReviewItem("무스쨩", true, "덕분에 재밌게 완독했어요~ 감사합니다", "2026. 04. 05."),
    ReceivedReviewItem("Hailey", false, "책이 파손되어 있었어요", "2026. 04. 05."),
)

@Composable
fun ReviewScreen(
    initialTab: ReviewTab = ReviewTab.WRITTEN,
    onBackClick: () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg) // 콘텐츠 배경
    ) {
        // 💡 1. 상태표시줄부터 탑바까지 하얗게 묶은 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white)
        ) {
            ReviewTopBar(onBackClick = onBackClick)
        }

        // 💡 2. 나머지 스크롤되는 콘텐츠 영역
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            ReviewTabRow(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                ReviewTab.WRITTEN -> WrittenSummaryCard(count = 45)
                ReviewTab.RECEIVED -> ReceivedSummaryCard(count = 38, name = "김스카이")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (selectedTab) {
                    ReviewTab.WRITTEN -> mockBookReviews.forEach { BookReviewCard(it) }
                    ReviewTab.RECEIVED -> mockReceivedReviews.forEach { ReceivedReviewCard(it) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun ReviewTopBar(onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "뒤로가기", tint = BookiiBookiiTheme.colors.grey900, modifier = Modifier.size(32.dp))
            }
            Text(text = "후기", style = BookiiBookiiTheme.typography.medium20, color = BookiiBookiiTheme.colors.grey900)
            IconButton(onClick = { /* 검색 동작 */ }, modifier = Modifier.size(40.dp)) {
                Icon(painter = painterResource(R.drawable.ic_search), contentDescription = "검색하기", tint = BookiiBookiiTheme.colors.grey900, modifier = Modifier.size(32.dp))
            }
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 0.5.dp)
    }
}

@Composable
private fun ReviewTabRow(selectedTab: ReviewTab, onTabSelected: (ReviewTab) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(ReviewTab.WRITTEN to "작성한 후기", ReviewTab.RECEIVED to "받은 후기").forEach { (tab, label) ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.white)
                    .then(if (!isSelected) Modifier.border(1.dp, BookiiBookiiTheme.colors.grey200, RoundedCornerShape(16.dp)) else Modifier)
                    .clickable { onTabSelected(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = label, style = BookiiBookiiTheme.typography.regular15, color = if (isSelected) BookiiBookiiTheme.colors.white else BookiiBookiiTheme.colors.grey900)
            }
        }
    }
}

@Composable
private fun WrittenSummaryCard(count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BookiiBookiiTheme.colors.white).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(painter = painterResource(R.drawable.ic_book), contentDescription = null, tint = BookiiBookiiTheme.colors.uiMain, modifier = Modifier.size(24.dp))
        val text = buildAnnotatedString {
            withStyle(BookiiBookiiTheme.typography.medium16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey900)) { append("$count") }
            withStyle(BookiiBookiiTheme.typography.regular16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey700)) { append("권의 책에 후기를 남겼어요") }
        }
        Text(text = text, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ReceivedSummaryCard(count: Int, name: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BookiiBookiiTheme.colors.white).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(painter = painterResource(R.drawable.ic_hand_thumbs_up), contentDescription = null, tint = BookiiBookiiTheme.colors.uiMain, modifier = Modifier.size(24.dp))
        val text = buildAnnotatedString {
            withStyle(BookiiBookiiTheme.typography.medium16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey900)) { append("$count") }
            withStyle(BookiiBookiiTheme.typography.regular16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey700)) { append("명의 부키메이트가 ") }
            withStyle(BookiiBookiiTheme.typography.medium16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey900)) { append(name) }
            withStyle(BookiiBookiiTheme.typography.regular16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey700)) { append("님을 좋아합니다.") }
        }
        Text(text = text, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BookReviewCard(review: BookReviewItem) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(BookiiBookiiTheme.colors.white).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(review.title, style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900)
                        Box(modifier = Modifier.width(1.dp).height(15.dp).background(BookiiBookiiTheme.colors.grey200))
                        Text(review.author, style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900)
                    }
                    ReviewStarRating(rating = review.rating)
                }
                ExchangeTypeChip(isDelivery = review.isDelivery)
            }
            HorizontalDivider(color = BookiiBookiiTheme.colors.grey200)
        }
        Text(text = review.comment, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey700)
        Text(text = review.date, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
    }
}

@Composable
private fun ReceivedReviewCard(review: ReceivedReviewItem) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(BookiiBookiiTheme.colors.white).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfilePlaceholder(modifier = Modifier.size(32.dp))
                Text(review.nickname, style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey800)
            }
            ReviewTypeChip(isGood = review.isGood)
        }
        Text(text = review.comment, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey700)
        Text(text = review.date, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
    }
}

@Composable
private fun ReviewStarRating(rating: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy((-2).dp)) {
        repeat(5) { index -> Icon(painter = painterResource(R.drawable.ic_star), contentDescription = null, tint = if (index < rating) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey200, modifier = Modifier.size(16.dp)) }
    }
}


@Preview(showBackground = true, widthDp = 412)
@Composable
private fun ReviewScreenWrittenPreview() {
    ReviewScreen(initialTab = ReviewTab.WRITTEN)
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun ReviewScreenReceivedPreview() {
    ReviewScreen(initialTab = ReviewTab.RECEIVED)
}
