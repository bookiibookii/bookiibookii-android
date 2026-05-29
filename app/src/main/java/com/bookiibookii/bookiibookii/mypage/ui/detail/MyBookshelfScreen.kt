package com.bookiibookii.bookiibookii.mypage.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.bookiibookii.bookiibookii.data.model.mypage.CompletedBook
import com.bookiibookii.bookiibookii.data.model.mypage.FavoriteBook
import com.bookiibookii.bookiibookii.data.model.mypage.RepresentativeBook
import com.bookiibookii.bookiibookii.mypage.vm.SortOrder
import com.bookiibookii.bookiibookii.onboarding.steps.model.BookSearchState
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

            RepresentativeBookSection(books = representativeBooks, onEditClick = { showEditBottomSheet = true })

            Spacer(modifier = Modifier.height(8.dp))

            LifeBookSection(
                lifeBooks = favoriteBooks,
                bookSearchState = bookSearchState,
                onSearchBooks = onSearchBooks,
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

            if (viewMode == BookViewMode.GRID) {
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
                showEditBottomSheet = false
                editableRepresentativeBooks.clear()
                editableRepresentativeBooks.addAll(representativeBooks)
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
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "뒤로 가기", tint = BookiiBookiiTheme.colors.grey900, modifier = Modifier.size(32.dp))
            }
            Text(text = "나의 책장", style = BookiiBookiiTheme.typography.medium20, color = BookiiBookiiTheme.colors.grey900, textAlign = TextAlign.Center)
            IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                Icon(painter = painterResource(R.drawable.ic_search), contentDescription = "검색 하기", tint = BookiiBookiiTheme.colors.grey900, modifier = Modifier.size(32.dp))
            }
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 0.5.dp)
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

@Preview(showBackground = true)
@Composable
private fun MyBookshelfScreenPreview() {
    MyBookshelfScreen()
}
