package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class Reaction(
    val iconRes: Int,
    val label: String,
)

private val reactionList = listOf(
    Reaction(R.drawable.ic_heart_empty, "좋아요"),
    Reaction(R.drawable.ic_star,        "슬퍼요"),
    Reaction(R.drawable.ic_shine,       "힘나요"),
    Reaction(R.drawable.ic_book,        "공감해요"),
    Reaction(R.drawable.ic_hand_thumbs_up, "멋져요"),
    Reaction(R.drawable.ic_smile,       "웃겨요"),
)

// 리액션 오버레이 zigzag 위치 계산
private fun reactionEndR(n: Int) = (16 + n * 44).dp
private fun reactionEndL(n: Int) = (56 + n * 44).dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingCardDetailScreen(
    cards: List<ReadingCard> = emptyList(),
    initialIndex: Int = 0,
    sortByLatest: Boolean = true,
    onBackClick: () -> Unit = {},
    onBookmarkToggle: (cardId: Long) -> Unit = {},
    onReactionToggle: (cardId: Long, reaction: String) -> Unit = { _, _ -> },
    onInstaShare: (card: ReadingCard) -> Unit = {},
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { cards.size }
    val coroutineScope = rememberCoroutineScope()
    var showShareSheet by remember { mutableStateOf(false) }

    // 카드별 북마크 상태 (낙관적 로컬 업데이트)
    val bookmarkStates = remember(cards) {
        mutableStateListOf(*Array(cards.size) { i -> cards.getOrNull(i)?.isBookmarked ?: false })
    }

    // 리액션 상태
    // 카드 버전 (1 or 2) — 페이지 변경 시 초기화
    var cardVersion by remember { mutableStateOf(1) }

    var activeReactionList by remember { mutableStateOf(listOf<Reaction>()) }
    val emojiVisibleCount = remember { mutableStateMapOf<String, Int>() }
    val animJobs = remember { mutableStateMapOf<String, Job>() }

    LaunchedEffect(pagerState.currentPage) {
        animJobs.values.forEach { it.cancel() }
        animJobs.clear()
        activeReactionList = listOf()
        emojiVisibleCount.clear()
        cardVersion = 1
    }

    val currentCard = cards.getOrNull(pagerState.currentPage)
    val progress = if (cards.isNotEmpty()) (pagerState.currentPage + 1).toFloat() / cards.size else 1f
    val currentBookmarked = bookmarkStates.getOrElse(pagerState.currentPage) { false }

    Column(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.uiBg)) {
        CardDetailHeader(onBackClick = onBackClick, onShareClick = { showShareSheet = true })

        CardInfoArea(
            card         = currentCard,
            sortByLatest = sortByLatest,
            progress     = progress,
            isBookmarked = currentBookmarked,
            onBookmarkToggle = {
                val idx = pagerState.currentPage
                if (idx in bookmarkStates.indices) {
                    bookmarkStates[idx] = !bookmarkStates[idx]
                    currentCard?.cardId?.let { onBookmarkToggle(it) }
                }
            },
        )

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = 8.dp,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val card = cards[page]
                val isCurrentPage = page == pagerState.currentPage
                ReadingCardDetailItem(
                    card               = card,
                    cardVersion        = if (isCurrentPage) cardVersion else 1,
                    activeReactions    = if (isCurrentPage) activeReactionList else emptyList(),
                    emojiVisibleCounts = if (isCurrentPage) emojiVisibleCount else emptyMap(),
                    modifier           = Modifier.fillMaxHeight().padding(vertical = 8.dp),
                )
            }
        }

        // 버전 선택 도트 2개 (카드 타입별 디자인 다름, 클릭 가능)
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val currentType = currentCard?.type ?: ReadingCardType.PHOTO
                CardVersionDot(version = 1, cardType = currentType, isSelected = cardVersion == 1, onClick = { cardVersion = 1 })
                CardVersionDot(version = 2, cardType = currentType, isSelected = cardVersion == 2, onClick = { cardVersion = 2 })
            }
        }

        ReactionBar(
            activeReactions = activeReactionList,
            onReact = { reaction ->
                val label = reaction.label
                if (activeReactionList.any { it.label == label }) {
                    // 비활성화
                    animJobs[label]?.cancel()
                    animJobs.remove(label)
                    activeReactionList = activeReactionList.filter { it.label != label }
                    emojiVisibleCount.remove(label)
                } else {
                    // 활성화 + 하트 애니메이션
                    activeReactionList = activeReactionList + reaction
                    emojiVisibleCount[label] = 0
                    animJobs[label] = coroutineScope.launch {
                        repeat(4) { i ->
                            delay(80L)
                            emojiVisibleCount[label] = i + 1
                        }
                        animJobs.remove(label)
                    }
                }
                currentCard?.cardId?.let { onReactionToggle(it, label) }
            },
            modifier = Modifier.padding(bottom = 20.dp),
        )
    }

    if (showShareSheet) {
        ReadingCardShareBottomSheet(
            onDismiss   = { showShareSheet = false },
            onInstaClick = {
                showShareSheet = false
                currentCard?.let { onInstaShare(it) }
            },
        )
    }
}

