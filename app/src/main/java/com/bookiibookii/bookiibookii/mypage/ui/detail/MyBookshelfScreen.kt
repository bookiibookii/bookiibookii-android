package com.bookiibookii.bookiibookii.mypage.ui.detail

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.mypage.CompletedBook
import com.bookiibookii.bookiibookii.data.model.mypage.FavoriteBook
import com.bookiibookii.bookiibookii.data.model.mypage.RepresentativeBook
import com.bookiibookii.bookiibookii.mypage.vm.SortOrder
import com.bookiibookii.bookiibookii.onboarding.steps.model.BookSearchState
import com.bookiibookii.bookiibookii.onboarding.steps.ui.component.LifeBookSearchDialog

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white)
        ) {
            BookshelfTopBar(onBack = onBack)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            RepresentativeBookSection(
                books = representativeBooks,
                onEditClick = { showEditBottomSheet = true },
            )

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
            onRemove = { book ->
                editableRepresentativeBooks.remove(book)
                onDeleteRepresentativeBook(book.userBookId)
            },
            onMove = { fromIndex, toIndex ->
                val temp = editableRepresentativeBooks[fromIndex]
                editableRepresentativeBooks[fromIndex] = editableRepresentativeBooks[toIndex]
                editableRepresentativeBooks[toIndex] = temp
            },
            onReorder = { userBookId, newOrder ->
                onReorderRepresentativeBook(userBookId, newOrder)
            },
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
            onAddRepresentativeClick = { memberBookId ->
                showBookBottomSheet = false
                onAddRepresentativeBook(memberBookId)
            },
            onRemoveRepresentativeClick = { userBookId ->
                showBookBottomSheet = false
                onRemoveRepresentativeBook(userBookId)
            },
            onLibraryClick = { showBookBottomSheet = false },
            onAladinClick = { showBookBottomSheet = false },
        )
    }
}

@Composable
private fun BookshelfTopBar(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "뒤로 가기",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(32.dp),
                )
            }
            Text(
                text = "나의 책장",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                textAlign = TextAlign.Center,
            )
            IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "검색 하기",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 0.5.dp)
    }
}

@Composable
private fun RepresentativeBookSection(
    books: List<RepresentativeBook>,
    onEditClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "나를 대표하는 책",
                style = BookiiBookiiTheme.typography.semibold16,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.dp, BookiiBookiiTheme.colors.grey200),
                color = BookiiBookiiTheme.colors.white,
            ) {
                Text(
                    text = "${books.size}/7권",
                    style = BookiiBookiiTheme.typography.regular11,
                    color = BookiiBookiiTheme.colors.grey700,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = BookiiBookiiTheme.colors.grey200,
                modifier = Modifier.clickable { onEditClick() },
            ) {
                Text(
                    text = "수정",
                    style = BookiiBookiiTheme.typography.regular11,
                    color = BookiiBookiiTheme.colors.grey700,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val gap = 8.dp
            val itemWidth = (maxWidth - gap * 6) / 7

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.Bottom,
            ) {
                books.forEach { book ->
                    BookSpineItemLocal(title = book.title, isOrange = !book.isFavorite, width = itemWidth)
                }
            }
        }
    }
}

