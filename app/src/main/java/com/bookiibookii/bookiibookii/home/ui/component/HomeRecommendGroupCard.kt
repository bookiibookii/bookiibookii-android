package com.bookiibookii.bookiibookii.home.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import android.graphics.BlurMaskFilter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.data.model.group.GroupItem
import com.bookiibookii.bookiibookii.ui.component.BookCover
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.common.stripBookSubtitle
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun RecommendGroupRow(
    groups: List<GroupItem>,
    onGroupClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { groups.size })

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(334.dp),
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 5.dp),
            // 한 번 스와이프 = 한 장씩(Pager 기본) + 느리고 묵직한 스냅으로 무게감.
            // 더 빠르게/덜 묵직하게 하려면 stiffness를 올리면 됨(StiffnessLow→MediumLow→Medium).
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapAnimationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            ),
        ) { page ->
            val group = groups[page]
            HomeRecommendGroupCard(
                group = group,
                onClick = { onGroupClick(group.groupId) },
            )
        }

        // 페이지 인디케이터 dot — 카드가 2개 이상일 때만 노출
        if (groups.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                groups.indices.forEach { index ->
                    val isSelected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) BookiiBookiiTheme.colors.grey400
                                else BookiiBookiiTheme.colors.grey200,
                            ),
                    )
                }
            }
        }
    }
}

// 피그마: 334dp wide, drop shadow(0 0 5px rgba(0,0,0,0.06)), round20, p=16dp
@Composable
private fun HomeRecommendGroupCard(
    group: GroupItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography
    val shape = BookiiBookiiTheme.shape.round20

    Column(
        modifier = modifier
            .width(334.dp)
            .drawBehind {
                drawIntoCanvas { canvas ->
                    val paint = Paint()
                    paint.asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = Color(0x0F000000).toArgb()
                        maskFilter = BlurMaskFilter(5.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.drawRoundRect(
                        0f, 0f, size.width, size.height,
                        20.dp.toPx(), 20.dp.toPx(),
                        paint,
                    )
                }
            }
            .clip(shape)
            .background(colors.white)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Info 영역: 책 표지 + 상세 정보 — 피그마: gap=16dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BookCover(
                imageUrl = group.bookImage,
                aladinCoverSize = "cover200",
                modifier = Modifier.size(width = 72.dp, height = 100.dp),
            )

            // 우측 정보 컬럼 — 피그마: h=100dp, SpaceBetween
            Column(
                modifier = Modifier
                    .height(100.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // 상단: 교환방식 뱃지 + 제목 + 저자(장르)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val badgeLabel = tradeTypeLabel(group.tradeType)
                        if (badgeLabel.isNotBlank()) {
                            RecommendExchangeBadge(text = badgeLabel)
                        }
                        Text(
                            text = group.title.stripBookSubtitle(),
                            style = typography.medium16,
                            color = colors.grey900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    val authorGenre = buildString {
                        group.author?.let { append(it) }
                        group.genre?.let { append("($it)") }
                    }
                    if (authorGenre.isNotBlank()) {
                        Text(
                            text = authorGenre,
                            style = typography.regular14,
                            color = colors.grey500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // 하단: 독서 기간 + 호스트 정보
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // 피그마: "예상 독서 기간"(grey700) + "7"(grey800) + "일"(grey700)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "예상 독서 기간",
                            style = typography.regular14,
                            color = colors.grey700,
                        )
                        Row {
                            Text(
                                text = group.readingPeriod.toString(),
                                style = typography.regular14,
                                color = colors.grey800,
                            )
                            Text(
                                text = "일",
                                style = typography.regular14,
                                color = colors.grey700,
                            )
                        }
                    }
                    // 피그마: 프로필 이미지 + 호스트 닉네임 + "·" + 그룹명
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProfilePlaceholder(
                            modifier = Modifier.size(20.dp),
                            imageUrl = group.hostProfileImageUrl,
                        )
                        Text(
                            text = group.hostNickname.orEmpty(),
                            style = typography.regular15,
                            color = colors.grey700,
                        )
                        Text(
                            text = "·",
                            style = typography.regular15,
                            color = colors.grey700,
                        )
                        Text(
                            text = group.groupName.orEmpty(),
                            style = typography.regular15,
                            color = colors.grey500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        // 자세히 보기 버튼 — 피그마: h=48dp, round16, uiMainPale bg, regular15 orange
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(BookiiBookiiTheme.shape.round16)
                .background(colors.uiMainPale),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "자세히 보기",
                style = typography.regular15,
                color = colors.uiMain,
            )
        }
    }
}

private fun tradeTypeLabel(tradeType: String?): String = when (tradeType) {
    "DIRECT" -> "직접"
    "DELIVERY" -> "택배"
    else -> tradeType.orEmpty()
}

// 교환방식 뱃지 — 피그마: uiMainPale bg, uiMain text, round8, px=4dp py=2dp
@Composable
private fun RecommendExchangeBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(BookiiBookiiTheme.shape.round8)
            .background(BookiiBookiiTheme.colors.uiMainPale)
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.medium11,
            color = BookiiBookiiTheme.colors.uiMain,
        )
    }
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

private val mockGroup = GroupItem(
    groupId = 1L, groupName = "책과 함께", title = "소년이 온다", author = "한강",
    genre = "한국소설", bookImage = null, hostNickname = "부키", hostProfileImageUrl = null,
    groupStatus = "RECRUITING", currentCount = 2, maxCapacity = 5, waitingCount = 0,
    isHot = false, tradeType = "직접", readingPeriod = 7, pictureBadge = null,
)
private val mockRecommendedGroups = listOf(
    mockGroup,
    mockGroup.copy(groupId = 2L, title = "채식주의자"),
    mockGroup.copy(groupId = 3L, title = "아몬드"),
)

@Preview(showBackground = true, name = "HomeRecommendGroupCard - 단일 카드")
@Composable
private fun HomeRecommendGroupCardPreview() {
    BookiiPreview {
        Box(Modifier.padding(16.dp)) {
            HomeRecommendGroupCard(
                group = mockRecommendedGroups.first(),
                onClick = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "RecommendGroupRow - 목록")
@Composable
private fun RecommendGroupRowPreview() {
    BookiiPreview {
        RecommendGroupRow(
            groups = mockRecommendedGroups,
            onGroupClick = {},
        )
    }
}
