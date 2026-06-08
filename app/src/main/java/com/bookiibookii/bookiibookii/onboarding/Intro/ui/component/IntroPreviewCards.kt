package com.bookiibookii.bookiibookii.onboarding.Intro.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// ─── 공통 래퍼 ───────────────────────────────────────────────────────────────

@Composable
private fun PreviewCardWrapper(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .width(300.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false,
                ambientColor = Color(0x1A000000),
                spotColor = Color(0x1A000000),
            )
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, BookiiBookiiTheme.colors.grey200, RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        content()
    }
}

// ─── 스플래시-3: 홈 ────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
private fun HomePreviewCardPreview() {
    BookiiBookiiTheme { HomePreviewCard() }
}

@Composable
fun HomePreviewCard() {
    PreviewCardWrapper {
        // 섹션 1: 신규 그룹
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            // 섹션 헤더
            Column(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "신규 그룹을 확인해보세요",
                    style = BookiiBookiiTheme.typography.medium14,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                Text(
                    text = "오늘 만들어진 따끈따끈한 그룹들만 모았어요",
                    style = BookiiBookiiTheme.typography.regular12,
                    color = BookiiBookiiTheme.colors.grey600,
                )
            }

            // 그룹 카드 슬라이더 — HorizontalPager로 카드 1장 단위 snap + 다음 카드 peek
            val groupPagerState = rememberPagerState(pageCount = { 2 })
            HorizontalPager(
                state = groupPagerState,
                pageSize = PageSize.Fixed(243.dp),
                pageSpacing = 8.dp,
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                when (page) {
                    0 -> GroupCard(
                        bookRes = R.drawable.intro_book_cant_bear,
                        profileRes = R.drawable.intro_profile_sayo,
                        bookTitle = "참을 수 없는 존재의 가벼움",
                        bookAuthor = "밀란 쿤데라 (소설)",
                        readingDays = "7일",
                        username = "sayo",
                        comment = "고전 완독하실 분 구해요",
                    )
                    else -> GroupCard(
                        bookRes = R.drawable.intro_book_boy,
                        profileRes = R.drawable.intro_profile_mus,
                        bookTitle = "소년이 온다",
                        bookAuthor = "한강 (소설)",
                        readingDays = "14일",
                        username = "무스",
                        comment = "같이 읽을 분 환영해요",
                    )
                }
            }

            // 페이지 인디케이터 (5개) — 카드 폭 기준 가운데 정렬
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
            ) {
                repeat(5) { i ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(6.dp)
                            .background(if (i == 0) BookiiBookiiTheme.colors.grey400 else BookiiBookiiTheme.colors.grey200),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp).background(BookiiBookiiTheme.colors.uiBg))

        // 섹션 2: 새로운 책
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "새로운 책이 도착했어요",
                    style = BookiiBookiiTheme.typography.medium14,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                Text(
                    text = "이번 달 신간들을 함께 읽어볼까요?",
                    style = BookiiBookiiTheme.typography.regular11,
                    color = BookiiBookiiTheme.colors.grey400,
                )
            }

            val books = listOf(
                Triple("안녕이라 그랬어", "김애란", R.drawable.intro_book_hello),
                Triple("급류", "정대건", R.drawable.intro_book_torrent),
                Triple("혼모노", "성해나", R.drawable.intro_book_honmono),
                Triple("괴테는 모든 것을 말했다", "스즈키 유이", R.drawable.intro_book_goethe),
                Triple("모순", "양귀자", R.drawable.intro_book_contradiction),
                Triple("할매", "황석영", R.drawable.intro_book_granny),
            )

            // 첫 번째 행
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                books.take(3).forEach { (title, author, res) ->
                    BookGridItem(
                        modifier = Modifier.weight(1f),
                        title = title,
                        author = author,
                        imageRes = res,
                    )
                }
            }

            // 두 번째 행
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                books.drop(3).forEach { (title, author, res) ->
                    BookGridItem(
                        modifier = Modifier.weight(1f),
                        title = title,
                        author = author,
                        imageRes = res,
                    )
                }
            }
        }
    }
}

// ─── 스플래시-4: 트래커 ──────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
private fun TrackerPreviewCardPreview() {
    BookiiBookiiTheme { TrackerPreviewCard() }
}

