package com.bookiibookii.bookiibookii.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.data.model.group.GroupItem
import com.bookiibookii.bookiibookii.ui.component.BookCover
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun RecommendGroupRow(
    groups: List<GroupItem>,
    onGroupClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(groups, key = { it.groupId }) { group ->
            HomeRecommendGroupCard(
                group = group,
                onClick = { onGroupClick(group.groupId) },
            )
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
            .shadow(
                elevation = 2.dp,
                shape = shape,
                ambientColor = Color(0x0F000000),
                spotColor = Color(0x0F000000),
            )
            .clip(shape)
            .background(colors.white)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Info 영역: 책 표지 + 제목
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookCover(
                imageUrl = group.bookImage,
                modifier = Modifier.size(width = 72.dp, height = 100.dp),
            )
            Text(
                text = group.title,
                style = typography.medium16,
                color = colors.grey900,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
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
