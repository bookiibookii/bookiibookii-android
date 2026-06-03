package com.bookiibookii.bookiibookii.home.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.data.model.group.GroupItem
import com.bookiibookii.bookiibookii.data.model.group.HomeBestsellerSection
import com.bookiibookii.bookiibookii.data.model.group.HomeCategorySection
import com.bookiibookii.bookiibookii.data.model.group.HomeRegionSection
import com.bookiibookii.bookiibookii.home.ui.component.RecommendGroupRow
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

internal fun LazyListScope.homeRecommendContent(
    newGroups: List<GroupItem>,
    categorySection: HomeCategorySection?,
    bestsellerSection: HomeBestsellerSection?,
    regionSection: HomeRegionSection?,
    onGroupClick: (Long) -> Unit,
) {
    // 피그마: 탭 영역 ~ 첫 번째 섹션 사이 8dp 회색 간격
    item { Box(Modifier.fillMaxWidth().height(8.dp)) }

    // ① 신규 그룹 섹션 — 오늘 생성된 그룹 없으면 섹션 미표시
    if (newGroups.isNotEmpty()) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BookiiBookiiTheme.colors.white)
                    .padding(top = 16.dp, bottom = 16.dp),
            ) {
                HomeSectionHeader(
                    title = "신규 그룹을 확인해보세요.",
                    subtitle = "오늘 만들어진 따끈따끈한 그룹들만 모았어요.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                )
                Box(Modifier.height(12.dp))
                RecommendGroupRow(
                    groups = newGroups,
                    onGroupClick = onGroupClick,
                )
            }
        }
    }

    // ② 카테고리 기반 섹션 — category != null 이고 그룹 있을 때만 노출
    val categoryGroups = categorySection?.groups.orEmpty()
    if (categorySection?.category != null && categoryGroups.isNotEmpty()) {
        item { Box(Modifier.fillMaxWidth().height(8.dp)) }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BookiiBookiiTheme.colors.white)
                    .padding(top = 16.dp, bottom = 16.dp),
            ) {
                HomeSectionHeader(
                    title = "최근 읽은 장르와 비슷해요",
                    subtitle = "${categorySection.category} 장르 그룹들을 모아봤어요.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                )
                Box(Modifier.height(12.dp))
                RecommendGroupRow(
                    groups = categoryGroups,
                    onGroupClick = onGroupClick,
                )
            }
        }
    }

    // ③ 베스트셀러 기반 섹션 — 그룹 있을 때만 노출
    val bestsellerGroups = bestsellerSection?.groups.orEmpty()
    if (bestsellerGroups.isNotEmpty()) {
        item { Box(Modifier.fillMaxWidth().height(8.dp)) }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BookiiBookiiTheme.colors.white)
                    .padding(top = 16.dp, bottom = 16.dp),
            ) {
                HomeSectionHeader(
                    title = "나 빼고 다 읽은 책 여기 있어요.",
                    subtitle = "이번 기회에 베스트셀러/스테디셀러 읽어볼까요?",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                )
                Box(Modifier.height(12.dp))
                RecommendGroupRow(
                    groups = bestsellerGroups,
                    onGroupClick = onGroupClick,
                )
            }
        }
    }

    // ④ 지역 기반 섹션 — region != null 이고 그룹 있을 때만 노출
    val regionGroups = regionSection?.groups.orEmpty()
    if (regionSection?.region != null && regionGroups.isNotEmpty()) {
        item { Box(Modifier.fillMaxWidth().height(8.dp)) }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BookiiBookiiTheme.colors.white)
                    .padding(top = 16.dp, bottom = 16.dp),
            ) {
                HomeSectionHeader(
                    title = "${regionSection.region}에서 교환할 수 있는 책",
                    subtitle = "직접 교환 가능한 그룹들이에요.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                )
                Box(Modifier.height(12.dp))
                RecommendGroupRow(
                    groups = regionGroups,
                    onGroupClick = onGroupClick,
                )
            }
        }
    }

    // 하단 여백
    item { Box(Modifier.height(80.dp)) }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val typography = BookiiBookiiTheme.typography
    val colors = BookiiBookiiTheme.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = typography.regular20,
            color = colors.grey900,
        )
        Text(
            text = subtitle,
            style = typography.medium16,
            color = colors.grey600,
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
private val mockNewGroups = listOf(
    mockGroup,
    mockGroup.copy(groupId = 2L, title = "채식주의자"),
    mockGroup.copy(groupId = 3L, title = "아몬드"),
)

@Preview(showBackground = true, name = "HomeRecommendContent - 전체 섹션")
@Composable
private fun HomeRecommendContentPreview() {
    BookiiPreview {
        LazyColumn {
            homeRecommendContent(
                newGroups = mockNewGroups,
                categorySection = HomeCategorySection(
                    category = "한국소설",
                    groups = mockNewGroups,
                ),
                bestsellerSection = HomeBestsellerSection(groups = mockNewGroups),
                regionSection = HomeRegionSection(
                    region = "인천 남동구",
                    groups = mockNewGroups,
                ),
                onGroupClick = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "HomeRecommendContent - 신규만")
@Composable
private fun HomeRecommendContentNewOnlyPreview() {
    BookiiPreview {
        LazyColumn {
            homeRecommendContent(
                newGroups = mockNewGroups,
                categorySection = null,
                bestsellerSection = null,
                regionSection = null,
                onGroupClick = {},
            )
        }
    }
}