@Composable
fun TrackerPreviewCard() {
    PreviewCardWrapper {
        // 알림 보드
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white)
                .border(
                    width = 0.5.dp,
                    color = BookiiBookiiTheme.colors.grey100,
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                )
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Row {
                Text(
                    text = "sayo",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.uiMain,
                )
                Text(
                    text = "님의",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey900,
                )
            }
            Text(
                text = "교환독서 현황을 알려드려요",
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey900,
            )
        }

        // 알림 카드
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white)
                .padding(horizontal = 17.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "D-2",
                    style = BookiiBookiiTheme.typography.semibold14,
                    color = BookiiBookiiTheme.colors.uiMainSub,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) { i ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(6.dp)
                                .background(if (i == 0) BookiiBookiiTheme.colors.grey500 else BookiiBookiiTheme.colors.grey200),
                        )
                    }
                }
            }
            Row {
                Text(
                    text = "살인자의 기억법",
                    style = BookiiBookiiTheme.typography.regular11,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                Text(
                    text = "을 읽고 후기를 작성해주세요",
                    style = BookiiBookiiTheme.typography.regular11,
                    color = BookiiBookiiTheme.colors.grey700,
                )
            }
            Text(
                text = "독서카드를 남기면 교환독서가 더욱 즐거워져요",
                style = BookiiBookiiTheme.typography.regular10,
                color = BookiiBookiiTheme.colors.grey400,
            )
        }

        // 본문 (회색 배경)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.uiBg)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            // 카운트 보드
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BookiiBookiiTheme.colors.white)
                    .padding(9.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TrackerCountItem(label = "전체", count = "3", active = true)
                Box(modifier = Modifier.width(0.5.dp).height(35.dp).background(BookiiBookiiTheme.colors.grey200))
                TrackerCountItem(label = "읽는 중", count = "1", active = true)
                Box(modifier = Modifier.width(0.5.dp).height(35.dp).background(BookiiBookiiTheme.colors.grey200))
                TrackerCountItem(label = "교환 중", count = "2", active = true)
                Box(modifier = Modifier.width(0.5.dp).height(35.dp).background(BookiiBookiiTheme.colors.grey200))
                TrackerCountItem(label = "후기", count = "0", active = false)
            }

            // 트래커 카드
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BookiiBookiiTheme.colors.white)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "김영하 도장깨기 하실 분",
                            style = BookiiBookiiTheme.typography.medium12,
                            color = BookiiBookiiTheme.colors.grey800,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = "살인자의 기억법",
                                style = BookiiBookiiTheme.typography.regular11,
                                color = BookiiBookiiTheme.colors.grey500,
                            )
                            Text(
                                text = "·",
                                style = BookiiBookiiTheme.typography.regular11,
                                color = BookiiBookiiTheme.colors.grey500,
                            )
                            Text(
                                text = "읽는 중",
                                style = BookiiBookiiTheme.typography.regular11,
                                color = BookiiBookiiTheme.colors.grey500,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BookiiBookiiTheme.colors.grey100)
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "D-2",
                            style = BookiiBookiiTheme.typography.semibold10,
                            color = BookiiBookiiTheme.colors.grey700,
                        )
                    }
                }

                // 두 사람 칼럼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TrackerPersonColumn(
                        modifier = Modifier.weight(1f),
                        name = "나",
                        bookTitle = "살인자의 기억법",
                        bookCoverRes = R.drawable.intro_book_killer,
                        progress = 0.59f,
                        progressText = "59%",
                        showMyBookChip = true,
                    )
                    TrackerPersonColumn(
                        modifier = Modifier.weight(1f),
                        name = "noshel",
                        bookTitle = "작별인사",
                        bookCoverRes = R.drawable.intro_book_farewell,
                        progress = 0.30f,
                        progressText = "30%",
                        showMyBookChip = false,
                    )
                }

                // 액션 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(41.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(0.5.dp, BookiiBookiiTheme.colors.grey200, RoundedCornerShape(12.dp))
                            .background(BookiiBookiiTheme.colors.white),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "독서카드 작성",
                            style = BookiiBookiiTheme.typography.regular12,
                            color = BookiiBookiiTheme.colors.grey900,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(41.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BookiiBookiiTheme.colors.uiMain),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "진행률 기록",
                            style = BookiiBookiiTheme.typography.regular12,
                            color = BookiiBookiiTheme.colors.white,
                        )
                    }
                }
            }
        }
    }
}

// ─── 스플래시-5: 서재 ──────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
private fun LibraryPreviewCardPreview() {
    BookiiBookiiTheme { LibraryPreviewCard() }
}

