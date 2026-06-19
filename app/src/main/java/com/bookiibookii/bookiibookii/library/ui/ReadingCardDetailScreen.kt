package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.component.BottomSheetBtnStyle
import com.bookiibookii.bookiibookii.ui.component.BottomSheetTwoBtnShort
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.common.stripBookSubtitle
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.bookiibookii.bookiibookii.ui.theme.MaruBuri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

// ── 데이터 ──────────────────────────────────────────────────────────────────

private data class Reaction(
    val iconRes: Int,
    val label: String,
)

// 5종 이모지 리액션 (기존 6종 → 5종 개편)
private val reactionList = listOf(
    Reaction(R.drawable.ic_empathy, "공감해요"),
    Reaction(R.drawable.ic_good,    "좋아요"),
    Reaction(R.drawable.ic_fun,     "웃겨요"),
    Reaction(R.drawable.ic_sad,     "슬퍼요"),
    Reaction(R.drawable.ic_angry,   "화나요"),
)

// 리액션 라벨 → API 키 (서버 전송용).
// "화나요"는 기존 6종에 없던 신규 라벨로, 더 이상 쓰지 않는 "힘나요"의 CHEERUP 키를 재사용한다.
private val reactionToApiKey = mapOf(
    "공감해요" to "FEELYOU",
    "좋아요"  to "LIKE",
    "웃겨요"  to "FUN",
    "슬퍼요"  to "SAD",
    "화나요"  to "CHEERUP",
)

// API 키 → Reaction 객체 (myReactions 초기 활성 상태 복원용)
private val apiKeyToReaction: Map<String, Reaction> by lazy {
    mapOf(
        "FEELYOU" to reactionList[0],
        "LIKE"    to reactionList[1],
        "FUN"     to reactionList[2],
        "SAD"     to reactionList[3],
        "CHEERUP" to reactionList[4],
    )
}

private data class Particle(
    val id: String,
    val iconResId: Int,
    val sizeDp: Float,
    val targetY: Float,
    val rotation: Float,
)

