package com.bookiibookii.bookiibookii.onboarding.Intro.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.onboarding.Intro.ui.component.HomePreviewCard
import com.bookiibookii.bookiibookii.onboarding.Intro.ui.component.LibraryPreviewCard
import com.bookiibookii.bookiibookii.onboarding.Intro.ui.component.ReviewPreviewCard
import com.bookiibookii.bookiibookii.onboarding.Intro.ui.component.TrackerPreviewCard
import androidx.compose.ui.tooling.preview.Preview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.delay

// 페이지: 0=로고, 1=슬로건1, 2=슬로건2, 3=홈, 4=트래커, 5=서재, 6=후기+시작
private const val PAGE_COUNT = 7

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Page 1 – 로고")
@Composable
private fun PreviewPage0() { BookiiBookiiTheme { IntroScreen(onStart = {}, initialPage = 0) } }

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Page 2 – 슬로건1")
@Composable
private fun PreviewPage1() { BookiiBookiiTheme { IntroScreen(onStart = {}, initialPage = 1) } }

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Page 3 – 슬로건2")
@Composable
private fun PreviewPage2() { BookiiBookiiTheme { IntroScreen(onStart = {}, initialPage = 2) } }

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Page 4 – 홈")
@Composable
private fun PreviewPage3() { BookiiBookiiTheme { IntroScreen(onStart = {}, initialPage = 3) } }

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Page 5 – 트래커")
@Composable
private fun PreviewPage4() { BookiiBookiiTheme { IntroScreen(onStart = {}, initialPage = 4) } }

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Page 6 – 서재")
@Composable
private fun PreviewPage5() { BookiiBookiiTheme { IntroScreen(onStart = {}, initialPage = 5) } }

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Page 7 – 시작")
@Composable
private fun PreviewPage6() { BookiiBookiiTheme { IntroScreen(onStart = {}, initialPage = 6) } }

@Composable
fun IntroScreen(onStart: () -> Unit, initialPage: Int = 0) {
    var currentPage by remember { mutableIntStateOf(initialPage) }
    var startVisible by remember { mutableStateOf(initialPage == PAGE_COUNT - 1) }

    LaunchedEffect(Unit) {
        if (initialPage != 0) return@LaunchedEffect
        // Page 1: 로고 (2500ms)
        delay(2500L)
        // Page 2: 슬로건1 (2000ms)
        currentPage = 1
        delay(2000L)
        // Page 3: 슬로건2 (2000ms)
        currentPage = 2
        delay(2000L)
        // Page 4~7: 프리뷰 카드 (2500ms 간격)
        for (page in 3 until PAGE_COUNT) {
            currentPage = page
            if (page < PAGE_COUNT - 1) delay(2500L)
        }
        delay(350L)
        startVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        // ── Crossfade: 페이드 인/아웃 전환 ───────────────────────────────────────
        Crossfade(
            targetState = currentPage,
            animationSpec = tween(durationMillis = 500),
            label = "introPageFade",
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (page) {
                    // Page 1: 로고 정중앙
                    0 -> Image(
                        painter = painterResource(R.drawable.ic_logo_wordmark),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(BookiiBookiiTheme.colors.uiMain),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(300.dp),
                    )

                    // Page 2: 슬로건1 — "읽고, 교환하고, 기록해요." (SUITE Medium 24)
                    1 -> Text(
                        text = "읽고, 교환하고, 기록해요.",
                        style = BookiiBookiiTheme.typography.suiteMedium24,
                        color = BookiiBookiiTheme.colors.uiMain,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center),
                    )

                    // Page 3: 슬로건2 — "부키부키에서" (Bold+Regular 혼합) + "교환독서 파트너를 찾아보세요!" (Medium)
                    2 -> Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row {
                            Text(
                                text = "부키부키",
                                style = BookiiBookiiTheme.typography.suiteBold24,
                                color = BookiiBookiiTheme.colors.uiMain,
                            )
                            Text(
                                text = "에서",
                                style = BookiiBookiiTheme.typography.suiteRegular24,
                                color = BookiiBookiiTheme.colors.uiMain,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "교환독서 파트너를 찾아보세요!",
                            style = BookiiBookiiTheme.typography.suiteMedium24,
                            color = BookiiBookiiTheme.colors.uiMain,
                            textAlign = TextAlign.Center,
                        )
                    }

                    // Page 4~7: 텍스트 + 프리뷰 카드 — 로고·푸터는 오버레이
                    else -> {
                        Text(
                            text = when (page) {
                                3 -> "오늘은 누구와\n어떤 책으로 만나볼까요?"
                                4 -> "책을 교환하는 모든 순간을\n단계별로 관리해요."
                                5 -> "서로의 문장을 공유하며\n넓어지는 우리만의 서재"
                                else -> "지금 부키부키에서\n나와 꼭 맞는 독서 파트너를 찾아보세요!"
                            },
                            style = BookiiBookiiTheme.typography.suiteSemibold20,
                            color = BookiiBookiiTheme.colors.grey900,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                                .padding(top = 121.dp),
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                                .padding(top = 239.dp),
                        ) {
                            when (page) {
                                3 -> HomePreviewCard()
                                4 -> TrackerPreviewCard()
                                5 -> LibraryPreviewCard()
                                else -> ReviewPreviewCard()
                            }
                        }
                    }
                }
            }
        }

        // ── 오버레이: 상단 로고 (Page 2~7 고정) ──────────────────────────────────
        if (currentPage >= 1) {
            Image(
                painter = painterResource(R.drawable.ic_logo_wordmark),
                contentDescription = null,
                colorFilter = ColorFilter.tint(BookiiBookiiTheme.colors.uiMain),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 36.dp)
                    .width(204.dp),
            )
        }

        // ── 오버레이: 하단 푸터 104dp (Page 4~7) ─────────────────────────────────
        if (currentPage >= 3) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(104.dp)
                    .background(BookiiBookiiTheme.colors.uiBg),
                contentAlignment = Alignment.Center,
            ) {
                // Page 7: 시작 버튼
                if (currentPage == PAGE_COUNT - 1) {
                    AnimatedVisibility(
                        visible = startVisible,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(BookiiBookiiTheme.colors.grey900)
                                .clickable(
                                    onClick = onStart,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "시작",
                                style = BookiiBookiiTheme.typography.medium18,
                                color = BookiiBookiiTheme.colors.white,
                            )
                        }
                    }
                }
            }
        }
    }
}
