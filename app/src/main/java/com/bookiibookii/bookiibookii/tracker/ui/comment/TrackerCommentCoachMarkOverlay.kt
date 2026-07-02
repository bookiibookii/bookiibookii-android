package com.bookiibookii.bookiibookii.tracker.ui.comment

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.annotation.DrawableRes
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CoachMarkDim = Color(0xC2100F0E)
private val CoachMarkIndicatorActive = Color(0x80F4F3F1)
private val CoachMarkIndicatorInactive = Color(0x4DF4F3F1)
private const val TRACKER_COACH_MARK_TOTAL_STEPS = 3

@Composable
fun TrackerCommentCoachMarkOverlay(
    onDismiss: () -> Unit,
) {
    var currentStep by remember { mutableStateOf(0) }

    fun advance() {
        if (currentStep < TRACKER_COACH_MARK_TOTAL_STEPS - 1) currentStep++
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
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(200))
            },
            modifier = Modifier.fillMaxSize(),
            label = "tracker_coach_mark_step",
        ) { step ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (step) {
                    0 -> TrackerCoachMarkStep1Content(modifier = Modifier.align(Alignment.Center))
                    1 -> TrackerCoachMarkStep2Content(modifier = Modifier.align(Alignment.Center))
                    2 -> TrackerCoachMarkStep3Content(modifier = Modifier.fillMaxSize())
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
            TrackerCoachMarkStepIndicator(currentStep = currentStep)

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
                    text = if (currentStep < TRACKER_COACH_MARK_TOTAL_STEPS - 1) "다음" else "확인",
                    style = BookiiBookiiTheme.typography.regular16,
                    color = Color.White,
                )
            }
        }
    }
}

/**
 * Step 1: 탭하여 답장 / 꾹 눌러 삭제
 * - 팝인: 두 아이콘 순서대로 스프링 팝인
 * - 두 애니메이션 항상 동시 시작 (LP가 마스터 사이클 결정)
 * - 탭 아이콘: 손이 위로 탭 + 링이 ripple(scale+alpha) 반응 → 나머지 시간 대기
 * - 꾹 누르기 아이콘: 손이 아래로 누르고 유지 후 복귀 + 링 breath
 *
 * 사이클 타이밍 (총 2600ms):
 *   TAP  : 손 위(150ms) → 대기(80ms) → 손 복귀(200ms) → 대기(~2170ms)
 *   LP   : 손 아래(700ms) → 유지(900ms) → 손 복귀(400ms) → 여유(600ms)
 *   링 ripple (탭): scale 1.0→1.25→1.0, alpha 1.0→0.35→1.0 (총 500ms)
 *   링 breath (LP): alpha 1.0→0.2(700ms)→1.0(500ms)
 */
