package com.bookiibookii.bookiibookii.mypage.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.data.model.mypage.BookReviewSummaryDto
import com.bookiibookii.bookiibookii.data.model.mypage.ReceivedMemberReviewDto
import com.bookiibookii.bookiibookii.ui.component.ExchangeTypeChip
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.component.ReviewTypeChip
import com.bookiibookii.bookiibookii.common.stripBookSubtitle
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun WrittenReviewsSection(
    reviewCount: Int,
    reviews: List<BookReviewSummaryDto>?,
    onArrowClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "작성한 후기", style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900)
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.size(24.dp).graphicsLayer { scaleX = -1f }.clickable { onArrowClick() },
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BookiiBookiiTheme.colors.white)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(painter = painterResource(R.drawable.ic_book), contentDescription = null, tint = BookiiBookiiTheme.colors.uiMain, modifier = Modifier.size(24.dp))
            val bookCountText = buildAnnotatedString {
                withStyle(BookiiBookiiTheme.typography.medium16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey900)) { append("$reviewCount") }
                withStyle(BookiiBookiiTheme.typography.regular16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey700)) { append("권의 책에 후기를 남겼어요") }
            }
            Text(text = bookCountText)
        }

        if (reviews.isNullOrEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BookiiBookiiTheme.colors.white).padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "작성한 후기가 없어요", style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey600, textAlign = TextAlign.Center)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                reviews.forEach { WrittenReviewCard(it) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WrittenReviewCard(review: BookReviewSummaryDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 제목+저자가 길면 칩 영역을 침범하지 않고 저자(구분선 포함)가 다음 줄로 내려가도록 FlowRow 사용
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = review.bookTitle.stripBookSubtitle(),
                        style = BookiiBookiiTheme.typography.semibold16,
                        color = BookiiBookiiTheme.colors.grey900,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                    Row(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(modifier = Modifier.width(1.dp).height(15.dp).background(BookiiBookiiTheme.colors.grey200))
                        Text(text = review.bookAuthor, style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900)
                    }
                }
                MypageStarRating(rating = review.rating.toInt().coerceIn(0, 5))
            }
            ExchangeTypeChip(isDelivery = review.tradeType != "DIRECT")
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200)
        Text(text = review.comment ?: "", style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey700)
        Text(text = DateUtils.formatDate(review.reviewDate), style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
    }
}

@Composable
internal fun ReceivedReviewsSection(
    boomUpCount: Int,
    nickname: String,
    reviews: List<ReceivedMemberReviewDto>?,
    onArrowClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "받은 후기", style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900)
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.size(24.dp).graphicsLayer { scaleX = -1f }.clickable { onArrowClick() },
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BookiiBookiiTheme.colors.white)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(painter = painterResource(R.drawable.ic_hand_thumbs_up), contentDescription = null, tint = BookiiBookiiTheme.colors.uiMain, modifier = Modifier.size(24.dp))
            val summaryText = buildAnnotatedString {
                withStyle(BookiiBookiiTheme.typography.medium16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey900)) { append("$boomUpCount") }
                withStyle(BookiiBookiiTheme.typography.regular16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey700)) { append("명의 부키메이트가 ") }
                withStyle(BookiiBookiiTheme.typography.medium16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey900)) { append(nickname) }
                withStyle(BookiiBookiiTheme.typography.regular16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey700)) { append("님을 좋아합니다.") }
            }
            Text(text = summaryText, modifier = Modifier.weight(1f))
        }

        if (reviews.isNullOrEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BookiiBookiiTheme.colors.white).padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "받은 후기가 없어요", style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey600, textAlign = TextAlign.Center)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                reviews.forEach { ReceivedReviewCard(it) }
            }
        }
    }
}

@Composable
private fun ReceivedReviewCard(review: ReceivedMemberReviewDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfilePlaceholder(modifier = Modifier.size(32.dp))
                Text(text = review.reviewerNickname, style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey800)
            }
            ReviewTypeChip(isGood = review.reaction == "BOOM_UP", modifier = Modifier)
        }
        Text(text = review.comment ?: "", style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey700)
        Text(text = DateUtils.formatDate(review.createdAt), style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
    }
}

@Composable
private fun MypageStarRating(rating: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy((-2).dp)) {
        repeat(5) { index ->
            // 채운 별: ic_star_fill + uiMainSub / 빈 별: ic_star + grey200
            val filled = index < rating
            Icon(
                painter = painterResource(if (filled) R.drawable.ic_star_fill else R.drawable.ic_star),
                contentDescription = null,
                tint = if (filled) BookiiBookiiTheme.colors.uiMainSub else BookiiBookiiTheme.colors.grey200,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun WrittenReviewsSectionPreview() {
    BookiiBookiiTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.uiBg)
                .padding(vertical = 16.dp),
        ) {
            WrittenReviewsSection(
                reviewCount = 3,
                reviews = listOf(
                    BookReviewSummaryDto(
                        bookTitle = "데미안",
                        bookAuthor = "헤르만 헤세",
                        tradeType = "DELIVERY",
                        rating = 4.0,
                        comment = "성장에 대해 다시 생각하게 한 책.",
                        reviewDate = "2026. 05. 01.",
                    ),
                ),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun ReceivedReviewsSectionPreview() {
    BookiiBookiiTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.uiBg)
                .padding(vertical = 16.dp),
        ) {
            ReceivedReviewsSection(
                boomUpCount = 5,
                nickname = "부키",
                reviews = listOf(
                    ReceivedMemberReviewDto(
                        reviewerNickname = "noshel",
                        reviewerProfileUrl = null,
                        reaction = "BOOM_UP",
                        comment = "교환 매너가 좋았어요!",
                        createdAt = "2026. 05. 02.",
                    ),
                ),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun ReviewsSectionEmptyPreview() {
    BookiiBookiiTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.uiBg)
                .padding(vertical = 16.dp),
        ) {
            WrittenReviewsSection(reviewCount = 0, reviews = emptyList())
        }
    }
}
