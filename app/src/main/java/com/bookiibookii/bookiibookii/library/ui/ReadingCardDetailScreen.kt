package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.sin

private data class Reaction(
    val iconRes: Int,
    val label: String,
    val overlayTint: Color,
)

private val reactionUiMain = Color(0xFFFF7618)

private val reactionList = listOf(
    Reaction(R.drawable.ic_heart_empty, "좋아요", reactionUiMain),
    Reaction(R.drawable.ic_star,       "슬퍼요", reactionUiMain),
    Reaction(R.drawable.ic_shine,      "힘나요", reactionUiMain),
    Reaction(R.drawable.ic_book,       "공감해요", reactionUiMain),
    Reaction(R.drawable.ic_hand_thumbs_up, "멋져요", reactionUiMain),
    Reaction(R.drawable.ic_smile,      "웃겨요", reactionUiMain),
)

// 역동적인 파티클 애니메이션을 위한 색상 풀
private val particleColors = listOf(
    Color(0xFFFF5252), Color(0xFFFF4081), Color(0xFFE040FB),
    Color(0xFF7C4DFF), Color(0xFF536DFE), Color(0xFF448AFF),
    Color(0xFF40C4FF), Color(0xFF18FFFF), Color(0xFF64FFDA),
    Color(0xFF69F0AE), Color(0xFFB2FF59), Color(0xFFEEFF41),
    Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800)
)

private data class Particle(
    val id: String,
    val iconResId: Int,
    val tint: Color,          // 내부 색상
    val sizeDp: Float,        // 랜덤 크기
    val targetY: Float,       // 랜덤 도달 높이
    val amplitude: Float      // 랜덤 좌우 흔들림 폭
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingCardDetailScreen(
    cards: List<ReadingCard> = mockReadingCards,
    initialIndex: Int = 0,
    sortByLatest: Boolean = true,
    bookTitle: String = "나는 당신을 편애합니다",
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onReaction: (cardIndex: Int, label: String) -> Unit = { _, _ -> },
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { cards.size }
    val coroutineScope = rememberCoroutineScope()
    var showShareSheet by remember { mutableStateOf(false) }
    var cardVersion by remember { mutableStateOf(1) }

    // 1. 활성화된 반응 목록 (개수 제한 없이 전부 유지)
    var activeReactionList by remember { mutableStateOf(listOf<Reaction>()) }

    val particles = remember { mutableStateListOf<Particle>() }

    LaunchedEffect(pagerState.currentPage) {
        activeReactionList = emptyList()
        particles.clear()
        cardVersion = 1
    }

    val currentCard = cards.getOrNull(pagerState.currentPage)
    val progress = if (cards.isNotEmpty()) (pagerState.currentPage + 1).toFloat() / cards.size else 1f

    Column(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.uiBg)) {
        CardDetailHeader(onBackClick = onBackClick, onShareClick = { showShareSheet = true })

        CardInfoArea(
            card = currentCard,
            sortByLatest = sortByLatest,
            progress = progress,
            bookTitle = bookTitle,
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
                    card = card,
                    cardVersion = if (isCurrentPage) cardVersion else 1,
                    particles = if (isCurrentPage) particles else emptyList(),
                    onParticleEnd = { particles.remove(it) },
                    modifier = Modifier.fillMaxHeight().padding(vertical = 8.dp),
                )
            }
        }

        // 버전 선택 도트 2개 (카드 타입별 디자인 다름)
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
                val isAlreadyActive = activeReactionList.any { it.label == label }

                // 활성화 상태 토글 (여기서는 개수 제한 없이 껐다 켰다만 함)
                if (isAlreadyActive) {
                    activeReactionList = activeReactionList.filter { it.label != label }
                } else {
                    activeReactionList = activeReactionList + reaction
                }

                // 2. 파티클 폭발 효과: 가장 최근에 누른 2개로만 한정
                coroutineScope.launch {
                    val burstCount = (5..8).random() // 5~8개 랜덤 다발

                    // 핵심: 애니메이션 풀은 무조건 '가장 최근에 켜진 2개'로 제한
                    val recentReactions = activeReactionList.takeLast(2)
                    val pool = if (recentReactions.isNotEmpty()) recentReactions else listOf(reaction)

                    repeat(burstCount) {
                        delay((10..50).random().toLong())

                        val randomReaction = pool.random()

                        particles.add(
                            Particle(
                                id = UUID.randomUUID().toString(),
                                iconResId = randomReaction.iconRes,
                                tint = particleColors.random(),
                                sizeDp = (20..38).random().toFloat(),
                                // 핵심: 끝까지 안가고 2/3 정도까지만 올라가도록 targetY 제한 (-150 ~ -320 범위)
                                targetY = -(150..320).random().toFloat(),
                                amplitude = (20..60).random().toFloat()
                            )
                        )
                    }
                }
                onReaction(pagerState.currentPage, label)
            },
            modifier = Modifier.padding(bottom = 20.dp),
        )
    }

    if (showShareSheet) {
        ReadingCardShareBottomSheet(onDismiss = { showShareSheet = false })
    }
}