@Composable
fun LibraryPreviewCard() {
    PreviewCardWrapper {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 책 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BookiiBookiiTheme.colors.white)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BookCoverImage(
                    imageRes = R.drawable.intro_book_hail_mary,
                    width = 74.dp,
                    height = 106.dp,
                    cornerRadius = 7.dp,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = "영화 보고 뒤늦게 책 읽는 모임",
                        style = BookiiBookiiTheme.typography.regular11,
                        color = BookiiBookiiTheme.colors.grey600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "프로젝트 헤일메리",
                        style = BookiiBookiiTheme.typography.semibold12,
                        color = BookiiBookiiTheme.colors.grey900,
                    )
                    Text(
                        text = "앤디 위어 (소설)",
                        style = BookiiBookiiTheme.typography.regular11,
                        color = BookiiBookiiTheme.colors.grey900,
                    )
                    // 별점 (4.5/5)
                    Row {
                        repeat(4) {
                            Text(text = "★", style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.uiMain)
                        }
                        Text(text = "☆", style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey300)
                    }
                    Text(
                        text = "2026. 05. 09. ~ 2026. 05. 31.",
                        style = BookiiBookiiTheme.typography.regular11,
                        color = BookiiBookiiTheme.colors.grey500,
                    )
                }
            }

            // 필터 바
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(BookiiBookiiTheme.colors.uiMainSubPale),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "✓",
                            style = BookiiBookiiTheme.typography.regular10,
                            color = BookiiBookiiTheme.colors.uiMainSub,
                        )
                    }
                    Text(
                        text = "내 독서카드만 보기",
                        style = BookiiBookiiTheme.typography.regular11,
                        color = BookiiBookiiTheme.colors.grey500,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(text = "최신순", style = BookiiBookiiTheme.typography.semibold12, color = BookiiBookiiTheme.colors.grey800)
                    Text(text = "|", style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey500)
                    Text(text = "페이지순", style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey500)
                }
            }

            // 독서카드 2×2 그리드
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // noshel 인용 카드 (왼쪽)
                ReadingCardQuote(
                    modifier = Modifier.weight(1f),
                    profileRes = R.drawable.intro_profile_noshel_lib2,
                    name = "noshel",
                    quote = "\"너랑 나는 좋은 사람.\" 로키가 말한다. \"그러게.\" 나는 미소 짓는다. \"그런 것 같아.\"",
                    page = "p.506",
                )
                // sayo 사진 카드 (오른쪽)
                ReadingCardText(
                    modifier = Modifier.weight(1f),
                    profileRes = R.drawable.intro_profile_sayo_lib,
                    name = "sayo",
                    text = "인생에서 나 지켜봐 줄 에리디언 너무 필요함",
                    page = "p.299",
                    photoRes = R.drawable.intro_reading_card_photo1,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // sayo 사진 카드 (왼쪽)
                ReadingCardText(
                    modifier = Modifier.weight(1f),
                    profileRes = R.drawable.intro_profile_sayo_lib4,
                    name = "sayo",
                    text = "나 행복. 너 안 죽음. 행성들을 구하자! 아름다워...",
                    page = "p.97",
                    photoRes = R.drawable.intro_reading_card_photo4,
                )
                // noshel 인용 카드 (오른쪽)
                ReadingCardQuote(
                    modifier = Modifier.weight(1f),
                    profileRes = R.drawable.intro_profile_noshel_lib3,
                    name = "noshel",
                    quote = "\"인간은 슬프면 눈에서 물이 흘러나와.\" \"알았다. 나는 네가 물이 새지 않을 때까지 지켜본다.\"",
                    page = "p.97",
                )
            }
        }
    }
}

// ─── 스플래시-6: 후기 ──────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
private fun ReviewPreviewCardPreview() {
    BookiiBookiiTheme { ReviewPreviewCard() }
}

