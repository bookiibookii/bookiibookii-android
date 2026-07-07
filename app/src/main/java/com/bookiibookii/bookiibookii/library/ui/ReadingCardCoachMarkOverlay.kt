package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val CoachMarkDim = Color(0xC2100F0E)
private val CoachMarkIndicatorActive = Color(0x80F4F3F1)
private val CoachMarkIndicatorInactive = Color(0x4DF4F3F1)
private const val COACH_MARK_TOTAL_STEPS = 4

private val coachMarkReactionIcons = listOf(
    R.drawable.ic_empathy,
    R.drawable.ic_good,
    R.drawable.ic_fun,
    R.drawable.ic_sad,
    R.drawable.ic_angry,
)

@Composable
fun ReadingCardCoachMarkOverlay(
    bookmarkTopLeft: Offset = Offset.Zero,
    shareTopLeft: Offset = Offset.Zero,
    onDismiss: () -> Unit,
) {
    var currentStep by remember { mutableStateOf(0) }

    fun advance() {
        if (currentStep < COACH_MARK_TOTAL_STEPS - 1) currentStep++
        else onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CoachMarkDim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { advance() },
    ) {
        // 단계 전환 시 콘텐츠 cross-fade
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(200))
            },
            modifier = Modifier.fillMaxSize(),
            label = "coach_mark_step",
        ) { step ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (step) {
                    0 -> CoachMarkStep1Content(modifier = Modifier.align(Alignment.BottomCenter))
                    1 -> CoachMarkStep2Content(bookmarkTopLeft = bookmarkTopLeft)
                    2 -> CoachMarkStep3Content(shareTopLeft = shareTopLeft)
                    3 -> CoachMarkStep4Content(modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        // 하단 고정: 인디케이터 + 버튼
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CoachMarkStepIndicator(currentStep = currentStep)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BookiiBookiiTheme.colors.uiMain)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { advance() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (currentStep < COACH_MARK_TOTAL_STEPS - 1) "다음" else "확인",
                    style = BookiiBookiiTheme.typography.regular16,
                    color = Color.White,
                )
            }
        }
    }
}

/**
 * Step 1: 공감 – 하단 중앙에 이모지 반응 소개
 * 이모지 아이콘이 순서대로 스프링 팝인 애니메이션으로 등장
 */
@Composable
private fun CoachMarkStep1Content(modifier: Modifier = Modifier) {
    // 아이콘마다 개별 scale 애니메이터
    val iconScales = remember { List(coachMarkReactionIcons.size) { Animatable(0f) } }
    LaunchedEffect(Unit) {
        iconScales.forEachIndexed { i, anim ->
            launch {
                delay(i * 70L)
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                )
            }
        }
    }

    Column(
        modifier = modifier.padding(bottom = 140.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = buildAnnotatedString {
                append("가장 가까운 반응으로\n독서카드에 ")
                withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) { append("공감") }
                append("해보세요.")
            },
            style = BookiiBookiiTheme.typography.medium18,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            coachMarkReactionIcons.forEachIndexed { index, iconRes ->
                Box(
                    modifier = Modifier
                        .scale(iconScales[index].value)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }
    }
}

/**
 * Step 2: 북마크 – 실제 북마크 버튼 위치에 하이라이트 + 펄스 링 표시 후 텍스트를 우측 정렬로 아래 배치
 * [bookmarkTopLeft] 는 루트 기준 북마크 Box의 좌상단 픽셀 좌표
 */
@Composable
private fun CoachMarkStep2Content(bookmarkTopLeft: Offset) {
    val pulseScale = remember { Animatable(1f) }
    val pulseAlpha = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        while (true) {
            launch { pulseAlpha.animateTo(0f, animationSpec = tween(900)) }
            pulseScale.animateTo(2f, animationSpec = tween(900))
            pulseScale.snapTo(1f)
            pulseAlpha.snapTo(0.6f)
        }
    }

    // 북마크 아이콘 하이라이트 + 펄스 링
    Box(
        modifier = Modifier
            .offset { IntOffset(bookmarkTopLeft.x.roundToInt(), bookmarkTopLeft.y.roundToInt()) }
            .size(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        // 펄스 링 (하이라이트 원 뒤에서 퍼져나감)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(pulseScale.value)
                .alpha(pulseAlpha.value)
                .clip(CircleShape)
                .background(BookiiBookiiTheme.colors.uiMain),
        )
        // 실제 하이라이트 원
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(BookiiBookiiTheme.colors.uiMainPale)
                .border(0.5.dp, BookiiBookiiTheme.colors.uiMain, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_bookmark_fill),
                contentDescription = "북마크",
                tint = BookiiBookiiTheme.colors.uiMain,
                modifier = Modifier.size(20.dp),
            )
        }
    }

    // 설명 텍스트 (북마크 아이콘 아래, 우측 정렬)
    Box(
        modifier = Modifier
            .offset { IntOffset(0, (bookmarkTopLeft.y + 32.dp.roundToPx() + 8.dp.roundToPx()).roundToInt()) }
            .fillMaxWidth()
            .padding(end = 16.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Text(
            text = buildAnnotatedString {
                append("모아서 보고 싶은\n독서카드를 ")
                withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) { append("북마크") }
                append("해요.")
            },
            style = BookiiBookiiTheme.typography.medium18,
            color = Color.White,
            textAlign = TextAlign.End,
        )
    }
}