// ── 메인 스크린 ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingCardDetailScreen(
    cards: List<ReadingCard> = emptyList(),
    initialIndex: Int = 0,
    sortByLatest: Boolean = true,
    onBackClick: () -> Unit = {},
    onBookmarkToggle: (cardId: Long) -> Unit = {},
    onReactionToggle: (cardId: Long, reaction: String) -> Unit = { _, _ -> },
    // 인스타/다운로드는 화면에서 직접 렌더링한 카드를 캡처하므로, 지금 보고 있는 cardVersion을 그대로 전달해 일치시킨다.
    onInstaShare: (card: ReadingCard, cardVersion: Int) -> Unit = { _, _ -> },
    onCopyLink: (card: ReadingCard) -> Unit = {},
    onKakaoShare: (card: ReadingCard) -> Unit = {},
    onXShare: (card: ReadingCard) -> Unit = {},
    onDownload: (card: ReadingCard, cardVersion: Int) -> Unit = { _, _ -> },
    onEditClick: (card: ReadingCard) -> Unit = {},
    onDeleteConfirmed: (card: ReadingCard) -> Unit = {},
) {
    val pagerState    = rememberPagerState(initialPage = initialIndex) { cards.size }
    val coroutineScope = rememberCoroutineScope()
    var showShareSheet  by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var cardVersion     by remember { mutableStateOf(1) }

    // 카드별 북마크 상태 (낙관적 업데이트)
    val bookmarkStates = remember(cards) {
        mutableStateListOf(*Array(cards.size) { i -> cards.getOrNull(i)?.isBookmarked ?: false })
    }

    // 리액션 + 파티클 상태
    var activeReactionList by remember { mutableStateOf(listOf<Reaction>()) }
    val particles          = remember { mutableStateListOf<Particle>() }

    LaunchedEffect(pagerState.currentPage) {
        // myReactions(API 키 목록)에서 초기 활성 리액션 복원
        val card = cards.getOrNull(pagerState.currentPage)
        activeReactionList = card?.myReactions
            ?.mapNotNull { apiKeyToReaction[it] }
            ?: emptyList()
        particles.clear()
        cardVersion = 1
    }

    val currentCard     = cards.getOrNull(pagerState.currentPage)
    val progress        = if (cards.isNotEmpty()) (pagerState.currentPage + 1).toFloat() / cards.size else 1f
    val currentBookmark = bookmarkStates.getOrElse(pagerState.currentPage) { false }

    Column(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.uiBg)) {

        CardDetailHeader(
            onBackClick = onBackClick,
            onShareClick = { showShareSheet = true },
            showMenu = currentCard?.isMine == true,
            onEditClick = { currentCard?.let(onEditClick) },
            onDeleteClick = { showDeleteDialog = true },
        )

        CardInfoArea(
            card             = currentCard,
            sortByLatest     = sortByLatest,
            progress         = progress,
            isBookmarked     = currentBookmark,
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
                state          = pagerState,
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing    = 8.dp,
                modifier       = Modifier.fillMaxSize(),
            ) { page ->
                val card          = cards[page]
                val isCurrentPage = page == pagerState.currentPage
                ReadingCardDetailItem(
                    card        = card,
                    cardVersion = if (isCurrentPage) cardVersion else 1,
                    particles   = if (isCurrentPage) particles else emptyList(),
                    onParticleEnd = { particles.remove(it) },
                    modifier    = Modifier.fillMaxHeight().padding(vertical = 8.dp),
                )
            }
        }

        // 버전 선택 도트 2개 — 카드 타입별 디자인
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            val currentType = currentCard?.type ?: ReadingCardType.PHOTO
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CardVersionDot(version = 1, cardType = currentType, isSelected = cardVersion == 1) { cardVersion = 1 }
                CardVersionDot(version = 2, cardType = currentType, isSelected = cardVersion == 2) { cardVersion = 2 }
            }
        }

        ReactionBar(
            activeReactions = activeReactionList,
            onReact = { reaction ->
                val label           = reaction.label
                val isAlreadyActive = activeReactionList.any { it.label == label }

                if (isAlreadyActive) {
                    activeReactionList = activeReactionList.filter { it.label != label }
                } else {
                    activeReactionList = activeReactionList + reaction
                    // 비활성 → 활성 시에만 파티클 발생
                    coroutineScope.launch {
                        val burstCount     = (5..8).random()
                        val recentReactions = activeReactionList.takeLast(2)
                        val pool           = if (recentReactions.isNotEmpty()) recentReactions else listOf(reaction)
                        repeat(burstCount) {
                            delay((10..50).random().toLong())
                            val r = pool.random()
                            particles.add(
                                Particle(
                                    id        = UUID.randomUUID().toString(),
                                    iconResId = r.iconRes,
                                    sizeDp    = (28..44).random().toFloat(),
                                    targetY   = -(140..300).random().toFloat(),
                                    rotation  = (-20..20).random().toFloat(),
                                )
                            )
                        }
                    }
                }
                val apiKey = reactionToApiKey[label] ?: label
                currentCard?.cardId?.let { onReactionToggle(it, apiKey) }
            },
            modifier = Modifier.navigationBarsPadding().padding(bottom = 20.dp),
        )
    }

    if (showShareSheet) {
        // 시트 자체가 슬라이드다운 애니메이션이 끝난 뒤 onDismiss를 호출하므로 여기서 바로
        // showShareSheet = false를 하지 않는다 (클릭 즉시 끄면 닫히는 애니메이션이 생략됨)
        ReadingCardShareBottomSheet(
            onDismiss    = { showShareSheet = false },
            onKakaoClick = { currentCard?.let { onKakaoShare(it) } },
            onInstaClick = { currentCard?.let { onInstaShare(it, cardVersion) } },
            onXClick     = { currentCard?.let { onXShare(it) } },
            onDownloadClick = { currentCard?.let { onDownload(it, cardVersion) } },
            onCopyLinkClick = { currentCard?.let { onCopyLink(it) } },
        )
    }

    if (showDeleteDialog) {
        Dialog(onDismissRequest = { showDeleteDialog = false }) {
            ReadingCardDeleteDialog(
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    showDeleteDialog = false
                    currentCard?.let(onDeleteConfirmed)
                },
            )
        }
    }
}

