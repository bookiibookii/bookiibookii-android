package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

enum class LibrarySortType { RECENT, OLDEST, RATING, TITLE }
private enum class LibraryViewType { GRID, LIST }

private val searchBarShape = RoundedCornerShape(
    topStart = 20.dp, topEnd = 30.dp,
    bottomStart = 20.dp, bottomEnd = 30.dp,
)

data class LibraryBook(
    val groupId: Int = 0,
    val memberBookId: Int = 0,
    val groupName: String,
    val title: String,
    val author: String = "",
    val genre: String = "",
    val coverUrl: String? = null,
    val progress: Float? = null,
    val rating: Int? = null,
    val startDate: String = "",
    val endDate: String? = null,
    val completedAt: String? = null,
    val totalPages: Int? = null,
)

@Composable
fun LibraryMainRoute(
    onProfileClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onBookClick: (LibraryBook) -> Unit,
    onMatchingStatusClick: () -> Unit = {},
    viewModel: com.bookiibookii.bookiibookii.library.vm.LibraryMainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        viewModel.fetchBooks()
    }

    LibraryScreen(
        readingBooks = state.readingBooks,
        doneBooks = state.doneBooks,
        sortType = state.sortType,
        isLoading = state.isLoading,
        onSortChange = { viewModel.setSortType(it) },
        onProfileClick = onProfileClick,
        onBookmarkClick = onBookmarkClick,
        onBookClick = onBookClick,
        onMatchingStatusClick = onMatchingStatusClick,
    )
}

@Composable
fun LibraryScreen(
    readingBooks: List<LibraryBook> = emptyList(),
    doneBooks: List<LibraryBook> = emptyList(),
    sortType: LibrarySortType = LibrarySortType.RECENT,
    isLoading: Boolean = false,
    onSortChange: (LibrarySortType) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {},
    onBookClick: (LibraryBook) -> Unit = {},
    onMatchingStatusClick: () -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }
    var viewType by remember { mutableStateOf(LibraryViewType.GRID) }
    val allBooks = remember(readingBooks, doneBooks) { readingBooks + doneBooks }
    val isSearchActive = searchQuery.isNotEmpty()
    val filteredBooks = remember(allBooks, searchQuery) {
        if (searchQuery.isEmpty()) allBooks else allBooks.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.groupName.contains(searchQuery, ignoreCase = true) ||
            it.author.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        LibraryHeader(onProfileClick = onProfileClick, onBookmarkClick = onBookmarkClick)

        LazyColumn(modifier = Modifier.weight(1f)) {
            item(key = "search_bar") {
                LibrarySearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                )
            }

            if (isSearchActive) {
                if (filteredBooks.isEmpty()) {
                    item(key = "search_empty") {
                        LibraryEmptyStateBox(
                            text = "그룹을 찾지 못했어요.\n검색어를 다시 확인해주세요.",
                            textColor = BookiiBookiiTheme.colors.grey600,
                            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp),
                        )
                    }
                } else {
                    item(key = "search_count") {
                        Text(
                            text = "${filteredBooks.size}권",
                            style = BookiiBookiiTheme.typography.regular15,
                            color = BookiiBookiiTheme.colors.grey900,
                            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 16.dp),
                        )
                    }
                    libraryBookRows("search", filteredBooks, viewType, onBookClick)
                }
            } else {
                item(key = "filter_bar") {
                    LibraryFilterBar(
                        bookCount = allBooks.size,
                        sortType = sortType,
                        viewType = viewType,
                        onSortChange = { onSortChange(it) },
                        onViewToggle = {
                            viewType = if (viewType == LibraryViewType.GRID) LibraryViewType.LIST else LibraryViewType.GRID
                        },
                    )
                }
                item(key = "reading_title") { LibrarySectionTitle("읽는 중") }
                if (readingBooks.isNotEmpty()) {
                    libraryBookRows("reading", readingBooks, viewType, onBookClick)
                } else {
                    item(key = "reading_empty") {
                        LibraryEmptyStateBox(
                            text = "아직 진행 중인 그룹이 없어요,\n독서메이트 매칭 현황을 확인해보세요.",
                            textColor = BookiiBookiiTheme.colors.grey900,
                            buttonText = "매칭 현황 확인하기",
                            onButtonClick = onMatchingStatusClick,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
                item(key = "done_title") { LibrarySectionTitle("다 읽었어요", Modifier.padding(top = 24.dp)) }
                if (doneBooks.isNotEmpty()) {
                    libraryBookRows("done", doneBooks, viewType, onBookClick)
                } else {
                    item(key = "done_empty") {
                        LibraryEmptyStateBox(
                            text = "아직 종료된 그룹이 없어요.",
                            textColor = BookiiBookiiTheme.colors.grey600,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }

            item(key = "footer") { LibraryFooter() }
        }
    }
}

@Composable
private fun LibraryFooter() {
    val context = LocalContext.current
    Column {
        Text(
            text = buildAnnotatedString {
                append("도서 DB 제공 : 알라딘 인터넷서점(")
                withStyle(SpanStyle(color = Color(0xFF1E88E5), textDecoration = TextDecoration.Underline)) {
                    append("www.aladin.co.kr")
                }
                append(")")
            },
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey700,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 40.dp)
                .clickable {
                    context.startActivity(
                        android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://www.aladin.co.kr"),
                        )
                    )
                },
        )

        Spacer(modifier = Modifier.height(192.dp))
    }
}

@Composable
private fun LibrarySectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = BookiiBookiiTheme.typography.semibold20,
        color = BookiiBookiiTheme.colors.grey900,
        modifier = modifier.padding(horizontal = 16.dp).padding(vertical = 16.dp),
    )
}

