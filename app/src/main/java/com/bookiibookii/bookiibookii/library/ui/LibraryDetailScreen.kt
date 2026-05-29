package com.bookiibookii.bookiibookii.library.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

enum class ReadingCardType { PHOTO, QUOTE }

data class ReadingCard(
    val username: String,
    val content: String,
    val page: String,
    val type: ReadingCardType,
    val isBookmarked: Boolean = false,
    val date: String = "",
)

data class LibraryDetailBook(
    val groupName: String,
    val title: String,
    val author: String,
    val genre: String,
    val rating: Int,
    val startDate: String,
    val endDate: String,
)

private val mockDetailBook = LibraryDetailBook(
    groupName = "[헤일리와 함께해요]",
    title = "프로젝트 헤일메리",
    author = "앤디 위어",
    genre = "소설",
    rating = 4,
    startDate = "2025. 12. 18.",
    endDate = "2026. 01. 12.",
)

val mockReadingCards = listOf(
    ReadingCard("nue_sway", "책을 쓰지 않고 한 우물만 팠다면, 나는 그들이 원하는 자리에 앉아 행복했을까. 나는 오히려 우물을 나와서 많이 느낀다.", "p.97", ReadingCardType.PHOTO, true, "2026. 04. 05."),
    ReadingCard("nue_sway", "책을 쓰지 않고 한 우물만 팠다면, 나는 그들이 원하는 자리에 앉아 행복했을까. 나는 오히려 우물을 나와서 많이 느낀다.", "p.112", ReadingCardType.QUOTE, true, "2026. 04. 06."),
    ReadingCard("sayo", "세상의 다양성을, 내가 보고 느낄 수 있는 것들의 가치를. 그것이 내가 우물 밖으로 나온 이유다.", "p.134", ReadingCardType.QUOTE, false, "2026. 04. 07."),
    ReadingCard("sayo", "책을 쓰지 않고 한 우물만 팠다면, 나는 그들이 원하는 자리에 앉아 행복했을까.", "p.58", ReadingCardType.PHOTO, false, "2026. 04. 08."),
    ReadingCard("nue_sway", "나는 오히려 우물을 나와서 많이 느낀다. 세상의 다양성을.", "p.97", ReadingCardType.PHOTO, true, "2026. 04. 09."),
    ReadingCard("sayo", "태어나려는 자는 한 세계를 파괴해야 한다.", "p.200", ReadingCardType.QUOTE, false, "2026. 04. 10."),
)

@Composable
fun LibraryDetailScreen(
    book: LibraryDetailBook = mockDetailBook,
    cards: List<ReadingCard> = mockReadingCards,
    onBackClick: () -> Unit = {},
    onAddTextCard: () -> Unit = {},
    onAddPhotoCard: () -> Unit = {},
    onCardClick: (initialIndex: Int, sortedCards: List<ReadingCard>, sortByLatest: Boolean) -> Unit = { _, _, _ -> },
    onReviewClick: () -> Unit = {},
) {
    var myCardsOnly by remember { mutableStateOf(true) }
    var sortByLatest by remember { mutableStateOf(true) }
    var showBookSheet by remember { mutableStateOf(false) }
    var isRepresentative by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }

    val displayedCards = if (myCardsOnly) cards else cards
    val sortedCards = if (sortByLatest) displayedCards else displayedCards.reversed()

    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 딤 (FAB 메뉴 열렸을 때)
        if (showFabMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000))
                    .clickable { showFabMenu = false },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BookiiBookiiTheme.colors.uiBg)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 104.dp),
        ) {
            DetailHeader(title = book.title, onBackClick = onBackClick, onMenuClick = { showBookSheet = true })
            Spacer(modifier = Modifier.height(16.dp))
            BookInfoCard(book = book, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            FilterRow(
                myCardsOnly = myCardsOnly,
                sortByLatest = sortByLatest,
                onMyCardsToggle = { myCardsOnly = !myCardsOnly },
                onSortToggle = { sortByLatest = !sortByLatest },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            CardGrid(
                cards = sortedCards,
                onCardClick = { index -> onCardClick(index, sortedCards, sortByLatest) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // FAB 메뉴 + FAB 버튼 영역
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 확장 메뉴 항목들
            AnimatedVisibility(
                visible = showFabMenu,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // 이미지 카드 → 위쪽
                    FabMenuItem(
                        label = "이미지 카드 추가하기",
                        onClick = {
                            showFabMenu = false
                            onAddPhotoCard()
                        },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_image),
                                contentDescription = null,
                                tint = BookiiBookiiTheme.colors.white,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )
                    // 인용구 카드 → 아래쪽
                    FabMenuItem(
                        label = "인용구 카드 추가하기",
                        onClick = {
                            showFabMenu = false
                            onAddTextCard()
                        },
                        icon = {
                            Text(
                                text = "T",
                                color = BookiiBookiiTheme.colors.white,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                            )
                        },
                    )
                }
            }

            // 메인 FAB
            // FAB 색: Grey900 검정 (피그마 스펙)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(BookiiBookiiTheme.colors.grey900)
                    .clickable { showFabMenu = !showFabMenu },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (showFabMenu) "닫기" else "독서카드 추가",
                    tint = BookiiBookiiTheme.colors.white,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        if (showBookSheet) {
            LibraryBookBottomSheet(
                title = book.title,
                author = book.author,
                genre = book.genre,
                isRepresentative = isRepresentative,
                onDismiss = { showBookSheet = false },
                onReviewClick = {
                    showBookSheet = false
                    onReviewClick()
                },
                onToggleRepresentativeClick = {
                    isRepresentative = !isRepresentative
                    showBookSheet = false
                },
                onAladinClick = { showBookSheet = false },
                onDeleteClick = { showBookSheet = false },
            )
        }
    }
}

// 아이콘과 글씨가 검정 원형(pill) 배경에 함께 둘러싸인 FAB 메뉴 아이템
@Composable
private fun FabMenuItem(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(BookiiBookiiTheme.colors.grey900)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        icon()
        Text(
            text = label,
            style = BookiiBookiiTheme.typography.medium14,
            color = BookiiBookiiTheme.colors.white,
        )
    }
}