// 독서카드 삭제 확인 다이얼로그 (그룹 삭제 다이얼로그와 동일 패턴)
@Composable
private fun ReadingCardDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round24)
            .background(BookiiBookiiTheme.colors.white)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "독서카드 삭제",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BookiiBookiiTheme.colors.grey100)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = "닫기",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = "독서카드를 삭제하시겠습니까? 삭제하면 즉시 사라지며, 이후 되돌릴 수 없습니다.",
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey900,
            modifier = Modifier.padding(top = 24.dp),
        )
        Row(
            modifier = Modifier.padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BottomSheetTwoBtnShort(
                text = "취소",
                style = BottomSheetBtnStyle.White,
                textStyle = BookiiBookiiTheme.typography.regular16,
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            BottomSheetTwoBtnShort(
                text = "삭제",
                style = BottomSheetBtnStyle.Red,
                textStyle = BookiiBookiiTheme.typography.regular16,
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ── 파티클 애니메이션 ─────────────────────────────────────────────────────────

// 인스타 스토리 더블탭 하트처럼: 튕기듯 팝업(스프링) → 살짝 회전하며 위로 떠오르다 페이드아웃
@Composable
private fun FloatingParticle(particle: Particle, onAnimationEnd: (Particle) -> Unit) {
    val scale      = remember { Animatable(0f) }
    val rotation   = remember { Animatable(0f) }
    val translateY = remember { Animatable(0f) }
    val alpha      = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(1.2f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
        }
        launch {
            rotation.animateTo(particle.rotation, animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing))
        }
        launch {
            delay(120)
            translateY.animateTo(particle.targetY, animationSpec = tween(durationMillis = 1300, easing = LinearOutSlowInEasing))
        }
        launch {
            delay(850)
            alpha.animateTo(0f, animationSpec = tween(durationMillis = 650))
        }
        delay(1550)
        onAnimationEnd(particle)
    }

    Box(
        modifier = Modifier
            .offset(y = translateY.value.dp)
            .rotate(rotation.value)
            .scale(scale.value)
            .alpha(alpha.value)
            .size(particle.sizeDp.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter            = painterResource(particle.iconResId),
            contentDescription = null,
            tint               = Color.Unspecified,
            modifier           = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun BoxScope.ReactionOverlay(
    particles: List<Particle>,
    onParticleEnd: (Particle) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        particles.forEach { particle ->
            key(particle.id) {
                FloatingParticle(particle = particle, onAnimationEnd = { onParticleEnd(it) })
            }
        }
    }
}

// ── 카드 아이템 ──────────────────────────────────────────────────────────────

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
            ReadingCardType.PHOTO -> PhotoCard(card, cardVersion, particles, onParticleEnd)
            ReadingCardType.QUOTE -> QuoteCard(card, cardVersion, particles, onParticleEnd)
        }
    }
}

// ── PHOTO 카드 ────────────────────────────────────────────────────────────────
// v1 = Type A (shareFragment.txt): 상단 이미지 + 책 제목 배지 오버레이, 하단 메모
// v2 = Type B (shareFragment.txt): 전체 이미지 + 상단 흰 그라디언트, 좌상단 책 제목
// 상세 화면에서는 닉네임/B로고를 표시하지 않음 (공유용 ShareableCard에만 표시)

// 사진 카드 v2 상단 화이트 그라데이션: 11%까지 흰색 100%, 41%에서 90%, 100%에서 0%로 페이드
private val photoCardTopGradient = Brush.verticalGradient(
    0f to Color.White,
    0.11f to Color.White,
    0.41f to Color.White.copy(alpha = 0.9f),
    1f to Color.White.copy(alpha = 0f),
)
private const val PHOTO_GRADIENT_HEIGHT_FRACTION = 0.55f

