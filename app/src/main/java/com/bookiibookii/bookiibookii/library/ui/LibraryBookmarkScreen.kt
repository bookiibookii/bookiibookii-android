package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private enum class BookmarkSortType { RECENT, OLDEST }

// 라우트 진입점 — 구 LibraryBookmarkFragment의 onCreateView/onResume 로직을 그대로 이식
@Composable
fun LibraryBookmarkRoute(
    onBackClick: () -> Unit,
    onCardClick: (initialIndex: Int, sortByLatest: Boolean, cards: List<ReadingCard>) -> Unit,
    viewModel: com.bookiibookii.bookiibookii.library.vm.LibraryBookmarkViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // 구 Fragment의 onResume()처럼 화면이 다시 보일 때마다(최초 진입 포함) 재조회
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        viewModel.fetchBookmarkedCards()
    }

    LibraryBookmarkScreen(
        cards = state.cards,
        isLoading = state.isLoading,
        onSortChange = { isLatest -> viewModel.sortByLatest(isLatest) },
        onBackClick = onBackClick,
        onMoveToLibrary = onBackClick,
        onCardClick = { index, bookmarkedCards -> onCardClick(index, true, bookmarkedCards) },
    )
}

@Composable
fun LibraryBookmarkScreen(
    cards: List<ReadingCard> = emptyList(),
    isLoading: Boolean = false,
    onSortChange: (isLatest: Boolean) -> Unit = {},
    onBackClick: () -> Unit = {},
    onCardClick: (index: Int, bookmarkedCards: List<ReadingCard>) -> Unit = { _, _ -> },
    onMoveToLibrary: () -> Unit = {},
) {
    var sortType by remember { mutableStateOf(BookmarkSortType.RECENT) }
    val bookmarkedCards = cards  // 이미 서버에서 필터된 북마크 카드

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BookiiBackButton(onClick = onBackClick)
                Text(text = "북마크", style = BookiiBookiiTheme.typography.medium20, color = BookiiBookiiTheme.colors.grey900)
                // 정렬 아이콘 자리
                Box(modifier = Modifier.size(40.dp))
            }
            HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 1.dp)
        }

        if (!isLoading && bookmarkedCards.isEmpty()) {
            // 빈 상태: 안내 카드 + 서재 이동 버튼
            BookmarkEmptyContent(onMoveToLibrary = onMoveToLibrary)
        } else {
            // 카운트 + 정렬
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${bookmarkedCards.size} 개",
                    style = BookiiBookiiTheme.typography.medium16,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "최신순",
                        style = if (sortType == BookmarkSortType.RECENT) BookiiBookiiTheme.typography.semibold14 else BookiiBookiiTheme.typography.regular14,
                        color = if (sortType == BookmarkSortType.RECENT) BookiiBookiiTheme.colors.grey800 else BookiiBookiiTheme.colors.grey500,
                        modifier = Modifier.clickable { sortType = BookmarkSortType.RECENT; onSortChange(true) },
                    )
                    Text(text = " | ", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey300)
                    Text(
                        text = "과거순",
                        style = if (sortType == BookmarkSortType.OLDEST) BookiiBookiiTheme.typography.semibold14 else BookiiBookiiTheme.typography.regular14,
                        color = if (sortType == BookmarkSortType.OLDEST) BookiiBookiiTheme.colors.grey800 else BookiiBookiiTheme.colors.grey500,
                        modifier = Modifier.clickable { sortType = BookmarkSortType.OLDEST; onSortChange(false) },
                    )
                }
            }

            // 카드 그리드 (2열)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                bookmarkedCards.chunked(2).forEachIndexed { rowIndex, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        row.forEachIndexed { colIndex, card ->
                            BookmarkCardItem(
                                card = card,
                                onClick = { onCardClick(rowIndex * 2 + colIndex, bookmarkedCards) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// 빈 상태 안내 카드 + "서재로 이동하기" 버튼
@Composable
private fun BookmarkEmptyContent(
    onMoveToLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
            .clip(BookiiBookiiTheme.shape.round24)
            .background(BookiiBookiiTheme.colors.white)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "아직 저장된 독서카드가 없어요.\n공감되는 독서카드를 저장해보세요.",
            style = BookiiBookiiTheme.typography.medium16,
            color = BookiiBookiiTheme.colors.grey900,
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(BookiiBookiiTheme.shape.round16)
                .background(BookiiBookiiTheme.colors.uiMain)
                .clickable { onMoveToLibrary() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "서재로 이동하기",
                style = BookiiBookiiTheme.typography.regular15,
                color = BookiiBookiiTheme.colors.white,
            )
        }
    }
}