@Composable
private fun LibraryHeader(
    onProfileClick: () -> Unit,
    onBookmarkClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onProfileClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_person2),
                    contentDescription = "프로필",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = "서재",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
            )
            IconButton(onClick = onBookmarkClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_bookmark),
                    contentDescription = "북마크",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = BookiiBookiiTheme.colors.grey200,
        )
    }
}

@Composable
private fun LibrarySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(searchBarShape)
            .background(BookiiBookiiTheme.colors.white)
            .border(1.dp, BookiiBookiiTheme.colors.grey100, searchBarShape)
            .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = BookiiBookiiTheme.typography.medium16.copy(color = BookiiBookiiTheme.colors.grey900),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = "그룹명, 도서명, 저자명으로 검색",
                            style = BookiiBookiiTheme.typography.regular16,
                            color = BookiiBookiiTheme.colors.grey500,
                        )
                    }
                    inner()
                },
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(if (query.isNotEmpty()) BookiiBookiiTheme.colors.grey900 else BookiiBookiiTheme.colors.grey300),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun LibraryFilterBar(
    bookCount: Int,
    sortType: LibrarySortType,
    viewType: LibraryViewType,
    onSortChange: (LibrarySortType) -> Unit,
    onViewToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.clickable { onViewToggle() },
        ) {
            Icon(
                painter = painterResource(if (viewType == LibraryViewType.GRID) R.drawable.ic_album else R.drawable.ic_line),
                contentDescription = "뷰 전환",
                tint = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.size(24.dp),
            )
            Text(text = "$bookCount", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
            Text(text = "권", style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey900)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val sortOptions = listOf(
                LibrarySortType.RECENT to "최신순",
                LibrarySortType.OLDEST to "과거순",
                LibrarySortType.RATING to "별점순",
                LibrarySortType.TITLE to "제목순",
            )
            sortOptions.forEachIndexed { index, (type, label) ->
                Text(
                    text = label,
                    style = if (sortType == type) BookiiBookiiTheme.typography.semibold14 else BookiiBookiiTheme.typography.regular14,
                    color = if (sortType == type) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey500,
                    modifier = Modifier.clickable { onSortChange(type) },
                )
                if (index < sortOptions.size - 1) {
                    Text(
                        text = "|",
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey500,
                    )
                }
            }
        }
    }
}

// 격자는 3권씩 한 행을 Lazy 항목 하나로 둔다. 행 간격은 격자 16dp, 목록 12dp로 기존과 같다.
// shortcut: key가 memberBookId라 서버가 같은 책을 중복으로 주면 크래시한다. 중복이 실제로 생기면 index를 섞은 key로 바꾼다.
private fun LazyListScope.libraryBookRows(
    section: String,
    books: List<LibraryBook>,
    viewType: LibraryViewType,
    onBookClick: (LibraryBook) -> Unit,
) {
    if (viewType == LibraryViewType.GRID) {
        val rows = books.chunked(3)
        itemsIndexed(rows, key = { _, row -> "$section-grid-${row.first().memberBookId}" }) { index, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = if (index == 0) 0.dp else 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { book ->
                    LibraryBookGridItem(book = book, modifier = Modifier.weight(1f).clickable { onBookClick(book) })
                }
                repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    } else {
        itemsIndexed(books, key = { _, book -> "$section-list-${book.memberBookId}" }) { index, book ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = if (index == 0) 0.dp else 12.dp),
            ) {
                LibraryBookListItem(book = book, onClick = { onBookClick(book) })
            }
        }
    }
}

@Composable
private fun LibraryEmptyStateBox(
    text: String,
    textColor: Color,
    buttonText: String? = null,
    onButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.regular16,
            color = textColor,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (buttonText != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BookiiBookiiTheme.colors.uiMain)
                    .clickable { onButtonClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = buttonText,
                    style = BookiiBookiiTheme.typography.regular15,
                    color = BookiiBookiiTheme.colors.white,
                )
            }
        }
    }
}

private val previewReadingBooks = listOf(
    LibraryBook(groupId = 1, memberBookId = 1, groupName = "숭실대 경제 독서모임", title = "데미안", author = "헤르만 헤세", progress = 0.64f, startDate = "2026-05-20"),
    LibraryBook(groupId = 2, memberBookId = 2, groupName = "주말 소설 읽기", title = "어린 왕자", author = "생텍쥐페리", progress = 0.3f, startDate = "2026-05-25"),
)

private val previewDoneBooks = listOf(
    LibraryBook(groupId = 3, memberBookId = 3, groupName = "한 달 한 권", title = "1984", author = "조지 오웰", rating = 5, startDate = "2026-04-01", endDate = "2026-04-20"),
)

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun LibraryScreenPreview() {
    BookiiPreview {
        LibraryScreen(readingBooks = previewReadingBooks, doneBooks = previewDoneBooks)
    }
}
