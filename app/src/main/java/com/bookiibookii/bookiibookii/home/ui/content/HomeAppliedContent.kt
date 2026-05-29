package com.bookiibookii.bookiibookii.home.ui.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.data.model.group.GroupItem
import com.bookiibookii.bookiibookii.home.ui.component.HomeGroupCard
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

internal fun LazyListScope.homeAppliedContent(
    appliedGroups: List<GroupItem>,
    onGroupClick: (Long) -> Unit,
) {
    // 피그마: 탭 영역 ~ 첫 섹션 사이 8dp 회색 간격
    item { Box(Modifier.fillMaxWidth().height(8.dp)) }

    // 피그마: 외부 컨테이너 px=16dp, py=12dp, gap=12dp
    item {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // "N 권" 라벨 (regular14, grey900)
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = appliedGroups.size.toString(),
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                Text(
                    text = " 권",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey900,
                )
            }

            // 안내 문구
            Text(
                text = "매칭 대기 중인 그룹만 노출됩니다.",
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey500,
            )

            if (appliedGroups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "신청한 그룹이 없어요",
                        style = BookiiBookiiTheme.typography.regular15,
                        color = BookiiBookiiTheme.colors.grey400,
                    )
                }
            } else {
                // 피그마: 신청한 그룹 카드에는 상태 텍스트 없음 (showStatus = false)
                appliedGroups.forEach { group ->
                    HomeGroupCard(
                        group = group,
                        onClick = { onGroupClick(group.groupId) },
                        showStatus = false,
                    )
                }
            }
        }
    }

    // 하단 여백
    item { Box(Modifier.height(80.dp)) }
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

private val mockGroup = GroupItem(
    groupId = 1L,
    groupName = "책과 함께 그룹",
    title = "살인자의 기억법",
    author = "김영하",
    genre = "한국소설",
    bookImage = null,
    hostNickname = "부키유저",
    hostProfileImageUrl = null,
    groupStatus = "RECRUITING",
    currentCount = 2,
    maxCapacity = 5,
    waitingCount = 1,
    isHot = false,
    tradeType = "직접",
    readingPeriod = 7,
    pictureBadge = null,
)

private val mockAppliedGroups = listOf(
    mockGroup,
    mockGroup.copy(groupId = 2L, title = "채식주의자", tradeType = "택배"),
    mockGroup.copy(groupId = 3L, title = "작별인사", tradeType = "직접"),
)

@Preview(showBackground = true, name = "HomeAppliedContent - 데이터 있음")
@Composable
private fun HomeAppliedContentPreview() {
    BookiiPreview {
        LazyColumn {
            homeAppliedContent(
                appliedGroups = mockAppliedGroups,
                onGroupClick = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "HomeAppliedContent - 빈 상태")
@Composable
private fun HomeAppliedContentEmptyPreview() {
    BookiiPreview {
        LazyColumn {
            homeAppliedContent(
                appliedGroups = emptyList(),
                onGroupClick = {},
            )
        }
    }
}