@Composable
private fun BookSpineItemLocal(title: String, isOrange: Boolean, width: Dp) {
    val bgColor = if (isOrange) BookiiBookiiTheme.colors.uiMain150 else BookiiBookiiTheme.colors.uiMainSubPale
    val textColor = if (isOrange) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.uiMainSub
    val archHeight = 15.dp

    Box(contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier
                .padding(top = archHeight / 2)
                .width(width)
                .background(color = bgColor, shape = RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                .padding(top = 24.dp, bottom = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                style = BookiiBookiiTheme.typography.medium16,
                color = textColor,
                maxLines = 1,
                modifier = Modifier.verticalRotation(),
            )
        }
        Canvas(modifier = Modifier.width(width).height(archHeight)) {
            val path = Path().apply {
                arcTo(
                    rect = Rect(0f, 0f, size.width, size.height),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = true,
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, color = bgColor)
        }
    }
}

@Composable
private fun LifeBookSection(
    lifeBooks: List<FavoriteBook>,
    bookSearchState: BookSearchState?,
    onSearchBooks: (String) -> Unit,
    onClearSearch: () -> Unit,
    onAddFavoriteBook: (String) -> Unit,
    onDeleteFavoriteBook: (Long) -> Unit,
    onReplaceFavoriteBook: (Long, String) -> Unit,
) {
    var showSearchDialog by remember { mutableStateOf(false) }
    var editingBook by remember { mutableStateOf<FavoriteBook?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "나의 인생 책",
                style = BookiiBookiiTheme.typography.semibold16,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.dp, BookiiBookiiTheme.colors.grey200),
                color = BookiiBookiiTheme.colors.white,
            ) {
                Text(
                    text = "${lifeBooks.size}/3권",
                    style = BookiiBookiiTheme.typography.regular11,
                    color = BookiiBookiiTheme.colors.grey700,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            for (slotIndex in 0..2) {
                val book = lifeBooks.getOrNull(slotIndex)
                if (book != null) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.fillMaxWidth().aspectRatio(119f / 170f)) {
                            if (book.image != null) {
                                AsyncImage(
                                    model = book.image,
                                    contentDescription = book.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(10.dp)),
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BookiiBookiiTheme.colors.grey200),
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.9f))
                                    .clickable {
                                        if (lifeBooks.size == 1) {
                                            editingBook = book
                                            showSearchDialog = true
                                        } else {
                                            onDeleteFavoriteBook(book.userBookId)
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (lifeBooks.size == 1) R.drawable.ic_edit else R.drawable.ic_x
                                    ),
                                    contentDescription = if (lifeBooks.size == 1) "수정" else "삭제",
                                    tint = BookiiBookiiTheme.colors.grey700,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = book.title,
                                style = BookiiBookiiTheme.typography.semibold14,
                                color = BookiiBookiiTheme.colors.grey900,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = book.author ?: "",
                                style = BookiiBookiiTheme.typography.regular14,
                                color = BookiiBookiiTheme.colors.grey700,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { editingBook = null; showSearchDialog = true },
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(119f / 170f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BookiiBookiiTheme.colors.grey100),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(BookiiBookiiTheme.colors.grey200),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_plus),
                                    contentDescription = "추가",
                                    tint = BookiiBookiiTheme.colors.white,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(52.dp))
                    }
                }
            }
        }
    }

    if (showSearchDialog) {
        LifeBookSearchDialog(
            bookSearchState = bookSearchState,
            onQueryChange = onSearchBooks,
            onBookSelected = { selectedBook ->
                val editing = editingBook
                if (editing != null) {
                    onReplaceFavoriteBook(editing.userBookId, selectedBook.isbn13)
                } else {
                    onAddFavoriteBook(selectedBook.isbn13)
                }
                showSearchDialog = false
                editingBook = null
                onClearSearch()
            },
            onDismiss = {
                showSearchDialog = false
                editingBook = null
                onClearSearch()
            },
        )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onViewModeToggle, modifier = Modifier.size(24.dp)) {
            val iconRes = if (viewMode == BookViewMode.GRID) R.drawable.ic_album else R.drawable.ic_line
            Icon(
                painter = painterResource(iconRes),
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
            if (index > 0) {
                Text(text = " | ", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
            }
            val isSelected = sortOrder == order
            Text(
                text = label,
                style = if (isSelected) BookiiBookiiTheme.typography.semibold14 else BookiiBookiiTheme.typography.regular14,
                color = if (isSelected) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey500,
                modifier = Modifier.clickable { onSortOrderChange(order) },
            )
        }
    }
}

