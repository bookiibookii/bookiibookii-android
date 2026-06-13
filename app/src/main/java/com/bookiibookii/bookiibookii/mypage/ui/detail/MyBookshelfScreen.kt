package com.bookiibookii.bookiibookii.mypage.ui.detail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.data.model.mypage.CompletedBook
import com.bookiibookii.bookiibookii.data.model.mypage.FavoriteBook
import com.bookiibookii.bookiibookii.data.model.mypage.RepresentativeBook
import com.bookiibookii.bookiibookii.mypage.vm.SortOrder
import com.bookiibookii.bookiibookii.onboarding.steps.model.BookSearchState
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

enum class BookViewMode { GRID, LIST }

@Composable
fun MyBookshelfScreen(
    sortedCompletedBooks: List<CompletedBook> = emptyList(),
    favoriteBooks: List<FavoriteBook> = emptyList(),
    representativeBooks: List<RepresentativeBook> = emptyList(),
    representativeTitles: Set<String> = emptySet(),
    sortOrder: SortOrder = SortOrder.LATEST,
    bookSearchState: BookSearchState? = null,
    onBack: () -> Unit = {},
    onSortOrderChange: (SortOrder) -> Unit = {},
    onSearchBooks: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onClearBookSearch: () -> Unit = {},
    onDeleteRepresentativeBook: (Long) -> Unit = {},
    onReorderRepresentativeBook: (Long, Int) -> Unit = { _, _ -> },
    onAddFavoriteBook: (String) -> Unit = {},
    onDeleteFavoriteBook: (Long) -> Unit = {},
    onReplaceFavoriteBook: (Long, String) -> Unit = { _, _ -> },
    onAddRepresentativeBook: (Long) -> Unit = {},
    onRemoveRepresentativeBook: (Long) -> Unit = {},
) {
    var viewMode by remember { mutableStateOf(BookViewMode.GRID) }
    var showEditBottomSheet by remember { mutableStateOf(false) }
    var showBookBottomSheet by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<CompletedBook?>(null) }

    val editableRepresentativeBooks = remember { mutableStateListOf<RepresentativeBook>() }

    LaunchedEffect(representativeBooks) {
        if (!showEditBottomSheet) {
            editableRepresentativeBooks.clear()
            editableRepresentativeBooks.addAll(representativeBooks)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.uiBg)) {
        Column(modifier = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white)) {
            BookshelfTopBar(onBack = onBack)
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(8.dp))

            // 항상 로컬 리스트를 소스로 사용 — 편집 중 즉시 반영되고, 닫을 때 소스 전환(백엔드)으로 인한
            // 순서 튐이 없다. editable은 백엔드 갱신 시 LaunchedEffect(닫힌 상태)에서 조용히 동기화됨
            RepresentativeBookSection(
                books = editableRepresentativeBooks,
                onEditClick = { showEditBottomSheet = true },
            )

            Spacer(modifier = Modifier.height(8.dp))

            LifeBookSection(
                lifeBooks = favoriteBooks,
                bookSearchState = bookSearchState,
                onSearchBooks = onSearchBooks,
                onSearch = onSearchClick,
                onClearSearch = onClearBookSearch,
                onAddFavoriteBook = onAddFavoriteBook,
                onDeleteFavoriteBook = onDeleteFavoriteBook,
                onReplaceFavoriteBook = onReplaceFavoriteBook,
            )

            FilterBar(
                viewMode = viewMode,
                sortOrder = sortOrder,
                totalCount = sortedCompletedBooks.size,
                onViewModeToggle = {
                    viewMode = if (viewMode == BookViewMode.GRID) BookViewMode.LIST else BookViewMode.GRID
                },
                onSortOrderChange = onSortOrderChange,
            )

            if (sortedCompletedBooks.isEmpty()) {
                // 책 0권: 빈 상태 카드
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                        .background(BookiiBookiiTheme.colors.white)
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "책장이 비어 있어요.",
                        style = BookiiBookiiTheme.typography.regular16,
                        color = BookiiBookiiTheme.colors.grey600,
                    )
                }
            } else if (viewMode == BookViewMode.GRID) {
                BookGridView(
                    books = sortedCompletedBooks,
                    representativeTitles = representativeTitles,
                    onBookClick = { book -> selectedBook = book; showBookBottomSheet = true },
                )
            } else {
                BookListView(
                    books = sortedCompletedBooks,
                    representativeTitles = representativeTitles,
                    onBookClick = { book -> selectedBook = book; showBookBottomSheet = true },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }

    if (showEditBottomSheet) {
        RepresentativeEditBottomSheet(
            bookList = editableRepresentativeBooks,
            onDismiss = {
                // 변경은 드래그마다 PATCH로 이미 저장됨 → 닫을 때 editable을 백엔드로 강제 리셋하지 않는다.
                // (리셋하면 재조회 전 stale 값으로 순서가 튐). 백엔드 동기화는 LaunchedEffect가 처리
                showEditBottomSheet = false
            },
            onRemove = { book -> editableRepresentativeBooks.remove(book); onDeleteRepresentativeBook(book.userBookId) },
            onMove = { fromIndex, toIndex ->
                val temp = editableRepresentativeBooks[fromIndex]
                editableRepresentativeBooks[fromIndex] = editableRepresentativeBooks[toIndex]
                editableRepresentativeBooks[toIndex] = temp
            },
            onReorder = { userBookId, newOrder -> onReorderRepresentativeBook(userBookId, newOrder) },
        )
    }

    if (showBookBottomSheet && selectedBook != null) {
        val book = selectedBook!!
        val repBook = representativeBooks.find { it.title == book.title }
        BookshelfBookBottomSheet(
            title = book.title,
            author = book.author ?: "",
            genre = book.category?.trim('(', ')') ?: "",
            memberBookId = book.memberBookId,
            representativeUserBookId = repBook?.userBookId,
            onDismiss = { showBookBottomSheet = false },
            onReviewClick = { showBookBottomSheet = false },
            onAddRepresentativeClick = { memberBookId -> showBookBottomSheet = false; onAddRepresentativeBook(memberBookId) },
            onRemoveRepresentativeClick = { userBookId -> showBookBottomSheet = false; onRemoveRepresentativeBook(userBookId) },
            onLibraryClick = { showBookBottomSheet = false },
            onAladinClick = { showBookBottomSheet = false },
        )
    }
}

