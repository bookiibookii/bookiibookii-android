package com.bookiibookii.bookiibookii.home.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookiibookii.bookiibookii.data.model.group.GroupItem
import com.bookiibookii.bookiibookii.home.HomeTab
import com.bookiibookii.bookiibookii.home.HomeUiState
import com.bookiibookii.bookiibookii.home.HomeViewModel
import com.bookiibookii.bookiibookii.home.ui.component.HomeSearchCreateRow
import com.bookiibookii.bookiibookii.home.ui.component.HomeTabRow
import com.bookiibookii.bookiibookii.home.ui.component.HomeTopBar
import com.bookiibookii.bookiibookii.home.ui.component.HomeWelcomeSection
import com.bookiibookii.bookiibookii.home.ui.content.homeAppliedContent
import com.bookiibookii.bookiibookii.home.ui.content.homeMyGroupsContent
import com.bookiibookii.bookiibookii.home.ui.content.homeRecommendContent
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// ─── Stateful 진입점 ──────────────────────────────────────────────────────────

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onGroupClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onTabSelect = viewModel::selectTab,
        onGroupClick = onGroupClick,
        onSearchClick = onSearchClick,
        onCreateGroupClick = onCreateGroupClick,
        onNotificationClick = onNotificationClick,
        onProfileClick = onProfileClick,
    )
}

// ─── Stateless 레이아웃 ───────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onTabSelect: (HomeTab) -> Unit,
    onGroupClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    val lazyListState = rememberLazyListState()

    // 탭 전환 시 탭이 보이지 않으면 탭 위치로 자동 스크롤 (index 3 = stickyHeader)
    LaunchedEffect(uiState.selectedTab) {
        if (lazyListState.firstVisibleItemIndex > 3) {
            lazyListState.animateScrollToItem(3)
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        item { HomeTopBar(onNotificationClick = onNotificationClick, onProfileClick = onProfileClick) }
        item { HomeWelcomeSection(nickname = uiState.nickname) }
        item { HomeSearchCreateRow(onSearchClick = onSearchClick, onCreateGroupClick = onCreateGroupClick) }

        stickyHeader {
            HomeTabRow(selectedTab = uiState.selectedTab, onTabSelect = onTabSelect)
        }

        when (uiState.selectedTab) {
            HomeTab.RECOMMEND -> homeRecommendContent(
                newGroups = uiState.newGroups,
                categorySection = uiState.categorySection,
                regionSection = uiState.regionSection,
                onGroupClick = onGroupClick,
            )
            HomeTab.MY_GROUPS -> homeMyGroupsContent(
                myGroups = uiState.myGroups,
                onGroupClick = onGroupClick,
            )
            HomeTab.APPLIED -> homeAppliedContent(
                appliedGroups = uiState.appliedGroups,
                onGroupClick = onGroupClick,
            )
        }
    }
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

private val mockGroupItem = GroupItem(
    groupId = 1L, groupName = "책과 함께", title = "살인자의 기억법", author = "김영하",
    genre = "한국소설", bookImage = null, hostNickname = "부키", hostProfileImageUrl = null,
    groupStatus = "RECRUITING", currentCount = 2, maxCapacity = 5, waitingCount = 0,
    isHot = false, tradeType = "직접", readingPeriod = 7, pictureBadge = null,
)
private val mockNewGroups = listOf(
    mockGroupItem,
    mockGroupItem.copy(groupId = 2L, title = "채식주의자"),
    mockGroupItem.copy(groupId = 3L, title = "아몬드"),
)

@Preview(showBackground = true, name = "HomeScreen - 추천 탭")
@Composable
private fun HomeScreenRecommendPreview() {
    BookiiPreview {
        HomeScreen(
            uiState = HomeUiState(
                nickname = "부키유저",
                selectedTab = HomeTab.RECOMMEND,
                newGroups = mockNewGroups,
            ),
            onTabSelect = {},
            onGroupClick = {},
            onSearchClick = {},
            onCreateGroupClick = {},
            onNotificationClick = {},
            onProfileClick = {},
        )
    }
}

@Preview(showBackground = true, name = "HomeScreen - 추천 탭 (빈 상태)")
@Composable
private fun HomeScreenRecommendEmptyPreview() {
    BookiiPreview {
        HomeScreen(
            uiState = HomeUiState(
                nickname = "부키유저",
                selectedTab = HomeTab.RECOMMEND,
                newGroups = emptyList(),
            ),
            onTabSelect = {},
            onGroupClick = {},
            onSearchClick = {},
            onCreateGroupClick = {},
            onNotificationClick = {},
            onProfileClick = {},
        )
    }
}

@Preview(showBackground = true, name = "HomeScreen - 내 그룹 탭 (빈 상태)")
@Composable
private fun HomeScreenMyGroupsEmptyPreview() {
    BookiiPreview {
        HomeScreen(
            uiState = HomeUiState(
                nickname = "부키유저",
                selectedTab = HomeTab.MY_GROUPS,
                myGroups = emptyList(),
            ),
            onTabSelect = {},
            onGroupClick = {},
            onSearchClick = {},
            onCreateGroupClick = {},
            onNotificationClick = {},
            onProfileClick = {},
        )
    }
}

@Preview(showBackground = true, name = "HomeScreen - 신청한 그룹 탭 (빈 상태)")
@Composable
private fun HomeScreenAppliedEmptyPreview() {
    BookiiPreview {
        HomeScreen(
            uiState = HomeUiState(
                nickname = "부키유저",
                selectedTab = HomeTab.APPLIED,
                appliedGroups = emptyList(),
            ),
            onTabSelect = {},
            onGroupClick = {},
            onSearchClick = {},
            onCreateGroupClick = {},
            onNotificationClick = {},
            onProfileClick = {},
        )
    }
}
