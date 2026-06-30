package com.bookiibookii.bookiibookii.mypage.ui.detail

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.bookiibookii.bookiibookii.data.model.mypage.ReceivedReviewItem
import com.bookiibookii.bookiibookii.data.model.mypage.WrittenReviewItem
import com.bookiibookii.bookiibookii.ui.component.ExchangeTypeChip
import com.bookiibookii.bookiibookii.ui.component.ReviewTypeChip
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.common.DateUtils

enum class ReviewTab { WRITTEN, RECEIVED }

@Composable
fun ReviewRoute(
    viewModel: com.bookiibookii.bookiibookii.mypage.vm.MypageViewModel,
    initialTab: ReviewTab,
    onBackClick: () -> Unit,
) {
    val profile by viewModel.profileData.observeAsState()
    val writtenState by viewModel.writtenReviews.observeAsState(com.bookiibookii.bookiibookii.mypage.vm.WrittenReviewUiState())
    val receivedState by viewModel.receivedReviews.observeAsState(com.bookiibookii.bookiibookii.mypage.vm.ReceivedReviewUiState())

    LaunchedEffect(Unit) {
        viewModel.fetchWrittenReviews(reset = true)
        viewModel.fetchReceivedReviews(reset = true)
    }

    ReviewScreen(
        initialTab = initialTab,
        onBackClick = onBackClick,
        bookReviewCount = writtenState.totalCount.toInt(),
        writtenReviews = writtenState.items,
        writtenHasNext = writtenState.hasNext,
        onLoadMoreWritten = { viewModel.fetchWrittenReviews(reset = false) },
        boomUpCount = receivedState.positiveCount.toInt(),
        receivedReviews = receivedState.items,
        receivedHasNext = receivedState.hasNext,
        onLoadMoreReceived = { viewModel.fetchReceivedReviews(reset = false) },
        nickname = profile?.nickname ?: "",
    )
}

@Composable
fun ReviewScreen(
    initialTab: ReviewTab = ReviewTab.WRITTEN,
    onBackClick: () -> Unit = {},
    bookReviewCount: Int = 0,
    writtenReviews: List<WrittenReviewItem> = emptyList(),
    writtenHasNext: Boolean = false,
    onLoadMoreWritten: () -> Unit = {},
    boomUpCount: Int = 0,
    receivedReviews: List<ReceivedReviewItem> = emptyList(),
    receivedHasNext: Boolean = false,
    onLoadMoreReceived: () -> Unit = {},
    nickname: String = "",
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val listState = rememberLazyListState()

    LaunchedEffect(selectedTab) {
        listState.scrollToItem(0)
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore, selectedTab) {
        if (!shouldLoadMore) return@LaunchedEffect
        when (selectedTab) {
            ReviewTab.WRITTEN -> if (writtenHasNext) onLoadMoreWritten()
            ReviewTab.RECEIVED -> if (receivedHasNext) onLoadMoreReceived()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white)
        ) {
            ReviewTopBar(onBackClick = onBackClick)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ReviewTabRow(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                Spacer(modifier = Modifier.height(16.dp))
                when (selectedTab) {
                    ReviewTab.WRITTEN -> WrittenSummaryCard(count = bookReviewCount)
                    ReviewTab.RECEIVED -> ReceivedSummaryCard(count = boomUpCount, name = nickname)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (selectedTab) {
                ReviewTab.WRITTEN -> items(writtenReviews) { BookReviewCard(it) }
                ReviewTab.RECEIVED -> items(receivedReviews) { ReceivedReviewCard(it) }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
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
            BookiiBackButton(onClick = onBackClick)
            Text(text = "후기", style = BookiiBookiiTheme.typography.medium20, color = BookiiBookiiTheme.colors.grey900)
            Box(modifier = Modifier.size(40.dp))
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 1.dp)
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
private fun ReviewEmptyCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = message, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey600)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookReviewCard(review: WrittenReviewItem) {
    val isDelivery = review.exchangeType == "DELIVERY"
    val displayDate = review.reviewedAt?.let { DateUtils.formatDate(it) } ?: ""

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(BookiiBookiiTheme.colors.white).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            review.bookTitle.orEmpty(),
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
                            Text(review.author.orEmpty(), style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900)
                        }
                    }
                    ReviewStarRating(rating = review.rating)
                }
                ExchangeTypeChip(isDelivery = isDelivery)
            }
            HorizontalDivider(color = BookiiBookiiTheme.colors.grey200)
        }
        if (!review.content.isNullOrBlank()) {
            Text(text = review.content, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey700)
        }
        if (displayDate.isNotBlank()) {
            Text(text = displayDate, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
        }
    }
}