@Composable
private fun BookmarkCardItem(
    card: ReadingCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(275.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .clickable { onClick() },
    ) {
        // 상단 텍스트 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(147.dp)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProfilePlaceholder(imageUrl = card.creatorProfileImageUrl, modifier = Modifier.size(24.dp))
                    Text(text = card.username, style = BookiiBookiiTheme.typography.medium14, color = BookiiBookiiTheme.colors.grey800)
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(BookiiBookiiTheme.colors.uiMainPale)
                        .border(0.5.dp, BookiiBookiiTheme.colors.uiMain, CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bookmark_fill),
                        contentDescription = null,
                        tint = BookiiBookiiTheme.colors.uiMain,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Text(
                text = card.content,
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey800,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // 리액션 아이콘
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                    card.reactionCounts.filter { it.value > 0 }.keys.forEach { apiKey ->
                        com.bookiibookii.bookiibookii.library.ui.reactionIconByApiKey[apiKey]?.let { iconRes ->
                            // 풀컬러 이모지 아이콘이라 독서카드 상세와 동일하게 원래 색 그대로 표시(tint 미적용)
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(13.dp),
                            )
                        }
                    }
                }
                Text(
                    text = if (card.page.isNotBlank() && card.page != "0") "p.${card.page}" else "",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey400,
                )
            }
        }

        // 하단 비주얼 영역
        when (card.type) {
            ReadingCardType.PHOTO -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(BookiiBookiiTheme.colors.grey200),
            ) {
                if (!card.imageUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model              = card.imageUrl,
                        contentDescription = null,
                        contentScale       = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier           = Modifier.matchParentSize(),
                    )
                }
            }
            ReadingCardType.QUOTE -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFF4E18), Color(0xFFFF7618), Color(0xFFFFC9A4)),
                            start = Offset(0f, Float.POSITIVE_INFINITY),
                            end = Offset(Float.POSITIVE_INFINITY, 0f),
                        )
                    )
                    .padding(8.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_quote),
                        contentDescription = null,
                        tint = BookiiBookiiTheme.colors.white,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = card.content,
                        style = BookiiBookiiTheme.typography.regular12,
                        color = BookiiBookiiTheme.colors.white,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private val previewBookmarkCards = listOf(
    ReadingCard(
        username = "북이",
        content = "새는 알에서 나오려고 투쟁한다. 알은 세계다.",
        page = "123",
        type = ReadingCardType.QUOTE,
        bookTitle = "데미안",
        reactionCounts = mapOf("LIKE" to 3, "FUN" to 1),
    ),
    ReadingCard(
        username = "부키",
        content = "이 장면이 특히 인상 깊었어요.",
        page = "45",
        type = ReadingCardType.PHOTO,
        bookTitle = "데미안",
    ),
    ReadingCard(
        username = "초록",
        content = "오래 남는 문장이었다.",
        page = "0",
        type = ReadingCardType.QUOTE,
        bookTitle = "어린 왕자",
    ),
)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LibraryBookmarkScreenPreview() {
    BookiiPreview {
        LibraryBookmarkScreen(cards = previewBookmarkCards)
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LibraryBookmarkScreenEmptyPreview() {
    BookiiPreview {
        LibraryBookmarkScreen(cards = emptyList())
    }
}
