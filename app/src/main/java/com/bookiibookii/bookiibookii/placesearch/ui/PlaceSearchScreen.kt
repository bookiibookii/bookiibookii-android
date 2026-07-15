package com.bookiibookii.bookiibookii.placesearch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.data.model.location.PlaceSearchResult
import com.bookiibookii.bookiibookii.group.ui.search.SearchInputField
import com.bookiibookii.bookiibookii.placesearch.model.PlaceSearchUiState
import com.bookiibookii.bookiibookii.placesearch.vm.PlaceSearchViewModel
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 카카오 키워드 장소 검색 풀스크린. 결과 선택 시 onPlaceClick으로 좌표 포함 결과 반환
@Composable
fun PlaceSearchScreen(
    onBackClick: () -> Unit,
    onPlaceClick: (PlaceSearchResult) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceSearchViewModel = viewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    PlaceSearchContent(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::onSearch,
        onLoadMore = viewModel::loadMore,
        onRetry = viewModel::retry,
        onBackClick = onBackClick,
        onPlaceClick = onPlaceClick,
        modifier = modifier,
    )
}

@Composable
private fun PlaceSearchContent(
    uiState: PlaceSearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onPlaceClick: (PlaceSearchResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.white)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookiiBackButton(onClick = onBackClick)
            Text(
                text = "장소 검색",
                style = BookiiBookiiTheme.typography.semibold18,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.size(40.dp))
        }

        // 검색 입력바
        SearchInputField(
            query = uiState.query,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            hint = "장소명, 주소로 검색",
            autoFocus = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )

        // 결과 영역
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.loading && uiState.results.isEmpty() -> {
                    CircularProgressIndicator(color = BookiiBookiiTheme.colors.uiMain)
                }

                uiState.error != null && uiState.results.isEmpty() -> {
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

                uiState.searchKeyword.isBlank() -> {
                    Text(
                        text = "만날 장소를 검색해보세요",
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey500,
                    )
                }

                uiState.results.isEmpty() -> {
                    Text(
                        text = "검색 결과가 없어요",
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
                            .padding(horizontal = 16.dp),
                    ) {
                        items(
                            items = uiState.results,
                            key = { "${it.placeName}_${it.x}_${it.y}" },
                        ) { place ->
                            PlaceResultRow(place = place, onClick = { onPlaceClick(place) })
                            HorizontalDivider(
                                color = BookiiBookiiTheme.colors.grey100,
                                thickness = 1.dp,
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
}

@Composable
private fun PlaceResultRow(
    place: PlaceSearchResult,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = place.placeName,
            style = BookiiBookiiTheme.typography.medium16,
            color = BookiiBookiiTheme.colors.grey900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = place.address,
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(widthDp = 412, showBackground = true)
@Composable
private fun PlaceSearchContentPreview() {
    BookiiPreview {
        PlaceSearchContent(
            uiState = PlaceSearchUiState(
                query = "강남 스타벅스",
                searchKeyword = "강남 스타벅스",
                results = listOf(
                    PlaceSearchResult("스타벅스 강남점", "서울 강남구 강남대로 396", 127.0276, 37.4979),
                    PlaceSearchResult("스타벅스 강남2호점", "서울 강남구 테헤란로 101", 127.0301, 37.4985),
                ),
            ),
            onQueryChange = {},
            onSearch = {},
            onLoadMore = {},
            onRetry = {},
            onBackClick = {},
            onPlaceClick = {},
        )
    }
}