@Composable
private fun PhotoCard(
    card: ReadingCard,
    cardVersion: Int,
    particles: List<Particle>,
    onParticleEnd: (Particle) -> Unit,
) {
    if (cardVersion == 1) {
        // v1 (Type A)
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 상단 이미지 영역
                Box(
                    modifier = Modifier.fillMaxWidth().weight(336f / 464f).background(BookiiBookiiTheme.colors.grey300),
                ) {
                    if (!card.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model              = card.imageUrl,
                            contentDescription = null,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.matchParentSize(),
                        )
                    }
                    // 책 제목은 상세 화면에선 숨김 (공유 카드에만 표시)
                }
                // 하단 흰 텍스트 영역
                Box(
                    modifier = Modifier.fillMaxWidth().weight(128f / 464f),
                ) {
                    // 메모 텍스트 (좌상단)
                    if (card.content.isNotBlank()) {
                        Text(
                            text = card.content,
                            style = BookiiBookiiTheme.typography.regular16,
                            color = BookiiBookiiTheme.colors.grey800,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp).align(Alignment.TopStart),
                        )
                    }
                }
            }
            ReactionOverlay(particles, onParticleEnd, Modifier.align(Alignment.BottomStart).padding(bottom = 128.dp, start = 24.dp))
        }
    } else {
        // v2 (Type B): 전체 이미지 + 텍스트 오버레이
        Box(modifier = Modifier.fillMaxSize()) {
            // 전체 이미지 배경
            Box(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.grey300)) {
                if (!card.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model              = card.imageUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.matchParentSize(),
                    )
                }
            }
            // 상단 흰 그라디언트 오버레이
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(PHOTO_GRADIENT_HEIGHT_FRACTION).background(photoCardTopGradient))
            // 책 제목은 상세 화면에선 숨김 (공유 카드에만 표시)
            // 메모 텍스트 (좌상단)
            if (card.content.isNotBlank()) {
                Text(
                    text = card.content,
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey800,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 52.dp, end = 20.dp),
                )
            }
            ReactionOverlay(particles, onParticleEnd, Modifier.align(Alignment.BottomStart).padding(bottom = 56.dp, start = 24.dp))
        }
    }
}

// ── QUOTE 카드 ────────────────────────────────────────────────────────────────
// v1: 흰→연한주황 그라데이션, 주황 텍스트 — 좌상단 책 제목(배지)
// v2: 진한 주황 그라데이션, 흰 텍스트   — 좌상단 책 제목(흰)
// 상세 화면에서는 닉네임을 표시하지 않음 (공유용 ShareableCard에만 표시)

@Composable
private fun QuoteCard(
    card: ReadingCard,
    cardVersion: Int,
    particles: List<Particle>,
    onParticleEnd: (Particle) -> Unit,
) {
    val quotationText = card.quotation.ifBlank { card.content }

    val topGradient = if (cardVersion == 1) {
        Brush.linearGradient(
            colors = listOf(Color.White, Color(0xFFFFC9A4)),
            start  = Offset(0f, 0f),
            end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFFFF4E18), Color(0xFFFF7618), Color(0xFFFFC9A4)),
            start  = Offset(0f, 0f),
            end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    }
    // 따옴표 아이콘: 항상 main_150 (3번 요구사항)
    val iconTint  = BookiiBookiiTheme.colors.uiMain150
    val textColor = if (cardVersion == 1) BookiiBookiiTheme.colors.uiMain else Color.White

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 상단 컬러 영역 (텍스트 영역)
            Box(
                modifier = Modifier.fillMaxWidth().weight(336f / 464f).background(topGradient),
            ) {
                Column(
                    modifier            = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    // 책 제목은 상세 화면에선 숨김 (공유 카드에만 표시)
                    Icon(painter = painterResource(R.drawable.ic_quote), contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
                    Text(text = "“$quotationText”", style = TextStyle(fontFamily = MaruBuri, fontWeight = FontWeight.Bold, fontSize = 20.sp), color = textColor, overflow = TextOverflow.Ellipsis)
                }
            }
            // 하단 메모 영역 (상세 화면에서는 닉네임 미표시 — 공유용 ShareableCard에만 표시)
            Box(
                modifier = Modifier.fillMaxWidth().weight(128f / 464f),
            ) {
                // 메모 텍스트 (좌상단)
                if (card.content.isNotBlank()) {
                    Text(
                        text     = card.content,
                        style    = BookiiBookiiTheme.typography.regular16,
                        color    = BookiiBookiiTheme.colors.grey800,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 16.dp, end = 20.dp),
                    )
                }
            }
        }
        ReactionOverlay(particles, onParticleEnd, Modifier.align(Alignment.BottomStart).padding(bottom = 24.dp, start = 24.dp))
    }
}