// ── 버전 선택 도트 (2개, 클릭 가능) ─────────────────────────────────────────
// PHOTO v1: 흰→주황 그라데이션 / PHOTO v2: 대각 분할
// QUOTE v1: 연한 주황 T       / QUOTE v2: 진한 주황 T

@Composable
private fun CardVersionDot(
    version: Int,
    cardType: ReadingCardType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) Color(0x99100F0E) else Color(0x66E2E1DF)

    Box(
        modifier = Modifier
            .size(46.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .border(2.dp, borderColor, CircleShape),
        ) {
            when (cardType) {
                ReadingCardType.PHOTO -> {
                    if (version == 1) {
                        // 사진 v1: 흰→주황 그라데이션
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.White, Color(0xFFFF7618)),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, size.height),
                                )
                            )
                        }
                    } else {
                        // 사진 v2: 우상단 주황 삼각 / 좌하단 흰
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(color = Color.White)
                            drawPath(
                                path = Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(size.width, 0f)
                                    lineTo(size.width, size.height)
                                    close()
                                },
                                color = Color(0xFFFF7618),
                            )
                        }
                    }
                }
                ReadingCardType.QUOTE -> {
                    // 인용구 v1: 연한(페일) 주황 T / v2: 진한(비비드) 주황 T
                    val bgColor = if (version == 1) Color(0xFFFFC9A4) else Color(0xFFFF7618)
                    Box(
                        modifier = Modifier.fillMaxSize().background(bgColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "T",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                        )
                    }
                }
            }
        }
    }
}

// ── 리액션 오버레이 (zigzag 하트 4개, AnimatedVisibility) ─────────────────────