@Composable
private fun ReceivedReviewCard(review: ReceivedReviewItem) {
    val isGood = review.partnerReviewType == "BOOM_UP"
    val displayDate = review.reviewedAt?.let { DateUtils.formatDate(it) } ?: ""

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(BookiiBookiiTheme.colors.white).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfilePlaceholder(modifier = Modifier.size(32.dp))
                Text(review.reviewerNickname.orEmpty(), style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey800)
            }
            ReviewTypeChip(isGood = isGood)
        }
        if (!review.comment.isNullOrBlank()) {
            Text(text = review.comment, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey700)
        }
        if (displayDate.isNotBlank()) {
            Text(text = displayDate, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
        }
    }
}

@Composable
private fun ReviewStarRating(rating: Double) {
    Row {
        for (i in 1..5) {
            val starValue = (rating - (i - 1)).coerceIn(0.0, 1.0)
            val isFull = starValue >= 0.75
            val isHalf = starValue >= 0.25
            when {
                isFull -> Icon(painter = painterResource(R.drawable.ic_star_fill), contentDescription = null, tint = BookiiBookiiTheme.colors.uiMainSub, modifier = Modifier.size(16.dp))
                isHalf -> Box(modifier = Modifier.size(16.dp)) {
                    Icon(painter = painterResource(R.drawable.ic_star_fill), contentDescription = null, tint = BookiiBookiiTheme.colors.uiMainSubPale, modifier = Modifier.size(16.dp))
                    Icon(painter = painterResource(R.drawable.ic_star), contentDescription = null, tint = BookiiBookiiTheme.colors.uiMainSub, modifier = Modifier.size(16.dp))
                }
                else -> Icon(painter = painterResource(R.drawable.ic_star), contentDescription = null, tint = BookiiBookiiTheme.colors.grey300, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 800)
@Composable
private fun ReviewScreenWrittenPreview() {
    BookiiPreview {
        ReviewScreen(
            initialTab = ReviewTab.WRITTEN,
            bookReviewCount = 2,
            writtenReviews = listOf(
                WrittenReviewItem(reviewId = 1, bookId = 1, bookTitle = "데미안", author = "헤르만 헤세", rating = 4.5, content = "인생 책이에요.", exchangeType = "DELIVERY", exchangeTypeLabel = "택배", reviewedAt = "2026-05-01"),
                WrittenReviewItem(reviewId = 2, bookId = 2, bookTitle = "1984", author = "조지 오웰", rating = 5.0, content = "강렬했습니다.", exchangeType = "DIRECT", exchangeTypeLabel = "직거래", reviewedAt = "2026-04-20"),
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 800)
@Composable
private fun ReviewScreenReceivedPreview() {
    BookiiPreview {
        ReviewScreen(
            initialTab = ReviewTab.RECEIVED,
            boomUpCount = 1,
            nickname = "부키",
            receivedReviews = listOf(
                ReceivedReviewItem(reviewId = 1, reviewerId = 1, reviewerNickname = "책벌레", reviewerProfileImageUrl = null, partnerReviewType = "BOOM_UP", partnerReviewLabel = "최고예요", comment = "친절한 교환 감사했어요!", reviewedAt = "2026-05-02"),
                ReceivedReviewItem(reviewId = 2, reviewerId = 2, reviewerNickname = "독서왕", reviewerProfileImageUrl = null, partnerReviewType = "BOOM_DOWN", partnerReviewLabel = "아쉬워요", comment = "좋았습니다.", reviewedAt = "2026-04-21"),
            ),
        )
    }
}