@Composable
private fun BookGridView(
    books: List<CompletedBook>,
    representativeTitles: Set<String>,
    onBookClick: (CompletedBook) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        books.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowItems.forEach { book ->
                    BookGridItem(
                        book = book,
                        isRepresentative = book.title in representativeTitles,
                        modifier = Modifier.weight(1f),
                        onClick = { onBookClick(book) },
                    )
                }
                repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BookGridItem(
    book: CompletedBook,
    isRepresentative: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(modifier = modifier.clickable { onClick() }) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(119f / 170f)) {
            if (book.image != null) {
                AsyncImage(
                    model = book.image,
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().clip(RoundedCornerShape(10.dp)),
                )
            } else {
                Box(modifier = Modifier.matchParentSize().clip(RoundedCornerShape(10.dp)).background(BookiiBookiiTheme.colors.grey200))
            }
            if (isRepresentative) {
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BookiiBookiiTheme.colors.uiMainPale)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopStart),
                ) {
                    Text(text = "대표", style = BookiiBookiiTheme.typography.medium11, color = BookiiBookiiTheme.colors.uiMain)
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        if (book.completedAt != null) {
            Text(text = book.completedAt, style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey700)
        }
        Text(text = book.title, style = BookiiBookiiTheme.typography.semibold14, color = BookiiBookiiTheme.colors.grey900, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            text = listOfNotNull(book.author, book.category).joinToString(" "),
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        StarRatingRow(rating = book.rating)
    }
}

@Composable
private fun BookListView(
    books: List<CompletedBook>,
    representativeTitles: Set<String>,
    onBookClick: (CompletedBook) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        books.forEach { book ->
            BookListItem(
                book = book,
                isRepresentative = book.title in representativeTitles,
                onClick = { onBookClick(book) },
            )
        }
    }
}

@Composable
private fun BookListItem(
    book: CompletedBook,
    isRepresentative: Boolean,
    onClick: () -> Unit = {},
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = BookiiBookiiTheme.colors.white,
        border = BorderStroke(1.dp, BookiiBookiiTheme.colors.grey100),
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (isRepresentative) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BookiiBookiiTheme.colors.uiMainPale)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(text = "대표", style = BookiiBookiiTheme.typography.regular11, color = BookiiBookiiTheme.colors.uiMain)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = book.title,
                        style = BookiiBookiiTheme.typography.semibold14,
                        color = BookiiBookiiTheme.colors.grey900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                StarRatingRow(rating = book.rating)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = listOfNotNull(book.author, book.category).joinToString(" "),
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey700,
                )
                if (book.completedAt != null) {
                    Text(text = book.completedAt, style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey500)
                }
            }
        }
    }
}

@Composable
private fun StarRatingRow(rating: Double) {
    Row(horizontalArrangement = Arrangement.spacedBy((-2).dp)) {
        for (i in 1..5) {
            val starValue = (rating - (i - 1)).coerceIn(0.0, 1.0)
            StarIcon(starValue = starValue)
        }
    }
}

@Composable
private fun StarIcon(starValue: Double) {
    val colors = BookiiBookiiTheme.colors
    when {
        starValue >= 0.75 -> Icon(
            painter = painterResource(R.drawable.ic_star_fill),
            contentDescription = null,
            tint = colors.uiMainSub,
            modifier = Modifier.size(16.dp),
        )
        starValue >= 0.25 -> Box(modifier = Modifier.size(16.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_star_fill),
                contentDescription = null,
                tint = colors.uiMainSub150,
                modifier = Modifier.size(16.dp),
            )
            Icon(
                painter = painterResource(R.drawable.ic_star),
                contentDescription = null,
                tint = colors.uiMainSub,
                modifier = Modifier.size(16.dp),
            )
        }
        else -> Icon(
            painter = painterResource(R.drawable.ic_star),
            contentDescription = null,
            tint = colors.grey200,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MyBookshelfScreenPreview() {
    MyBookshelfScreen()
}