@Composable
private fun BoxScope.ReactionOverlay(
    activeReactions: List<Reaction>,
    emojiVisibleCounts: Map<String, Int>,
) {
    activeReactions.forEachIndexed { reactionIdx, reaction ->
        val visibleCount = emojiVisibleCounts[reaction.label] ?: 0
        val endR = reactionEndR(reactionIdx)
        val endL = reactionEndL(reactionIdx)

        listOf(
            Triple(1, endR, 16.dp),
            Triple(2, endL, 56.dp),
            Triple(3, endR, 96.dp),
            Triple(4, endL, 136.dp),
        ).forEachIndexed { posIdx, (minCount, endPad, bottomPad) ->
            key(reaction.label, posIdx) {
                AnimatedVisibility(
                    visible = visibleCount >= minCount,
                    enter = scaleIn(initialScale = 0.3f, animationSpec = spring()) + fadeIn(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = bottomPad, end = endPad),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_heart_fill),
                        contentDescription = null,
                        tint = BookiiBookiiTheme.colors.uiPointRed,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

// ── 카드 아이템 컨테이너 ──────────────────────────────────────────────────────

@Composable
private fun ReadingCardDetailItem(
    card: ReadingCard,
    cardVersion: Int,
    activeReactions: List<Reaction>,
    emojiVisibleCounts: Map<String, Int>,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .shadow(elevation = 10.dp, shape = cardShape, ambientColor = Color(0x1A000000), spotColor = Color(0x1A000000))
            .clip(cardShape)
            .background(BookiiBookiiTheme.colors.white)
            .fillMaxWidth(),
    ) {
        when (card.type) {
            ReadingCardType.PHOTO -> PhotoCard(card, cardVersion, activeReactions, emojiVisibleCounts)
            ReadingCardType.QUOTE -> QuoteCard(card, cardVersion, activeReactions, emojiVisibleCounts)
        }
    }
}

// ── PHOTO 카드 (v1: 회색bg+페이드+텍스트 / v2: 상단사진+하단흰텍스트) ────────

@Composable
private fun PhotoCard(
    card: ReadingCard,
    cardVersion: Int,
    activeReactions: List<Reaction> = emptyList(),
    emojiVisibleCounts: Map<String, Int> = emptyMap(),
) {
    if (cardVersion == 1) {
        // v1: BookkiiTest 동일 — 회색 배경 + 상단 흰 페이드 + 텍스트
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.grey300))
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0.00f to Color.White,
                        0.22f to Color.White.copy(alpha = 0.9f),
                        0.54f to Color.Transparent,
                    )
                ),
            )
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(text = card.content, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey800, modifier = Modifier.fillMaxWidth())
            }
            ReactionOverlay(activeReactions, emojiVisibleCounts)
        }
    } else {
        // v2: 상단 사진 영역(336) + 하단 흰색 메모(128)
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(336f / 464f)
                        .background(BookiiBookiiTheme.colors.grey300),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(128f / 464f)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    Text(text = card.content, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey800, overflow = TextOverflow.Ellipsis)
                }
            }
            ReactionOverlay(activeReactions, emojiVisibleCounts)
        }
    }
}

// ── QUOTE(텍스트) 카드 — 사진 영역을 텍스트+배경으로 교체 ─────────────────────
// v1: PHOTO v1 구조 그대로, 회색 대신 주황 그라데이션 배경 + 인용구 텍스트
// v2: PHOTO v2 구조 그대로, 상단 주황 그라데이션+인용구 / 하단 흰색+메모

@Composable
private fun QuoteCard(
    card: ReadingCard,
    cardVersion: Int,
    activeReactions: List<Reaction> = emptyList(),
    emojiVisibleCounts: Map<String, Int> = emptyMap(),
) {
    val quotationText = card.quotation.ifBlank { card.content }
    val vividGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF4E18), Color(0xFFFF7618), Color(0xFFFFC9A4)),
        start = Offset(0f, Float.POSITIVE_INFINITY),
        end = Offset(Float.POSITIVE_INFINITY, 0f),
    )

    if (cardVersion == 1) {
        // v1: 주황 그라데이션 배경 + 상단 흰 페이드 + 인용구 텍스트 (PHOTO v1과 동일 구조)
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(vividGradient))
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0.00f to Color.White,
                        0.22f to Color.White.copy(alpha = 0.9f),
                        0.54f to Color.Transparent,
                    )
                ),
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(painter = painterResource(R.drawable.ic_quote), contentDescription = null, tint = BookiiBookiiTheme.colors.uiMain, modifier = Modifier.size(24.dp))
                Text(text = "\"$quotationText\"", style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey800, modifier = Modifier.fillMaxWidth())
            }
            ReactionOverlay(activeReactions, emojiVisibleCounts)
        }
    } else {
        // v2: 상단 주황 그라데이션+인용구(336) + 하단 흰색+메모(128) (PHOTO v2와 동일 구조)
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(336f / 464f)
                        .background(vividGradient),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_quote), contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Text(text = "\"$quotationText\"", style = BookiiBookiiTheme.typography.semibold20, color = Color.White, overflow = TextOverflow.Ellipsis)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(128f / 464f)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    Text(text = card.content, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey800, overflow = TextOverflow.Ellipsis)
                }
            }
            ReactionOverlay(activeReactions, emojiVisibleCounts)
        }
    }
}

