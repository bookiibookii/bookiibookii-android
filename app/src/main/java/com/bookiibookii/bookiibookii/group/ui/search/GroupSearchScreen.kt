package com.bookiibookii.bookiibookii.group.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.group.GroupItem
import com.bookiibookii.bookiibookii.group.model.GroupSearchUiState
import com.bookiibookii.bookiibookii.group.vm.GroupSearchViewModel
import com.bookiibookii.bookiibookii.ui.component.FilterChip
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.launch

// 열려 있는 필터 바텀시트 종류
private enum class FilterSheet { EXCHANGE, GENRE }

// 그룹 탐색(목록) 화면 — VM 주입/상태 수집 (stateful)
@Composable
fun GroupSearchRoute(
    onBack: () -> Unit,
    viewModel: GroupSearchViewModel = viewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    GroupSearchScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::onSearch,
        onApplyTradeTypes = viewModel::applyTradeTypes,
        onApplyCategories = viewModel::applyCategories,
        onLoadMore = viewModel::loadMore,
    )
}

// 그룹 탐색(목록) 화면 (stateless: 상태·콜백을 파라미터로 받음)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSearchScreen(
    uiState: GroupSearchUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onApplyTradeTypes: (List<String>) -> Unit,
    onApplyCategories: (List<String>) -> Unit,
    onLoadMore: () -> Unit,
) {
    var openSheet by remember { mutableStateOf<FilterSheet?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    // 적용/취소 시 슬라이드 다운 후 닫기
    val closeSheet: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) openSheet = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron),
                        contentDescription = "뒤로가기",
                        tint = BookiiBookiiTheme.colors.grey900,
                    )
                }
                SearchInputField(
                    query = uiState.query,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    text = "교환 방식",
                    selected = uiState.tradeTypes.isNotEmpty(),
                    onClick = { openSheet = FilterSheet.EXCHANGE },
                )
                // TODO(보류): 지역 필터 — regions 문자열 포맷 백엔드 확인 후 연동
                FilterChip(
                    text = "지역별",
                    selected = uiState.regions.isNotEmpty(),
                    onClick = {},
                )
                FilterChip(
                    text = "분야별",
                    selected = uiState.categories.isNotEmpty(),
                    onClick = { openSheet = FilterSheet.GENRE },
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.loading && uiState.items.isEmpty() -> {
                    CircularProgressIndicator(color = BookiiBookiiTheme.colors.uiMain)
                }

                uiState.error != null && uiState.items.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = uiState.error,
                            style = BookiiBookiiTheme.typography.regular14,
                            color = BookiiBookiiTheme.colors.grey500,
                        )
                        Text(
                            text = "다시 시도",
                            style = BookiiBookiiTheme.typography.medium14,
                            color = BookiiBookiiTheme.colors.uiMain,
                            modifier = Modifier.clickable(onClick = onRetry),
                        )
                    }
                }

                uiState.items.isEmpty() -> {
                    Text(
                        text = if (uiState.isSearchMode) "검색 결과가 없어요" else "조건에 맞는 그룹이 없어요",
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey500,
                    )
                }

                else -> {
                    val listState = rememberLazyListState()
                    // 마지막 아이템이 보이면 다음 페이지 요청 (중복/불가 상황은 VM에서 무시)
                    val reachedEnd by remember {
                        derivedStateOf {
                            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                            last != null && last >= listState.layoutInfo.totalItemsCount - 1
                        }
                    }
                    LaunchedEffect(reachedEnd) {
                        if (reachedEnd) onLoadMore()
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (uiState.isSearchMode && uiState.totalCount != null) {
                            item {
                                Text(
                                    text = "${uiState.totalCount} 권",
                                    style = BookiiBookiiTheme.typography.regular14,
                                    color = BookiiBookiiTheme.colors.grey900,
                                )
                            }
                        }
                        items(uiState.items, key = { it.groupId }) { item ->
                            ExploreGroupCard(
                                title = item.title,
                                author = item.author.orEmpty(),
                                category = item.genre.orEmpty(),
                                exchangeType = tradeTypeLabel(item.tradeType),
                                expectedDays = item.readingPeriod,
                                nickname = item.hostNickname.orEmpty(),
                                groupName = item.groupName,
                                imageUrl = item.bookImage,
                            )
                        }
                        if (uiState.loadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        color = BookiiBookiiTheme.colors.uiMain,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (openSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { openSheet = null },
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null,
        ) {
            when (openSheet) {
                FilterSheet.EXCHANGE -> ExchangeMethodBottomSheet(
                    initialTradeTypes = uiState.tradeTypes,
                    onApply = { onApplyTradeTypes(it); closeSheet() },
                    onCancel = closeSheet,
                )

                FilterSheet.GENRE -> GenreBottomSheet(
                    initialCategories = uiState.categories,
                    onApply = { onApplyCategories(it); closeSheet() },
                    onCancel = closeSheet,
                )

                null -> Unit
            }
        }
    }
}

// 거래 방식 코드 → 표시 라벨
private fun tradeTypeLabel(tradeType: String?): String = when (tradeType) {
    "DIRECT" -> "직접"
    "DELIVERY" -> "택배"
    else -> ""
}

@Preview(widthDp = 412, heightDp = 917, showBackground = true)
@Composable
private fun GroupSearchScreenPreview() {
    BookiiPreview {
        GroupSearchScreen(
            uiState = GroupSearchUiState(
                items = listOf(
                    GroupItem(
                        groupId = 1,
                        groupName = "김영하 도장깨기 하실 분",
                        title = "살인자의 기억법",
                        author = "김영하",
                        genre = "한국소설",
                        bookImage = null,
                        hostNickname = "sayo",
                        hostProfileImageUrl = null,
                        groupStatus = "RECRUITING",
                        currentCount = 1,
                        maxCapacity = 4,
                        waitingCount = 0,
                        isHot = false,
                        tradeType = "DIRECT",
                        readingPeriod = 7,
                        pictureBadge = null,
                    ),
                    GroupItem(
                        groupId = 2,
                        groupName = "자연과 함께 하는 삶",
                        title = "참을 수 없는 존재의 가벼움",
                        author = "밀란 쿤데라",
                        genre = "세계소설",
                        bookImage = null,
                        hostNickname = "kanghunsim",
                        hostProfileImageUrl = null,
                        groupStatus = "RECRUITING",
                        currentCount = 2,
                        maxCapacity = 5,
                        waitingCount = 1,
                        isHot = true,
                        tradeType = "DELIVERY",
                        readingPeriod = 7,
                        pictureBadge = null,
                    ),
                ),
            ),
            onBack = {},
            onRetry = {},
            onQueryChange = {},
            onSearch = {},
            onApplyTradeTypes = {},
            onApplyCategories = {},
            onLoadMore = {},
        )
    }
}
