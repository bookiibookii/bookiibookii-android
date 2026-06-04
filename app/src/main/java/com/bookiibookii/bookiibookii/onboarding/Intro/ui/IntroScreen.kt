package com.bookiibookii.bookiibookii.onboarding.Intro.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

private const val PAGE_COUNT = 6

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Page 0 – 로고")
@Composable
private fun PreviewPage0() { BookiiBookiiTheme { IntroScreen(onStart = {}, initialPage = 0) } }

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Page 1 – 슬로건")
@Composable
private fun PreviewPage1() { BookiiBookiiTheme { IntroScreen(onStart = {}, initialPage = 1) } }

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Page 2 – 홈")
@Composable
private fun PreviewPage2() { BookiiBookiiTheme { IntroScreen(onStart = {}, initialPage = 2) } }

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Page 3 – 트래커")
@Composable
private fun PreviewPage3() { BookiiBookiiTheme { IntroScreen(onStart = {}, initialPage = 3) } }

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Page 4 – 서재")
@Composable
private fun PreviewPage4() { BookiiBookiiTheme { IntroScreen(onStart = {}, initialPage = 4) } }

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Page 5 – 시작")
@Composable
private fun PreviewPage5() { BookiiBookiiTheme { IntroScreen(onStart = {}, initialPage = 5) } }

@Composable
fun IntroScreen(onStart: () -> Unit, initialPage: Int = 0) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { PAGE_COUNT })
    var startVisible by remember { mutableStateOf(initialPage == PAGE_COUNT - 1) }

    LaunchedEffect(Unit) {
        if (initialPage != 0) return@LaunchedEffect
        repeat(PAGE_COUNT - 1) { index ->
            delay(2500L)
            pagerState.animateScrollToPage(
                page = index + 1,
                animationSpec = tween(durationMillis = 500),
            )
        }
        delay(350L)
        startVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        // ── Pager: 페이지별 고유 콘텐츠만 (로고·푸터는 바깥 오버레이) ──────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                when (page) {
                    // 스플래시-1: 로고 정중앙 (300dp — Figma 기준)
                    0 -> Image(
                        painter = painterResource(R.drawable.ic_logo_wordmark),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(BookiiBookiiTheme.colors.uiMain),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(300.dp),
                    )

                    // 스플래시-2: 텍스트만 중앙 (로고는 오버레이)
                    1 -> Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "읽고, 교환하고, 기록해요.",
                            style = BookiiBookiiTheme.typography.medium24,
                            color = BookiiBookiiTheme.colors.uiMain,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "부키부키",
                            style = BookiiBookiiTheme.typography.bold24,
                            color = BookiiBookiiTheme.colors.uiMain,
                            textAlign = TextAlign.Center,
                        )
                    }

                    // 스플래시-3~6: 텍스트 + 카드 (로고·푸터는 오버레이)
                    else -> {
                        // 페이지 텍스트
                        Text(
                            text = when (page) {
                                2 -> "오늘은 누구와\n어떤 책으로 만나볼까요?"
                                3 -> "책을 교환하는 모든 순간을\n단계별로 관리해요."
                                4 -> "서로의 문장을 공유하며\n넓어지는 둘만의 서재"
                                else -> "지금 부키부키에서\n나와 꼭 맞는 독서 파트너를 찾아보세요!"
                            },
                            style = BookiiBookiiTheme.typography.semibold20,
                            color = BookiiBookiiTheme.colors.uiMain,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                                .padding(top = 121.dp),
                        )

                        // 프리뷰 카드 (화면 하단에서 피킹)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                                .padding(top = 239.dp),
                        ) {
                            when (page) {
                                2 -> HomePreviewCard()
                                3 -> TrackerPreviewCard()
                                4 -> LibraryPreviewCard()
                                else -> ReviewPreviewCard()
                            }
                        }
                    }
                }
            }
        }

        // ── 오버레이: 상단 로고 (페이지 1~5 고정 — 슬라이드 시 움직이지 않음) ──
        if (pagerState.currentPage >= 1) {
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

        // ── 오버레이: 하단 푸터 104dp (페이지 2~5 고정 — 카드 하단 가림) ────────
        if (pagerState.currentPage >= 2) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(104.dp)
                    .background(BookiiBookiiTheme.colors.uiBg),
                contentAlignment = Alignment.Center,
            ) {
                // 마지막 페이지: 시작 버튼
                if (pagerState.currentPage == PAGE_COUNT - 1) {
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
                                style = BookiiBookiiTheme.typography.semibold18,
                                color = BookiiBookiiTheme.colors.white,
                            )
                        }
                    }
                }
            }
        }
    }
}
