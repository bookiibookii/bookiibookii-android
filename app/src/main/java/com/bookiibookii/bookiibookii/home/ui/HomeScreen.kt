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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookiibookii.bookiibookii.data.model.group.HomeLayoutType
import com.bookiibookii.bookiibookii.data.model.group.HomeSection
import com.bookiibookii.bookiibookii.data.model.group.HomeSectionItem
import com.bookiibookii.bookiibookii.home.HomeTab
import com.bookiibookii.bookiibookii.home.HomeUiState
import com.bookiibookii.bookiibookii.home.HomeViewModel
import com.bookiibookii.bookiibookii.home.ui.component.HomeSearchCreateRow
import com.bookiibookii.bookiibookii.home.ui.component.HomeTabRow
import com.bookiibookii.bookiibookii.home.ui.component.HomeWelcomeSection
import com.bookiibookii.bookiibookii.ui.component.BookiiTopBar
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
    onBookClick: (String) -> Unit,
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
        // 책 탭 → 그 책 제목(searchKeyword)으로 그룹 검색
        onBookClick = { item -> onBookClick(item.searchKeyword ?: item.title.orEmpty()) },
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
    onBookClick: (HomeSectionItem) -> Unit = {},
) {
    val lazyListState = rememberLazyListState()

    // 탭 전환 시 sticky 탭바 위치(index 2)로 자동 스크롤 — 새 탭 콘텐츠를 상단부터 보이게
    LaunchedEffect(uiState.selectedTab) {
        if (lazyListState.firstVisibleItemIndex > 2) {
            lazyListState.animateScrollToItem(2)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        BookiiTopBar(
            title = "탐색",
            onProfileClick = onProfileClick,
            onNotificationClick = onNotificationClick,
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

            // [1] 검색창+그룹생성 — 일반 아이템: 맨 위에서만 보이고 스크롤하면 자연스럽게 사라짐
            // (sticky에서 빼서 헤더 높이를 고정 → 스크롤 점프/자동 스크롤 현상 제거)
            item {
                HomeSearchCreateRow(
                    onSearchClick = onSearchClick,
                    onCreateGroupClick = onCreateGroupClick,
                )
            }

            // [2] stickyHeader: 탭바만 고정 — 높이가 변하지 않아 스크롤 점프 없음
            stickyHeader {
                HomeTabRow(selectedTab = uiState.selectedTab, onTabSelect = onTabSelect)
            }

            when (uiState.selectedTab) {
                HomeTab.RECOMMEND -> homeRecommendContent(
                    sections = uiState.recommendSections,
                    onGroupClick = onGroupClick,
                    onBookClick = onBookClick,
                )
                HomeTab.MY_GROUPS -> homeMyGroupsContent(
                    myGroups = uiState.myGroups,
                    onGroupClick = onGroupClick,
                    onCreateGroupClick = onCreateGroupClick,
                )
                HomeTab.APPLIED -> homeAppliedContent(
                    appliedGroups = uiState.appliedGroups,
                    onGroupClick = onGroupClick,
                    // "그룹 탐색하기" → 그룹 검색 화면(GroupSearchScreen)
                    onExploreGroupClick = onSearchClick,
                )
            }
        }
    }
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

private val mockGroupItem = HomeSectionItem(
    groupId = 1L, groupName = "책과 함께", bookTitle = "살인자의 기억법", author = "김영하",
    bookImage = null, hostNickname = "부키", hostProfileImageUrl = null, readingPeriod = 7,
)
private val mockSections = listOf(
    HomeSection(
        sectionType = "NEW_GROUPS",
        title = "신규 그룹을 확인해보세요.",
        subtitle = "오늘 만들어진 따끈따끈한 그룹들만 모았어요.",
        layoutType = HomeLayoutType.GROUP_CARD_CAROUSEL,
        items = listOf(
            mockGroupItem,
            mockGroupItem.copy(groupId = 2L, bookTitle = "채식주의자"),
            mockGroupItem.copy(groupId = 3L, bookTitle = "아몬드"),
        ),
    ),
)

@Preview(showBackground = true, name = "HomeScreen - 추천 탭")
@Composable
private fun HomeScreenRecommendPreview() {
    BookiiPreview {
        HomeScreen(
            uiState = HomeUiState(
                nickname = "부키유저",
                selectedTab = HomeTab.RECOMMEND,
                recommendSections = mockSections,
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
                recommendSections = emptyList(),
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