/**
 * Step 3: 공유 – 실제 공유 버튼 위치에 하이라이트 + 펄스 링 표시 후 텍스트를 우측 정렬로 아래 배치
 * [shareTopLeft] 는 루트 기준 공유 IconButton의 좌상단 픽셀 좌표
 */
@Composable
private fun CoachMarkStep3Content(shareTopLeft: Offset) {
    val pulseScale = remember { Animatable(1f) }
    val pulseAlpha = remember { Animatable(0.5f) }
    LaunchedEffect(Unit) {
        while (true) {
            launch { pulseAlpha.animateTo(0f, animationSpec = tween(900)) }
            pulseScale.animateTo(2f, animationSpec = tween(900))
            pulseScale.snapTo(1f)
            pulseAlpha.snapTo(0.5f)
        }
    }

    // 공유 버튼 하이라이트 + 펄스 링
    Box(
        modifier = Modifier
            .offset { IntOffset(shareTopLeft.x.roundToInt(), shareTopLeft.y.roundToInt()) }
            .size(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        // 펄스 링
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(pulseScale.value)
                .alpha(pulseAlpha.value)
                .clip(CircleShape)
                .background(Color.White),
        )
        // 실제 하이라이트 원
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_share),
                contentDescription = "공유",
                tint = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.size(32.dp),
            )
        }
    }

    // 설명 텍스트 (공유 버튼 아래, 우측 정렬)
    Box(
        modifier = Modifier
            .offset { IntOffset(0, (shareTopLeft.y + 40.dp.roundToPx() + 8.dp.roundToPx()).roundToInt()) }
            .fillMaxWidth()
            .padding(end = 16.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Text(
            text = buildAnnotatedString {
                append("마음에 든 독서카드는\n")
                withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) { append("공유") }
                append("하거나 ")
                withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) { append("저장") }
                append("할 수 있어요.")
            },
            style = BookiiBookiiTheme.typography.medium18,
            color = Color.White,
            textAlign = TextAlign.End,
        )
    }
}

/**
 * Step 4: 스와이프 – 화살표는 고정, 손이 왼쪽으로 슬라이딩 반복
 * ic_swipe_gesture.xml: 화살표만 / ic_swipe_hand.xml: 손만
 */
@Composable
private fun CoachMarkStep4Content(modifier: Modifier = Modifier) {
    val handOffset = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(400)
        while (true) {
            // 왼쪽으로 스와이프
            handOffset.animateTo(-18f, animationSpec = tween(550, easing = FastOutSlowInEasing))
            delay(120)
            // 제자리로 복귀 (빠르게)
            handOffset.animateTo(0f, animationSpec = tween(300, easing = FastOutSlowInEasing))
            delay(700)
        }
    }

    Column(
        modifier = modifier.padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center,
        ) {
            // 화살표 (고정)
            Icon(
                painter = painterResource(R.drawable.ic_swipe_gesture),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxSize(),
            )
            // 손 (좌우 슬라이딩)
            Icon(
                painter = painterResource(R.drawable.ic_touch_hand),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = handOffset.value.dp),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = buildAnnotatedString {
                append("옆으로 스와이프해\n다른 독서카드를 ")
                withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) { append("넘겨 볼") }
                append(" 수 있어요.")
            },
            style = BookiiBookiiTheme.typography.medium18,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

/** 4단계 캐러셀 인디케이터 – 활성 dot 크기 변화에 애니메이션 적용 */
@Composable
private fun CoachMarkStepIndicator(currentStep: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(COACH_MARK_TOTAL_STEPS) { index ->
            val isActive = index == currentStep
            val width by animateDpAsState(
                targetValue = if (isActive) 8.dp else 20.dp,
                animationSpec = tween(300),
                label = "indicator_width_$index",
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(RoundedCornerShape(30.dp))
                    .background(if (isActive) CoachMarkIndicatorActive else CoachMarkIndicatorInactive),
            )
        }
    }
}