@Composable
fun ReviewPreviewCard() {
    PreviewCardWrapper {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp),
        ) {
            // 독서 후기 카드 1 (그룹 후기 채팅)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BookiiBookiiTheme.colors.white)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 헤더
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                            .border(0.5.dp, BookiiBookiiTheme.colors.grey100, shape = RoundedCornerShape(0.dp)),
                    ) {
                        Column(modifier = Modifier.padding(bottom = 6.dp)) {
                            Text(
                                text = "김영하 도장깨기 하실 분",
                                style = BookiiBookiiTheme.typography.medium12,
                                color = BookiiBookiiTheme.colors.grey900,
                            )
                            Text(
                                text = "2026. 04. 29. ~ 2026. 05. 17.",
                                style = BookiiBookiiTheme.typography.regular11,
                                color = BookiiBookiiTheme.colors.grey500,
                            )
                        }
                    }
                }

                // noshel 버블 (오른쪽)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProfileCircleImage(imageRes = R.drawable.intro_profile_noshel_review, size = 15.dp)
                        Text(text = "noshel", style = BookiiBookiiTheme.typography.semibold10, color = BookiiBookiiTheme.colors.grey800)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .border(0.5.dp, BookiiBookiiTheme.colors.uiMain, CircleShape)
                                .background(BookiiBookiiTheme.colors.uiMainPale),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = "👍", style = BookiiBookiiTheme.typography.regular10, color = BookiiBookiiTheme.colors.uiMain)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(BookiiBookiiTheme.colors.grey100)
                                .padding(12.dp),
                        ) {
                            Text(
                                text = "같은 책을 읽었다는 것만으로 왠지 이 사람이 궁금해졌습니다. 이 책을 어떤 마음으로 골랐는지, 기회가 되면 여쭤보고 싶네요.",
                                style = BookiiBookiiTheme.typography.regular11,
                                color = BookiiBookiiTheme.colors.grey900,
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                }

                // sayo 버블 (왼쪽)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProfileCircleImage(imageRes = R.drawable.intro_profile_sayo_review, size = 15.dp)
                        Text(text = "sayo", style = BookiiBookiiTheme.typography.semibold10, color = BookiiBookiiTheme.colors.grey800)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BookiiBookiiTheme.colors.uiMainSubPale)
                            .padding(12.dp),
                    ) {
                        Text(
                            text = "이 책을 읽은 분이라면 분명 할 말이 많을 것 같아서, 한번쯤 이야기 나눠보고 싶다는 생각이 들었습니다.",
                            style = BookiiBookiiTheme.typography.regular11,
                            color = BookiiBookiiTheme.colors.grey900,
                        )
                    }
                }
            }

            // 독서 후기 카드 2 (책 별점 후기)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BookiiBookiiTheme.colors.white)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 책 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    BookCoverImage(
                        imageRes = R.drawable.intro_book_destroy,
                        width = 51.dp,
                        height = 73.dp,
                        cornerRadius = 8.dp,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "나는 나를 파괴할 권리가 있다",
                            style = BookiiBookiiTheme.typography.semibold12,
                            color = BookiiBookiiTheme.colors.grey900,
                        )
                        Text(
                            text = "김영하",
                            style = BookiiBookiiTheme.typography.medium12,
                            color = BookiiBookiiTheme.colors.grey700,
                        )
                    }
                }

                // noshel 후기 (오른쪽)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProfileCircleImage(imageRes = R.drawable.intro_profile_noshel_review, size = 15.dp)
                        Text(text = "noshel", style = BookiiBookiiTheme.typography.semibold10, color = BookiiBookiiTheme.colors.grey800)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BookiiBookiiTheme.colors.grey100)
                            .padding(12.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(text = "2026. 04. 05.", style = BookiiBookiiTheme.typography.regular11, color = BookiiBookiiTheme.colors.grey500)
                                StarRow(filledCount = 3)
                            }
                            Text(
                                text = "제목부터가 선전포고였다. 읽는 내내 불편했는데, 그 불편함이 오래 남는다.",
                                style = BookiiBookiiTheme.typography.regular11,
                                color = BookiiBookiiTheme.colors.grey900,
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                }

                // sayo 후기 (왼쪽)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProfileCircleImage(imageRes = R.drawable.intro_profile_sayo_review, size = 15.dp)
                        Text(text = "sayo", style = BookiiBookiiTheme.typography.semibold10, color = BookiiBookiiTheme.colors.grey800)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BookiiBookiiTheme.colors.grey100)
                            .padding(12.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                StarRow(filledCount = 4)
                                Text(text = "2026. 04. 15.", style = BookiiBookiiTheme.typography.regular11, color = BookiiBookiiTheme.colors.grey500)
                            }
                            Text(
                                text = "\"죽음을 직업으로 삼는 사람의 이야기인데, 읽는 내내 오히려 '살고 싶다'는 감각이 선명해졌다. 이상한 책이다, 좋은 의미로.\"",
                                style = BookiiBookiiTheme.typography.regular11,
                                color = BookiiBookiiTheme.colors.grey900,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── 공통 헬퍼 컴포저블 ───────────────────────────────────────────────────────

@Composable
private fun GroupCard(
    @DrawableRes bookRes: Int,
    @DrawableRes profileRes: Int,
    bookTitle: String,
    bookAuthor: String,
    readingDays: String,
    username: String,
    comment: String,
) {
    Column(
        modifier = Modifier
            .width(243.dp)
            .shadow(4.dp, RoundedCornerShape(14.dp), clip = false, spotColor = Color(0x0F000000))
            .clip(RoundedCornerShape(14.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookCoverImage(imageRes = bookRes, width = 52.dp, height = 73.dp, cornerRadius = 6.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ExchangeBadge(text = "택배")
                        Text(
                            text = bookTitle,
                            style = BookiiBookiiTheme.typography.medium12,
                            color = BookiiBookiiTheme.colors.grey900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = bookAuthor,
                        style = BookiiBookiiTheme.typography.regular11,
                        color = BookiiBookiiTheme.colors.grey500,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(text = "예상 독서 기간", style = BookiiBookiiTheme.typography.regular11, color = BookiiBookiiTheme.colors.grey700)
                        Text(text = readingDays, style = BookiiBookiiTheme.typography.regular11, color = BookiiBookiiTheme.colors.grey800)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProfileCircleImage(imageRes = profileRes, size = 15.dp)
                        Text(text = username, style = BookiiBookiiTheme.typography.regular11, color = BookiiBookiiTheme.colors.grey700)
                        Text(text = "·", style = BookiiBookiiTheme.typography.regular11, color = BookiiBookiiTheme.colors.grey700)
                        Text(
                            text = comment,
                            style = BookiiBookiiTheme.typography.regular11,
                            color = BookiiBookiiTheme.colors.grey500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(41.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(BookiiBookiiTheme.colors.uiMainPale),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "자세히 보기", style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.uiMain)
        }
    }
}

@Composable
private fun BookCoverImage(
    @DrawableRes imageRes: Int,
    width: Dp,
    height: Dp,
    cornerRadius: Dp = 6.dp,
) {
    Image(
        painter = painterResource(imageRes),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius)),
    )
}

@Composable
private fun ProfileCircleImage(
    @DrawableRes imageRes: Int,
    size: Dp,
) {
    Image(
        painter = painterResource(imageRes),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(size)
            .clip(CircleShape),
    )
}

@Composable
private fun ExchangeBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(BookiiBookiiTheme.colors.uiMainPale)
            .padding(horizontal = 3.dp, vertical = 1.5.dp),
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.regular10,
            color = BookiiBookiiTheme.colors.uiMain,
        )
    }
}