@Composable
private fun FloatingParticle(
    particle: Particle,
    onAnimationEnd: (Particle) -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAnimating = true
        delay(2000)
        onAnimationEnd(particle)
    }

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 2000
            0f at 0
            1.3f at 200
            1.0f at 500
            0.8f at 1500
            0f at 2000
        },
        label = "scale"
    )

    val yOffset by animateFloatAsState(
        targetValue = if (isAnimating) particle.targetY else 0f,
        animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing),
        label = "yOffset"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isAnimating) 0f else 1f,
        animationSpec = keyframes {
            durationMillis = 2000
            1f at 0
            1f at 1200
            0f at 2000
        },
        label = "alpha"
    )

    val xOffset = sin(yOffset / 60f) * particle.amplitude

    Box(
        modifier = Modifier
            .offset(x = xOffset.dp, y = yOffset.dp)
            .alpha(alpha)
            .scale(scale)
            .size(particle.sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size((particle.sizeDp * 0.7f).dp)
                .clip(CircleShape)
                .background(particle.tint)
        )

        Icon(
            painter = painterResource(id = particle.iconResId),
            contentDescription = null,
            tint = BookiiBookiiTheme.colors.grey800,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// 시작 위치를 외부에서 주입받을 수 있도록 modifier 파라미터 추가
@Composable
private fun BoxScope.ReactionOverlay(
    particles: List<Particle>,
    onParticleEnd: (Particle) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        particles.forEach { particle ->
            key(particle.id) {
                FloatingParticle(
                    particle = particle,
                    onAnimationEnd = { onParticleEnd(it) }
                )
            }
        }
    }
}

@Composable
private fun ReadingCardDetailItem(
    card: ReadingCard,
    cardVersion: Int,
    particles: List<Particle>,
    onParticleEnd: (Particle) -> Unit,
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
            ReadingCardType.PHOTO -> PhotoCardFadedBg(card, cardVersion, particles, onParticleEnd)
            ReadingCardType.QUOTE -> QuoteCard(card, cardVersion, particles, onParticleEnd)
        }
    }
}