@Composable
private fun TrackerCoachMarkStep1Content(modifier: Modifier = Modifier) {
    val iconScales = remember { List(2) { Animatable(0f) } }
    // 탭 아이콘
    val tapHandOffsetY = remember { Animatable(0f) }
    val tapRingScale = remember { Animatable(1f) }
    val tapRingAlpha = remember { Animatable(1f) }
    // 꾹 누르기 아이콘
    val lpHandOffsetY = remember { Animatable(0f) }
    val lpRingScale = remember { Animatable(1f) }
    val lpRingAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // 팝인: 두 아이콘 순서대로 스프링 팝인
        iconScales.forEachIndexed { i, anim ->
            launch {
                delay(i * 150L)
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                )
            }
        }
        delay(900L)

        // 동기화 루프: 두 애니메이션 항상 동시에 시작
        while (true) {
            // 탭: 손·링 완전 동기화
            // 손 올라갈 때(150ms) → 링 동시에 확장+페이드
            // 손 내려올 때(200ms) → 링 동시에 원래대로 복귀
            launch {
                // 손 올라감 + 링 확장·페이드 동시 시작
                launch { tapHandOffsetY.animateTo(-6f, tween(150, easing = FastOutSlowInEasing)) }
                launch { tapRingScale.animateTo(1.25f, tween(150, easing = FastOutSlowInEasing)) }
                tapRingAlpha.animateTo(0.35f, tween(150, easing = FastOutSlowInEasing))
                delay(80)
                // 손 내려옴 + 링 복귀 동시 시작
                launch { tapHandOffsetY.animateTo(0f, tween(200, easing = FastOutSlowInEasing)) }
                launch { tapRingScale.animateTo(1f, tween(200, easing = FastOutSlowInEasing)) }
                tapRingAlpha.animateTo(1f, tween(200, easing = FastOutSlowInEasing))
            }
            // 꾹 누르기: 손·링 완전 동기화
            // 손 내려감(500ms) → 링 동시에 확장+페이드 → 유지(1100ms) → 손 올라옴 + 링 복귀(400ms)
            launch {
                // 누름: 손 올라감 + 링 확장·페이드 동시
                launch { lpHandOffsetY.animateTo(-6f, tween(500, easing = FastOutSlowInEasing)) }
                launch { lpRingScale.animateTo(1.3f, tween(500, easing = FastOutSlowInEasing)) }
                lpRingAlpha.animateTo(0.3f, tween(500, easing = FastOutSlowInEasing))
                // 꾹 누른 상태 유지 (탭보다 오래)
                delay(1100)
                // 떼기: 손 내려옴 + 링 복귀 동시
                launch { lpHandOffsetY.animateTo(0f, tween(400, easing = FastOutSlowInEasing)) }
                launch { lpRingScale.animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
                lpRingAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
            }
            // LP 전체 사이클(500+1100+400) + 여유(600) = 2600ms 후 재시작
            delay(2600)
        }
    }

    Column(
        modifier = modifier.padding(bottom = 160.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 탭 – 답장
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(iconScales[0].value),
                contentAlignment = Alignment.Center,
            ) {
                // 링 (탭 시 ripple: scale + alpha 애니메이션)
                Icon(
                    painter = painterResource(R.drawable.ic_touch_tap_ring),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(tapRingScale.value)
                        .alpha(tapRingAlpha.value),
                )
                // 손 (위로 탭 슬라이딩)
                Icon(
                    painter = painterResource(R.drawable.ic_touch_hand),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = tapHandOffsetY.value.dp),
                )
            }
            Text(
                text = buildAnnotatedString {
                    append("댓글을 탭하여 ")
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) { append("답장") }
                    append("하고,")
                },
                style = BookiiBookiiTheme.typography.medium18,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
        // 꾹 누르기 – 삭제
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(iconScales[1].value),
                contentAlignment = Alignment.Center,
            ) {
                // 링 (누름 시 확장+페이드, 뗄 때 복귀)
                Icon(
                    painter = painterResource(R.drawable.ic_touch_longpress_rings),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(lpRingScale.value)
                        .alpha(lpRingAlpha.value),
                )
                // 손 (아래로 꾹 누르기 슬라이딩)
                Icon(
                    painter = painterResource(R.drawable.ic_touch_hand),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = lpHandOffsetY.value.dp),
                )
            }
            Text(
                text = buildAnnotatedString {
                    append("꾹 눌러 ")
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) { append("삭제") }
                    append("하세요.")
                },
                style = BookiiBookiiTheme.typography.medium18,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Step 2: 위/아래 스크롤 칩 안내
 * 두 칩이 순서대로 스프링 팝인 애니메이션으로 등장
 */
@Composable
private fun TrackerCoachMarkStep2Content(modifier: Modifier = Modifier) {
    val chipScales = remember { List(2) { Animatable(0f) } }
    LaunchedEffect(Unit) {
        chipScales.forEachIndexed { i, anim ->
            launch {
                delay(i * 150L)
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
        modifier = modifier.padding(bottom = 160.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 위 버튼 (chevron 90° = 위 방향)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .scale(chipScales[0].value)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, BookiiBookiiTheme.colors.grey200, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron),
                    contentDescription = null,
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer { rotationZ = 90f },
                )
            }
            Text(
                text = buildAnnotatedString {
                    append("위 버튼은 ")
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) { append("오래된 댓글") }
                    append("로,")
                },
                style = BookiiBookiiTheme.typography.medium18,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
        // 아래 버튼 (chevron -90° = 아래 방향)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .scale(chipScales[1].value)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, BookiiBookiiTheme.colors.grey200, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron),
                    contentDescription = null,
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer { rotationZ = -90f },
                )
            }
            Text(
                text = buildAnnotatedString {
                    append("아래 버튼은 ")
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) { append("최신 댓글") }
                    append("로 이동해요.")
                },
                style = BookiiBookiiTheme.typography.medium18,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Step 3: 위로 당겨 새로고침
 * - 상단: 예시 댓글 카드 — 손이 올라갈 때 카드도 아래로 내려오며 연동
 * - 카드 바로 아래: pull-up 아이콘 (화살표 고정 + 손 위로 슬라이딩)
 */