@Composable
private fun BookGridItem(
    modifier: Modifier = Modifier,
    title: String,
    author: String,
    @DrawableRes imageRes: Int,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(87.dp)
                .clip(RoundedCornerShape(7.dp)),
        )
        Text(
            text = title,
            style = BookiiBookiiTheme.typography.regular11,
            color = BookiiBookiiTheme.colors.grey900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = author,
            style = BookiiBookiiTheme.typography.regular11,
            color = BookiiBookiiTheme.colors.grey500,
        )
    }
}

@Composable
private fun TrackerCountItem(
    label: String,
    count: String,
    active: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = BookiiBookiiTheme.typography.regular11,
            color = BookiiBookiiTheme.colors.grey700,
        )
        Text(
            text = count,
            style = BookiiBookiiTheme.typography.regular18,
            color = if (active) BookiiBookiiTheme.colors.grey900 else BookiiBookiiTheme.colors.grey300,
        )
    }
}

@Composable
private fun TrackerPersonColumn(
    modifier: Modifier = Modifier,
    name: String,
    bookTitle: String,
    @DrawableRes bookCoverRes: Int,
    progress: Float,
    progressText: String,
    showMyBookChip: Boolean,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // 책 표지
        Box(contentAlignment = Alignment.BottomEnd) {
            BookCoverImage(imageRes = bookCoverRes, width = 73.dp, height = 96.dp, cornerRadius = 8.dp)
            if (showMyBookChip) {
                Box(
                    modifier = Modifier
                        .padding(3.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xBFE2E1DF))
                        .padding(horizontal = 3.dp, vertical = 1.5.dp),
                ) {
                    Text(
                        text = "내 책",
                        style = BookiiBookiiTheme.typography.regular10,
                        color = BookiiBookiiTheme.colors.grey900,
                    )
                }
            }
        }

        // 이름 + 책 제목
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = name,
                style = BookiiBookiiTheme.typography.regular11,
                color = BookiiBookiiTheme.colors.grey700,
            )
            Text(
                text = bookTitle,
                style = BookiiBookiiTheme.typography.medium12,
                color = BookiiBookiiTheme.colors.grey800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }

        // 진행률
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // 진행 바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(BookiiBookiiTheme.colors.grey200),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(3.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(BookiiBookiiTheme.colors.uiMain),
                )
            }
            Text(
                text = progressText,
                style = BookiiBookiiTheme.typography.regular11,
                color = BookiiBookiiTheme.colors.grey800,
            )
        }
    }
}