// ── 공유용 카드 (오프스크린 비트맵 캡처용, 오버레이 없음) ──────────────────────

@Composable
internal fun ShareableCard(card: ReadingCard, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white),
    ) {
        when (card.type) {
            ReadingCardType.PHOTO -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.grey300))
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                0.00f to Color.White,
                                0.22f to Color.White.copy(alpha = 0.9f),
                                0.54f to Color.Transparent,
                            )
                        )
                    )
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Text(text = card.content, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey800)
                    }
                }
            }
            ReadingCardType.QUOTE -> {
                val quotationText = card.quotation.ifBlank { card.content }
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(336f / 464f)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFF4E18), Color(0xFFFF7618), Color(0xFFFFC9A4)),
                                    start = Offset(0f, Float.POSITIVE_INFINITY),
                                    end = Offset(Float.POSITIVE_INFINITY, 0f),
                                )
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 40.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(painter = painterResource(R.drawable.ic_quote), contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Text(text = "\"$quotationText\"", style = BookiiBookiiTheme.typography.semibold20, color = Color.White, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(128f / 464f).padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Text(text = card.content, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey800, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

// ── 헤더 ─────────────────────────────────────────────────────────────────────

@Composable
private fun CardDetailHeader(onBackClick: () -> Unit, onShareClick: () -> Unit) {
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
            Text(text = "독서카드", style = BookiiBookiiTheme.typography.medium20, color = BookiiBookiiTheme.colors.grey900)
            IconButton(onClick = onShareClick, modifier = Modifier.size(40.dp)) {
                Icon(painter = painterResource(R.drawable.ic_share), contentDescription = "공유", tint = BookiiBookiiTheme.colors.grey900, modifier = Modifier.size(24.dp))
            }
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 0.5.dp)
    }
}

// ── 카드 정보 영역 (진행도 바 + 북마크 버튼) ─────────────────────────────────

@Composable
private fun CardInfoArea(
    card: ReadingCard?,
    sortByLatest: Boolean,
    progress: Float,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(30.dp)).background(BookiiBookiiTheme.colors.grey200)) {
                Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(BookiiBookiiTheme.colors.uiMain))
            }
            Text(text = "| ${if (sortByLatest) "최신순" else "페이지순"}", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProfilePlaceholder(modifier = Modifier.size(32.dp))
                    Text(text = card?.username ?: "", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey800)
                }
                // 북마크 버튼 — 항상 표시, 상태에 따라 스타일 변경
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isBookmarked) BookiiBookiiTheme.colors.uiMainPale else Color.Transparent)
                        .border(
                            0.5.dp,
                            if (isBookmarked) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey300,
                            CircleShape,
                        )
                        .clickable { onBookmarkToggle() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(if (isBookmarked) R.drawable.ic_bookmark_fill else R.drawable.ic_bookmark),
                        contentDescription = "북마크",
                        tint = if (isBookmarked) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey500,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = card?.bookTitle ?: "", style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey800, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = card?.page ?: "", style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey500)
                }
                Text(text = card?.date ?: "", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
            }
        }
    }
}

// ── 리액션 바 ────────────────────────────────────────────────────────────────

@Composable
private fun ReactionBar(
    activeReactions: List<Reaction>,
    onReact: (Reaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        reactionList.forEach { reaction ->
            val isSelected = activeReactions.any { it.label == reaction.label }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { onReact(reaction) },
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.white),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(reaction.iconRes),
                        contentDescription = null,
                        tint = if (isSelected) BookiiBookiiTheme.colors.white else BookiiBookiiTheme.colors.grey700,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Text(text = reaction.label, style = BookiiBookiiTheme.typography.medium12, color = BookiiBookiiTheme.colors.grey800)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReadingCardDetailScreenPreview() {
    ReadingCardDetailScreen()
}