@Composable
private fun TrackerCoachMarkStep3Content(modifier: Modifier = Modifier) {
    val handOffsetY = remember { Animatable(0f) }
    val cardOffsetY = remember { Animatable(-600f) }

    LaunchedEffect(Unit) {
        delay(400)
        while (true) {
            // 손이 아래로 내려가면서 카드가 화면 밖(위)에서 천천히 내려옴 (동시에)
            launch { cardOffsetY.animateTo(-230f, animationSpec = tween(900, easing = FastOutSlowInEasing)) }
            handOffsetY.animateTo(18f, animationSpec = tween(700, easing = FastOutSlowInEasing))
            delay(900)
            // 함께 원위치: 카드 위로 사라지고 손 올라옴
            launch { cardOffsetY.animateTo(-600f, animationSpec = tween(500, easing = FastOutSlowInEasing)) }
            handOffsetY.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            delay(600)
        }
    }

    // Box 구조: 카드(뒤) → 아이콘+텍스트(앞) 순서로 렌더링
    // 피그마와 동일하게 아이콘이 항상 카드 위에 표시됨
    Box(modifier = modifier) {
        // 예시 댓글 카드 — 상단 오버레이, 위에서 슬라이딩 (아이콘 뒤에 렌더링)
        ExampleCommentCard(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 68.dp, start = 16.dp, end = 16.dp)
                .offset(y = cardOffsetY.value.dp),
        )

        // 아이콘 + 텍스트 — 화면 전체 기준 중앙 (카드 앞에 렌더링)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center,
            ) {
                // 화살표 (고정)
                Icon(
                    painter = painterResource(R.drawable.ic_touch_pullup_arrow),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxSize(),
                )
                // 손 (위로 슬라이딩)
                Icon(
                    painter = painterResource(R.drawable.ic_touch_hand),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = handOffsetY.value.dp),
                )
            }
            Text(
                text = buildAnnotatedString {
                    append("위로 당기면\n")
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) { append("최신 댓글") }
                    append("을 불러와요.")
                },
                style = BookiiBookiiTheme.typography.medium18,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private data class ExampleComment(
    val nickname: String,
    val isHost: Boolean,
    val time: String,
    val text: String,
    @DrawableRes val profileRes: Int,
)

private val exampleComments = listOf(
    ExampleComment("sayo", false, "55분 전", "질문이 있습니다!", R.drawable.img_coach_profile_kanghunshim),
    ExampleComment("sayo", false, "55분 전", "저는 이번 교환독서에서\n'스릴러' 장르를 읽고 싶어요. 참고 부탁드려요!", R.drawable.img_coach_profile_sayo),
    ExampleComment("sayo", false, "55분 전", "이번 교환독서는\n포스트잇을 적극적으로 활용해주세요", R.drawable.img_coach_profile_sayo),
    ExampleComment("Kanghunshim", false, "55분 전", "만나서 반갑습니다!", R.drawable.img_coach_profile_kanghunshim),
    ExampleComment("noshel", true, "방금", "우리 즐거운 교환독서해요!", R.drawable.img_coach_profile_noshel),
)

@Composable
private fun ExampleCommentCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(12.dp),
    ) {
        exampleComments.forEachIndexed { index, comment ->
            ExampleCommentItem(comment)
            if (index < exampleComments.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = BookiiBookiiTheme.colors.grey100,
                )
            }
        }
    }
}

private val ExampleProfileSquircleShape = GenericShape { size, _ ->
    val sx = size.width / 44f
    val sy = size.height / 44f
    moveTo(0f, 22f * sy)
    cubicTo(0f, 3.883f * sy, 3.883f * sx, 0f, 22f * sx, 0f)
    cubicTo(40.117f * sx, 0f, size.width, 3.883f * sy, size.width, 22f * sy)
    cubicTo(size.width, 40.117f * sy, 40.117f * sx, size.height, 22f * sx, size.height)
    cubicTo(3.883f * sx, size.height, 0f, 40.117f * sy, 0f, 22f * sy)
    close()
}

@Composable
private fun ExampleCommentItem(comment: ExampleComment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(comment.profileRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(ExampleProfileSquircleShape),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.nickname,
                    style = BookiiBookiiTheme.typography.regular14,
                    color = if (comment.isHost) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.uiMainSub,
                )
                Text(
                    text = comment.time,
                    style = BookiiBookiiTheme.typography.regular12,
                    color = BookiiBookiiTheme.colors.grey500,
                )
            }
            Text(
                text = comment.text,
                style = BookiiBookiiTheme.typography.regular15,
                color = BookiiBookiiTheme.colors.grey700,
            )
        }
    }
}

/** 3단계 인디케이터 – 활성 dot 크기 변화에 애니메이션 적용 */
@Composable
private fun TrackerCoachMarkStepIndicator(currentStep: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(TRACKER_COACH_MARK_TOTAL_STEPS) { index ->
            val isActive = index == currentStep
            val width by animateDpAsState(
                targetValue = if (isActive) 8.dp else 20.dp,
                animationSpec = tween(300),
                label = "tracker_indicator_width_$index",
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