@Composable
private fun BookshelfTopBar(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(modifier = Modifier.width(88.dp), verticalAlignment = Alignment.CenterVertically) {
                BookiiBackButton(onClick = onBack)
            }
            Text(text = "나의 책장", style = BookiiBookiiTheme.typography.medium20, color = BookiiBookiiTheme.colors.grey900, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.width(88.dp))
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 1.dp)
    }
}

@Composable
private fun FilterBar(
    viewMode: BookViewMode,
    sortOrder: SortOrder,
    totalCount: Int,
    onViewModeToggle: () -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onViewModeToggle, modifier = Modifier.size(24.dp)) {
            Icon(
                painter = painterResource(if (viewMode == BookViewMode.GRID) R.drawable.ic_album else R.drawable.ic_line),
                contentDescription = "뷰 모드 전환",
                tint = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "${totalCount}권", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
        Spacer(modifier = Modifier.weight(1f))
        val sortOptions = listOf(
            SortOrder.LATEST to "최신순",
            SortOrder.OLDEST to "과거순",
            SortOrder.RATING to "별점순",
            SortOrder.TITLE to "제목순",
        )
        sortOptions.forEachIndexed { index, (order, label) ->
            if (index > 0) Text(text = " | ", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
            Text(
                text = label,
                style = if (sortOrder == order) BookiiBookiiTheme.typography.semibold14 else BookiiBookiiTheme.typography.regular14,
                color = if (sortOrder == order) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey500,
                modifier = Modifier.clickable { onSortOrderChange(order) },
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun MyBookshelfScreenPreview() {
    val completedBooks = listOf(
        CompletedBook(memberBookId = 1, title = "데미안", author = "헤르만 헤세", image = null, category = "(소설)", rating = 4.5, completedAt = "2026-05-01"),
        CompletedBook(memberBookId = 2, title = "1984", author = "조지 오웰", image = null, category = "(소설)", rating = 5.0, completedAt = "2026-04-20"),
        CompletedBook(memberBookId = 3, title = "사피엔스", author = "유발 하라리", image = null, category = "(인문)", rating = 4.0, completedAt = "2026-04-10"),
        CompletedBook(memberBookId = 4, title = "코스모스", author = "칼 세이건", image = null, category = "(과학)", rating = 4.8, completedAt = "2026-03-15"),
    )
    val favoriteBooks = listOf(
        FavoriteBook(userBookId = 10, title = "데미안", author = "헤르만 헤세", category = "(소설)", image = null),
        FavoriteBook(userBookId = 11, title = "1984", author = "조지 오웰", category = "(소설)", image = null),
    )
    val representativeBooks = listOf(
        RepresentativeBook(userBookId = 10, title = "일이삼사오육칠팔구십일이삼사오", displayOrder = 0, isFavorite = true),
        RepresentativeBook(userBookId = 11, title = "1984", displayOrder = 1, isFavorite = false),
    )
    BookiiPreview {
        MyBookshelfScreen(
            sortedCompletedBooks = completedBooks,
            favoriteBooks = favoriteBooks,
            representativeBooks = representativeBooks,
            representativeTitles = setOf("데미안", "1984"),
        )
    }
}