// ── 공유용 카드 (오프스크린 비트맵 캡처, 오버레이 없음) ─────────────────────
// 실제 카드 디자인과 동일 (책 제목·닉네임·B로고 포함)

@Composable
internal fun ShareableCard(card: ReadingCard, cardVersion: Int = 2, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(BookiiBookiiTheme.colors.white)) {
        when (card.type) {
            ReadingCardType.PHOTO -> if (cardVersion == 1) {
                // v1: 상단 이미지 + 책 제목 배지, 하단 메모 + 닉네임(우하단)
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(336f / 464f).background(BookiiBookiiTheme.colors.grey300)) {
                        if (!card.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(card.imageUrl)
                                    .allowHardware(false)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize(),
                            )
                        }
                        if (card.bookTitle.isNotBlank()) {
                            Box(modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 20.dp, end = 40.dp)) {
                                BookTitleChip(title = card.bookTitle, solidBackground = true)
                            }
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().weight(128f / 464f)) {
                        if (card.content.isNotBlank()) {
                            Text(card.content, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey800, maxLines = 4, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 20.dp, end = 20.dp))
                        }
                        if (card.username.isNotBlank()) {
                            Text(card.username, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey400,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 16.dp))
                        }
                    }
                }
            } else {
                // v2: 전체 이미지 + 상단 흰 그라디언트, 좌하단 닉네임 + 우하단 B로고
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.grey300)) {
                        if (!card.imageUrl.isNullOrBlank()) {
                            // allowHardware(false): 소프트웨어 Canvas로 캡처(공유/다운로드)하려면 하드웨어 비트맵 비활성 필요
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(card.imageUrl)
                                    .allowHardware(false)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize(),
                            )
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(PHOTO_GRADIENT_HEIGHT_FRACTION).background(photoCardTopGradient))
                    if (card.bookTitle.isNotBlank()) {
                        Box(modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 20.dp, end = 40.dp)) {
                            BookTitleChip(title = card.bookTitle, solidBackground = true)
                        }
                    }
                    if (card.content.isNotBlank()) {
                        Text(card.content, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey800, maxLines = 4, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 52.dp, end = 20.dp))
                    }
                    if (card.username.isNotBlank()) {
                        Text("by. ${card.username}", style = BookiiBookiiTheme.typography.regular14, color = Color.White,
                            modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 20.dp))
                    }
                    Text("B", color = Color.White.copy(alpha = 0.85f), fontSize = 20.sp, fontWeight = FontWeight.Black,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 18.dp))
                }
            }
            ReadingCardType.QUOTE -> {
                val quotationText = card.quotation.ifBlank { card.content }
                val gradient = if (cardVersion == 1) {
                    Brush.linearGradient(
                        colors = listOf(Color.White, Color(0xFFFFC9A4)),
                        start  = Offset(0f, 0f),
                        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFF4E18), Color(0xFFFF7618), Color(0xFFFFC9A4)),
                        start  = Offset(0f, 0f),
                        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    )
                }
                val iconTint  = BookiiBookiiTheme.colors.uiMain150
                val textColor = if (cardVersion == 1) BookiiBookiiTheme.colors.uiMain else Color.White
                Column(modifier = Modifier.fillMaxSize()) {
                    // 상단 그라디언트 — 남는 공간을 모두 채움(하단 영역이 content만큼 차지하고 남은 만큼)
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(gradient)) {
                        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Spacer(modifier = Modifier.height(20.dp))
                            if (card.bookTitle.isNotBlank()) {
                                BookTitleChip(title = card.bookTitle, solidBackground = cardVersion != 1)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Icon(painter = painterResource(R.drawable.ic_quote), contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
                            Text("“$quotationText”", style = TextStyle(fontFamily = MaruBuri, fontWeight = FontWeight.Bold, fontSize = 20.sp), color = textColor, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    // 하단 — content 길이만큼 아래로 늘어남(말줄임 없음), username은 content 바로 아래 우측 정렬
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
                        if (card.content.isNotBlank()) {
                            Text(card.content, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey800)
                        }
                        if (card.username.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("by. ${card.username}", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey400,
                                modifier = Modifier.align(Alignment.End))
                        }
                    }
                }
            }
        }
    }
}