@Composable
private fun ReadingCardText(
    modifier: Modifier = Modifier,
    @DrawableRes profileRes: Int,
    name: String,
    text: String,
    page: String,
    @DrawableRes photoRes: Int,
) {
    Column(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BookiiBookiiTheme.colors.white),
    ) {
        // 상단 콘텐츠
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 9.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileCircleImage(imageRes = profileRes, size = 17.dp)
                    Text(text = name, style = BookiiBookiiTheme.typography.medium11, color = BookiiBookiiTheme.colors.grey800)
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(0.5.dp, BookiiBookiiTheme.colors.uiMain, CircleShape)
                        .background(BookiiBookiiTheme.colors.uiMainPale)
                        .padding(1.5.dp),
                ) {
                    Text(text = "🔖", style = BookiiBookiiTheme.typography.regular10)
                }
            }
            Text(
                text = text,
                style = BookiiBookiiTheme.typography.regular11,
                color = BookiiBookiiTheme.colors.grey800,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "♥", style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey400)
                Text(text = page, style = BookiiBookiiTheme.typography.regular11, color = BookiiBookiiTheme.colors.grey400)
            }
        }
        // 하단 사진 영역
        Image(
            painter = painterResource(photoRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(93.dp),
        )
    }
}

@Composable
private fun ReadingCardQuote(
    modifier: Modifier = Modifier,
    @DrawableRes profileRes: Int,
    name: String,
    quote: String,
    page: String,
) {
    Column(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BookiiBookiiTheme.colors.white),
    ) {
        // 상단 콘텐츠
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 9.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileCircleImage(imageRes = profileRes, size = 17.dp)
                    Text(text = name, style = BookiiBookiiTheme.typography.medium11, color = BookiiBookiiTheme.colors.grey800)
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(0.5.dp, BookiiBookiiTheme.colors.uiMain, CircleShape)
                        .background(BookiiBookiiTheme.colors.uiMainPale)
                        .padding(1.5.dp),
                ) {
                    Text(text = "🔖", style = BookiiBookiiTheme.typography.regular10)
                }
            }
            Text(
                text = quote.take(40) + "...",
                style = BookiiBookiiTheme.typography.regular11,
                color = BookiiBookiiTheme.colors.grey800,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "♥", style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey400)
                Text(text = page, style = BookiiBookiiTheme.typography.regular11, color = BookiiBookiiTheme.colors.grey400)
            }
        }
        // 하단 오렌지 그라디언트 인용구 (142.9° 대각선)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(93.dp),
        ) {
            val w = constraints.maxWidth.toFloat()
            val h = constraints.maxHeight.toFloat()
            // CSS 142.9° → direction (sin(142.9°), -cos(142.9°)) in screen space
            val vx = 0.6046f
            val vy = 0.7965f
            val len = w * vx + h * vy
            val cx = w / 2f
            val cy = h / 2f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFF4E18), Color(0xFFFF7618), Color(0xFFFFC9A4)),
                            start = Offset(cx - len / 2f * vx, cy - len / 2f * vy),
                            end = Offset(cx + len / 2f * vx, cy + len / 2f * vy),
                        )
                    )
                    .padding(6.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(text = "❝", style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.white)
                    Text(
                        text = quote,
                        style = BookiiBookiiTheme.typography.regular10,
                        color = BookiiBookiiTheme.colors.white,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun StarRow(filledCount: Int) {
    Row {
        repeat(5) { i ->
            Text(
                text = if (i < filledCount) "★" else "☆",
                style = BookiiBookiiTheme.typography.regular12,
                color = if (i < filledCount) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey300,
            )
        }
    }
}