@Composable
private fun DetailHeader(title: String, onBackClick: () -> Unit, onMenuClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "뒤로 가기", tint = BookiiBookiiTheme.colors.grey900, modifier = Modifier.size(24.dp))
            }
            Text(text = title, style = BookiiBookiiTheme.typography.medium20, color = BookiiBookiiTheme.colors.grey900)
            IconButton(onClick = onMenuClick, modifier = Modifier.size(40.dp)) {
                Icon(painter = painterResource(R.drawable.ic_hamburger), contentDescription = "메뉴", tint = BookiiBookiiTheme.colors.grey900, modifier = Modifier.size(24.dp))
            }
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 0.5.dp)
    }
}

@Composable
private fun BookInfoCard(book: LibraryDetailBook, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(modifier = Modifier.width(102.dp).height(146.dp).clip(RoundedCornerShape(10.dp)).background(BookiiBookiiTheme.colors.grey200))
        Column(modifier = Modifier.weight(1f).height(146.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = book.groupName, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey600, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = book.title, style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = book.author, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey900)
                    Text(text = "(${book.genre})", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey900)
                }
                // 별점 아이콘 크기: 20dp (피그마 스펙)
                Row {
                    for (i in 1..5) {
                        Icon(painter = painterResource(R.drawable.ic_star), contentDescription = null, tint = if (i <= book.rating) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey200, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Text(text = "${book.startDate} ~ ${book.endDate}", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
        }
    }
}

@Composable
private fun FilterRow(
    myCardsOnly: Boolean,
    sortByLatest: Boolean,
    onMyCardsToggle: () -> Unit,
    onSortToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onMyCardsToggle() }) {
            Box(
                modifier = Modifier.size(20.dp).clip(RoundedCornerShape(5.dp)).background(if (myCardsOnly) BookiiBookiiTheme.colors.uiMainSub else BookiiBookiiTheme.colors.grey200),
                contentAlignment = Alignment.Center,
            ) {
                if (myCardsOnly) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = BookiiBookiiTheme.colors.white, modifier = Modifier.size(16.dp))
                }
            }
            Text(text = "내 독서카드만 보기", style = BookiiBookiiTheme.typography.medium14, color = BookiiBookiiTheme.colors.grey500)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "최신순", style = if (sortByLatest) BookiiBookiiTheme.typography.semibold14 else BookiiBookiiTheme.typography.regular14, color = if (sortByLatest) BookiiBookiiTheme.colors.grey800 else BookiiBookiiTheme.colors.grey500, modifier = Modifier.clickable { onSortToggle() })
            Text(text = "|", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
            Text(text = "페이지순", style = if (!sortByLatest) BookiiBookiiTheme.typography.semibold14 else BookiiBookiiTheme.typography.regular14, color = if (!sortByLatest) BookiiBookiiTheme.colors.grey800 else BookiiBookiiTheme.colors.grey500, modifier = Modifier.clickable { onSortToggle() })
        }
    }
}

@Composable
private fun CardGrid(cards: List<ReadingCard>, onCardClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        cards.chunked(2).forEachIndexed { rowIndex, row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEachIndexed { colIndex, card ->
                    ReadingCardItem(card = card, onClick = { onCardClick(rowIndex * 2 + colIndex) }, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ReadingCardItem(card: ReadingCard, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.height(275.dp).clip(RoundedCornerShape(20.dp)).background(BookiiBookiiTheme.colors.white).clickable { onClick() }) {
        Column(
            modifier = Modifier.fillMaxWidth().height(147.dp).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProfilePlaceholder(modifier = Modifier.size(24.dp))
                    Text(text = card.username, style = BookiiBookiiTheme.typography.medium14, color = BookiiBookiiTheme.colors.grey800)
                }
                if (card.isBookmarked) {
                    Box(
                        modifier = Modifier.clip(CircleShape).background(BookiiBookiiTheme.colors.uiMainPale).border(0.5.dp, BookiiBookiiTheme.colors.uiMain, CircleShape).padding(2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_bookmark_fill), contentDescription = null, tint = BookiiBookiiTheme.colors.uiMain, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Text(text = card.content, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey800, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth().weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(painter = painterResource(R.drawable.ic_heart_fill), contentDescription = null, tint = BookiiBookiiTheme.colors.uiPointRed, modifier = Modifier.size(20.dp))
                Text(text = card.page, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey400)
            }
        }

        when (card.type) {
            ReadingCardType.PHOTO -> Box(modifier = Modifier.fillMaxWidth().weight(1f).background(BookiiBookiiTheme.colors.grey200))
            ReadingCardType.QUOTE -> Box(
                modifier = Modifier.fillMaxWidth().weight(1f).background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFF4E18), Color(0xFFFF7618), Color(0xFFFFC9A4)),
                        start = Offset(0f, Float.POSITIVE_INFINITY),
                        end = Offset(Float.POSITIVE_INFINITY, 0f),
                    )
                ).padding(8.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(painter = painterResource(R.drawable.ic_quote), contentDescription = null, tint = BookiiBookiiTheme.colors.white, modifier = Modifier.size(16.dp))
                    Text(text = "\"새는 알에서 나오려고 싸운다. 알은 세상이다. 태어나려는 자는 한 세계를 파괴해야 한다.\"", style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.white, maxLines = 5, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LibraryDetailScreenPreview() {
    LibraryDetailScreen()
}