// ── 버전 선택 도트 — 카드 타입별 디자인 ─────────────────────────────────────
// PHOTO v1: 흰→주황 그라디언트 원 / PHOTO v2: 우상단 주황 삼각 분할
// QUOTE v1: 연한 주황 T          / QUOTE v2: 진한 주황 T

@Composable
private fun CardVersionDot(
    version: Int,
    cardType: ReadingCardType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) Color(0x99100F0E) else Color(0x66E2E1DF)

    Box(
        modifier = Modifier.size(46.dp).clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).border(2.dp, borderColor, CircleShape),
        ) {
            when (cardType) {
                ReadingCardType.PHOTO -> {
                    if (version == 1) {
                        // 사진 v1: 흰→주황 그라디언트
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.White, Color(0xFFFF7618)),
                                    start  = Offset(0f, 0f),
                                    end    = Offset(size.width, size.height),
                                )
                            )
                        }
                    } else {
                        // 사진 v2: 좌상단→우하단 대각, 좌상단=main, 우하단=main_pale
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(color = Color(0xFFFFC9A4)) // main_150(main_pale) 배경
                            drawPath(
                                path = Path().apply {
                                    moveTo(0f, 0f)           // 좌상단
                                    lineTo(size.width, 0f)   // 우상단
                                    lineTo(0f, size.height)  // 좌하단
                                    close()
                                },
                                color = Color(0xFFFF7618),   // main 색상 (좌상단 삼각)
                            )
                        }
                    }
                }
                ReadingCardType.QUOTE -> {
                    // v1: main_pale 배경 + main T / v2: main 배경 + 흰 T
                    val bgColor  = if (version == 1) BookiiBookiiTheme.colors.uiMainPale else Color(0xFFFF7618)
                    val txtColor = if (version == 1) BookiiBookiiTheme.colors.uiMain else Color.White
                    Box(
                        modifier         = Modifier.fillMaxSize().background(bgColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "T", color = txtColor, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                    }
                }
            }
        }
    }
}

// ── 헤더 ─────────────────────────────────────────────────────────────────────

@Composable
private fun CardDetailHeader(
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    showMenu: Boolean = false,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white)) {
        Box(modifier = Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 16.dp)) {
            BookiiBackButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart))
            Text(
                text = "독서카드",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.align(Alignment.Center),
            )
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onShareClick, modifier = Modifier.size(40.dp)) {
                    Icon(painter = painterResource(R.drawable.ic_share), contentDescription = "공유", tint = BookiiBookiiTheme.colors.grey900, modifier = Modifier.size(32.dp))
                }
                if (showMenu) {
                    CardDetailEditMenu(onEditClick = onEditClick, onDeleteClick = onDeleteClick)
                }
            }
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 1.dp)
    }
}

// 미트볼 아이콘 + 메뉴 팝오버 (내 카드일 때만 노출). 바깥 탭/항목 선택 시 닫힘
@Composable
private fun CardDetailEditMenu(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val popupOffsetY = with(LocalDensity.current) { 56.dp.roundToPx() }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(40.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_meetball),
                contentDescription = "더보기",
                tint = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.size(32.dp),
            )
        }
        if (expanded) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(x = 0, y = popupOffsetY),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
            ) {
                CardDetailMenuPopover(
                    onEditClick = {
                        expanded = false
                        onEditClick()
                    },
                    onDeleteClick = {
                        expanded = false
                        onDeleteClick()
                    },
                )
            }
        }
    }
}

// 수정하기/삭제하기 카드 팝오버
@Composable
private fun CardDetailMenuPopover(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(160.dp)
            .shadow(elevation = 6.dp, shape = BookiiBookiiTheme.shape.round10)
            .background(color = BookiiBookiiTheme.colors.white, shape = BookiiBookiiTheme.shape.round10)
            .border(width = 1.dp, color = BookiiBookiiTheme.colors.grey200, shape = BookiiBookiiTheme.shape.round10)
            .padding(vertical = 4.dp),
    ) {
        CardDetailMenuItem(text = "수정하기", iconRes = R.drawable.ic_edit, onClick = onEditClick)
        HorizontalDivider(thickness = 1.dp, color = BookiiBookiiTheme.colors.grey100)
        CardDetailMenuItem(text = "삭제하기", iconRes = R.drawable.ic_trash, onClick = onDeleteClick)
    }
}

