package com.bookiibookii.bookiibookii.home.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.data.model.group.GroupItem
import com.bookiibookii.bookiibookii.home.ui.component.HomeGroupCard
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

internal fun LazyListScope.homeAppliedContent(
    appliedGroups: List<GroupItem>,
    onGroupClick: (Long) -> Unit,
    onCreateGroupClick: () -> Unit = {},
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
            // 피그마: "N 권" + 안내 문구를 gap=4dp 서브그룹으로 묶음
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // "N 권" 라벨 — 피그마: gap=4dp, h=20dp, regular14, grey900
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(20.dp),
                ) {
                    Text(
                        text = appliedGroups.size.toString(),
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey900,
                    )
                    Text(
                        text = "권",
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey900,
                    )
                }
                // 캡션 — 피그마: regular14, grey500
                Text(
                    text = "매칭을 기다리는 그룹만 보여요.",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey500,
                )
            }

            if (appliedGroups.isEmpty()) {
                // 피그마: NullModal — white bg, round24, padding=20dp, gap=20dp
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(BookiiBookiiTheme.colors.white)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Text(
                        text = "매칭을 기다리는 신청 내역이 없어요.\n새로운 교환독서를 시작해볼까요?",
                        style = BookiiBookiiTheme.typography.medium16,
                        color = BookiiBookiiTheme.colors.grey900,
                        textAlign = TextAlign.Center,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BookiiBookiiTheme.colors.uiMain)
                            .clickable(onClick = onCreateGroupClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "그룹 탐색하기",
                            style = BookiiBookiiTheme.typography.regular15,
                            color = BookiiBookiiTheme.colors.white,
                        )
                    }
                }
            } else {
                appliedGroups.forEach { group ->
                    HomeGroupCard(
                        group = group,
                        onClick = { onGroupClick(group.groupId) },
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
