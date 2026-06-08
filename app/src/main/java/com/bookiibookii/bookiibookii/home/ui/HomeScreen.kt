package com.bookiibookii.bookiibookii.home.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    // 스크롤 입력 자체를 감지 (layout shift 영향 없어 깜빡임 없음)
    var isScrollingUp by remember { mutableStateOf(false) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y != 0f) isScrollingUp = available.y > 0f
                return Offset.Zero
            }
        }
    }

    // 검색창 표시 조건:
    //   - 맨 위(WelcomeSection 보임): 항상 표시
    //   - 위로 스크롤 중 + WelcomeSection이 사라진 상태: 표시
    //   - 아래로 스크롤 중: 숨김
    val showStickySearchBar by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 ||
                (isScrollingUp && lazyListState.firstVisibleItemIndex > 0)
        }
    }

    // 탭 전환 시 stickyHeader 위치(index 1)로 자동 스크롤
    LaunchedEffect(uiState.selectedTab) {
        if (lazyListState.firstVisibleItemIndex > 1) {
            lazyListState.animateScrollToItem(1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg)
            .nestedScroll(nestedScrollConnection),
    ) {
        HomeTopBar(
            onNotificationClick = onNotificationClick,
            onProfileClick = onProfileClick,
            hasNewNotification = uiState.hasNewNotification,
            modifier = Modifier.fillMaxWidth(),
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .background(BookiiBookiiTheme.colors.uiBg),
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            // [0] 웰컴섹션 — 맨 위에서만 보임
            item { HomeWelcomeSection(nickname = uiState.nickname) }

            // stickyHeader: 검색창(조건부) + 탭바
            // 애니메이션 없이 즉시 전환 → stickyHeader 높이 변화로 인한 버벅임 제거
            stickyHeader {
                Column {
                    if (showStickySearchBar) {
                        HomeSearchCreateRow(
                            onSearchClick = onSearchClick,
                            onCreateGroupClick = onCreateGroupClick,
                        )
                    }
                    HomeTabRow(selectedTab = uiState.selectedTab, onTabSelect = onTabSelect)
                }
            }

            when (uiState.selectedTab) {
                HomeTab.RECOMMEND -> homeRecommendContent(
                    newGroups = uiState.newGroups,
                    categorySection = uiState.categorySection,
                    bestsellerSection = uiState.bestsellerSection,
                    regionSection = uiState.regionSection,
                    onGroupClick = onGroupClick,
                )
                HomeTab.MY_GROUPS -> homeMyGroupsContent(
                    myGroups = uiState.myGroups,
                    onGroupClick = onGroupClick,
                    onCreateGroupClick = onCreateGroupClick,
                )
                HomeTab.APPLIED -> homeAppliedContent(
                    appliedGroups = uiState.appliedGroups,
                    onGroupClick = onGroupClick,
                    onCreateGroupClick = onCreateGroupClick,
                )
            }
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