@Composable
private fun CardDetailMenuItem(text: String, iconRes: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = text, style = BookiiBookiiTheme.typography.medium14, color = BookiiBookiiTheme.colors.grey700)
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = BookiiBookiiTheme.colors.grey700,
            modifier = Modifier.size(24.dp),
        )
    }
}

// ── 카드 정보 영역 ────────────────────────────────────────────────────────────

@Composable
private fun CardInfoArea(
    card: ReadingCard?,
    sortByLatest: Boolean,
    progress: Float,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
) {
    val pageText = card?.page?.let { if (it.isNotBlank() && it != "0") "p.$it" else "" } ?: ""

    Column(
        modifier              = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white).padding(16.dp),
        verticalArrangement   = Arrangement.spacedBy(16.dp),
    ) {
        // 진행도 바 + 정렬 표시
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(30.dp)).background(BookiiBookiiTheme.colors.grey200)) {
                Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(BookiiBookiiTheme.colors.uiMain))
            }
            Text(text = "| ${if (sortByLatest) "최신순" else "페이지순"}", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // 프로필 + 닉네임 + 북마크 버튼
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProfilePlaceholder(imageUrl = card?.creatorProfileImageUrl, modifier = Modifier.size(32.dp))
                    Text(text = card?.username ?: "", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey800)
                }
                // 북마크 버튼 — 항상 표시, 상태에 따라 스타일 변경
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isBookmarked) BookiiBookiiTheme.colors.uiMainPale else Color.Transparent)
                        .border(0.5.dp, if (isBookmarked) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey300, CircleShape)
                        .clickable { onBookmarkToggle() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter            = painterResource(if (isBookmarked) R.drawable.ic_bookmark_fill else R.drawable.ic_bookmark),
                        contentDescription = "북마크",
                        tint               = if (isBookmarked) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey500,
                        modifier           = Modifier.size(20.dp),
                    )
                }
            }

            // 책 제목 + 페이지 + 날짜
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier              = Modifier.weight(1f),
                ) {
                    Text(text = (card?.bookTitle).stripBookSubtitle(), style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey800, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (pageText.isNotBlank()) {
                        Text(text = pageText, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey500)
                    }
                }
                Text(text = card?.date ?: "", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
            }
        }
    }
}

// ── 리액션 바 ─────────────────────────────────────────────────────────────────

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
                modifier            = Modifier.clickable { onReact(reaction) },
            ) {
                // 이모지 아이콘 자체가 풀컬러라 배경은 항상 흰 원, 선택 시 테두리로 강조
                Box(
                    modifier = Modifier
                        .size(63.dp)
                        .clip(CircleShape)
                        .background(BookiiBookiiTheme.colors.white)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey200,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painter = painterResource(reaction.iconRes), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(40.dp))
                }
                Text(text = reaction.label, style = BookiiBookiiTheme.typography.medium12, color = BookiiBookiiTheme.colors.grey800)
            }
        }
    }
}

private val previewCards = listOf(
    ReadingCard(
        cardId = 1L,
        username = "북이",
        content = "다시 읽어도 마음에 오래 남는 문장이었다.",
        page = "123",
        type = ReadingCardType.QUOTE,
        date = "2026.06.05",
        bookTitle = "데미안",
        quotation = "새는 알에서 나오려고 투쟁한다.",
        isBookmarked = true,
        myReactions = listOf("LIKE"),
    ),
    ReadingCard(
        cardId = 2L,
        username = "부키",
        content = "이 장면이 특히 인상 깊었어요.",
        page = "45",
        type = ReadingCardType.PHOTO,
        date = "2026.06.04",
        bookTitle = "어린 왕자",
    ),
)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReadingCardDetailScreenQuotePreview() {
    BookiiPreview {
        ReadingCardDetailScreen(cards = previewCards, initialIndex = 0)
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReadingCardDetailScreenPhotoPreview() {
    BookiiPreview {
        ReadingCardDetailScreen(cards = previewCards, initialIndex = 1)
    }
}
