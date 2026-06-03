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

internal fun LazyListScope.homeMyGroupsContent(
    myGroups: List<GroupItem>,
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
            // "N 권" 라벨 — 피그마: gap=4dp, h=20dp, regular14, grey900
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(20.dp),
            ) {
                Text(
                    text = myGroups.size.toString(),
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                Text(
                    text = "권",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey900,
                )
            }

            if (myGroups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "참여 중인 그룹이 없어요",
                        style = BookiiBookiiTheme.typography.regular15,
                        color = BookiiBookiiTheme.colors.grey400,
                    )
                }
            } else {
                // 카드들 — white bg, round20은 HomeGroupCard 내부에 적용
                myGroups.forEach { group ->
                    HomeGroupCard(
                        group = group,
                        onClick = { onGroupClick(group.groupId) },
                        showStatus = true,
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

private val mockMyGroups = listOf(
    mockGroup,
    mockGroup.copy(groupId = 2L, groupStatus = "MATCHED", tradeType = "택배", title = "채식주의자"),
)

@Preview(showBackground = true, name = "HomeMyGroupsContent - 데이터 있음")
@Composable
private fun HomeMyGroupsContentPreview() {
    BookiiPreview {
        LazyColumn {
            homeMyGroupsContent(
                myGroups = mockMyGroups,
                onGroupClick = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "HomeMyGroupsContent - 빈 상태")
@Composable
private fun HomeMyGroupsContentEmptyPreview() {
    BookiiPreview {
        LazyColumn {
            homeMyGroupsContent(
                myGroups = emptyList(),
                onGroupClick = {},
            )
        }
    }
}