@Composable
private fun PhotoCardFadedBg(
    card: ReadingCard,
    cardVersion: Int,
    particles: List<Particle>,
    onParticleEnd: (Particle) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 사진 영역 (회색 placeholder)
        Box(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.grey300))

        if (cardVersion == 1) {
            // v1: 위쪽 흰색 페이드 + 텍스트, 아래쪽 사진
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0.00f to Color.White, 0.22f to Color.White.copy(alpha = 0.9f), 0.54f to Color.Transparent)
                ),
            )
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(text = card.content, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey800, modifier = Modifier.fillMaxWidth())
            }
        } else {
            // v2: 사진 전체 + 텍스트는 하단 그라데이션 위에
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0.54f to Color.Transparent, 0.78f to Color(0xCC000000), 1f to Color.Black)
                ),
            )
            Box(
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
            ) {
                Text(text = card.content, style = BookiiBookiiTheme.typography.regular16, color = Color.White, modifier = Modifier.fillMaxWidth())
            }
        }

        ReactionOverlay(
            particles = particles,
            onParticleEnd = onParticleEnd,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = if (cardVersion == 1) 120.dp else 72.dp, start = 24.dp),
        )
    }
}

@Composable
private fun QuoteCard(
    card: ReadingCard,
    cardVersion: Int,
    particles: List<Particle>,
    onParticleEnd: (Particle) -> Unit,
) {
    // v1: 연한(pale) 그라데이션 + 짙은 텍스트
    // v2: 선명한(vivid) 그라데이션 + 흰 텍스트 (기존 디자인)
    val gradientColors = if (cardVersion == 1) {
        listOf(Color(0xFFFFC9A4), Color(0xFFFFEADB), Color.White)
    } else {
        listOf(Color(0xFFFF4E18), Color(0xFFFF7618), Color(0xFFFFC9A4))
    }
    val textColor = if (cardVersion == 1) Color(0xFFB34A00) else Color.White
    val quoteIconTint = if (cardVersion == 1) Color(0xFFFF7618) else Color.White

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(336f / 464f)
                    .background(
                        Brush.linearGradient(
                            colors = gradientColors,
                            start = Offset(0f, Float.POSITIVE_INFINITY),
                            end = Offset(Float.POSITIVE_INFINITY, 0f),
                        )
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(painter = painterResource(R.drawable.ic_quote), contentDescription = null, tint = quoteIconTint, modifier = Modifier.size(28.dp))
                    Text(text = "\"${card.content}\"", style = BookiiBookiiTheme.typography.semibold20, color = textColor, overflow = TextOverflow.Ellipsis)
                }
            }
            Box(modifier = Modifier.fillMaxWidth().weight(128f / 464f).padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(text = card.content, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey800, overflow = TextOverflow.Ellipsis)
            }
        }

        ReactionOverlay(
            particles = particles,
            onParticleEnd = onParticleEnd,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 24.dp, start = 24.dp),
        )
    }
}

// ... CardDetailHeader, CardInfoArea, CardVersionDot, ReactionBar, ShareBottomSheet, ShareOption, Preview 등 하단 생략 (이전 코드와 완전 동일) ...
// (전체 복사를 위해 아래에 기존 컴포넌트들도 모두 포함해 드립니다.)

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

@Composable
private fun CardInfoArea(card: ReadingCard?, sortByLatest: Boolean, progress: Float, bookTitle: String) {
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
                if (card?.isBookmarked == true) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(BookiiBookiiTheme.colors.uiMainPale).border(0.5.dp, BookiiBookiiTheme.colors.uiMain, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_bookmark_fill), contentDescription = null, tint = BookiiBookiiTheme.colors.uiMain, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = bookTitle, style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey800, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = card?.page ?: "", style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey500)
                }
                Text(text = card?.date ?: "", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
            }
        }
    }
}

/**
 * 카드 타입별 버전 도트
 * PHOTO: v1=그라데이션원(흰→주황), v2=반원(주황삼각+흰)
 * QUOTE: v1=연한T(페일 주황), v2=진한T(비비드 주황)
 */
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
                        // 사진 v1: 흰 → 주황 그라데이션 원
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
                        // 사진 v2: 우상단 주황 삼각 + 좌하단 흰 반원
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

// ShareBottomSheet → ReadingCardShareBottomSheet.kt

@Preview(showBackground = true)
@Composable
private fun ReadingCardDetailScreenPreview() {
    ReadingCardDetailScreen()
}