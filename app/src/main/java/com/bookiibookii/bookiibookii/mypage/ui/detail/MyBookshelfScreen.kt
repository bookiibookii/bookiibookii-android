package com.bookiibookii.bookiibookii.mypage.ui.detail

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R

enum class BookViewMode { GRID, LIST }
enum class SortOrder { LATEST, OLDEST, RATING, TITLE }

data class MockBookItem(
    val title: String,
    val author: String,
    val genre: String,
    val rating: Int,
    val isRepresentative: Boolean = false,
    val date: String? = null,
    val isOrange: Boolean = true,
)

data class MockSpineBook(
    val title: String,
    val isOrange: Boolean = true,
)

val mockBookItems = listOf(
    MockBookItem("프로젝트 헤일메리", "앤디 위어", "(소설)", 4, true, "2026.04.05."),
    MockBookItem("고쳐 쓰는 마음", "김연수", "(소설)", 3, true, "2026.04.05.", false),
    MockBookItem("채식주의자", "한강", "(소설)", 5, true, "2026.04.05."),
    MockBookItem("괴테는 모든 것을 말했다", "괴테", "(소설)", 4, true, "2026.04.05."),
    MockBookItem("인생을 위한 생각", "김스카이", "(에세이)", 4, true, "2026.04.05.", false),
    MockBookItem("모국어는 차라리 침묵", "김연수", "(소설)", 4, true, "2026.04.05."),
    MockBookItem("어린 개가 왔다", "김연수", "(소설)", 3, true, "2026.04.05.")
)

val mockSpineBooks = listOf(
    MockSpineBook("없어질 행성에서 씁니다", true),
    MockSpineBook("어린 개가 왔다", true),
    MockSpineBook("고쳐 쓰는 마음", false),
    MockSpineBook("모국어는 차라리 침묵", true),
    MockSpineBook("괴테는 모든 것을 말했다", true),
    MockSpineBook("인생을 위한 최소한의 생각", false),
    MockSpineBook("어린 개가 왔다", false),
)

@Composable
fun MyBookshelfScreen(
    onBack: () -> Unit = {},
) {
    var viewMode by remember { mutableStateOf(BookViewMode.GRID) }
    var sortOrder by remember { mutableStateOf(SortOrder.LATEST) }
    var showEditBottomSheet by remember { mutableStateOf(false) }
    var showBookSearchDialog by remember { mutableStateOf(false) }
    var showBookBottomSheet by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<MockBookItem?>(null) }

    val representativeBooks = remember { mutableStateListOf(*mockBookItems.toTypedArray()) }

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

            RepresentativeBookSection(onEditClick = { showEditBottomSheet = true })

            Spacer(modifier = Modifier.height(8.dp))

            LifeBookSection(
                onAddLifeBookClick = { showBookSearchDialog = true },
                onEditClick = { showBookSearchDialog = true },
            )

            FilterBar(
                viewMode = viewMode,
                sortOrder = sortOrder,
                onViewModeToggle = {
                    viewMode = if (viewMode == BookViewMode.GRID) BookViewMode.LIST else BookViewMode.GRID
                },
                onSortOrderChange = { sortOrder = it },
            )

            if (viewMode == BookViewMode.GRID) {
                BookGridView(
                    books = mockBookItems,
                    onBookClick = { book ->
                        selectedBook = book
                        showBookBottomSheet = true
                    },
                )
            } else {
                BookListView(
                    books = mockBookItems,
                    onBookClick = { book ->
                        selectedBook = book
                        showBookBottomSheet = true
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }

    if (showEditBottomSheet) {
        RepresentativeEditBottomSheet(
            bookList = representativeBooks,
            onDismiss = { showEditBottomSheet = false },
            onRemove = { bookToRemove ->
                representativeBooks.remove(bookToRemove)
            },
            onMove = { fromIndex, toIndex ->
                val temp = representativeBooks[fromIndex]
                representativeBooks[fromIndex] = representativeBooks[toIndex]
                representativeBooks[toIndex] = temp
            }
        )
    }

    if (showBookSearchDialog) {
        BookSearchBottomSheet(onDismiss = { showBookSearchDialog = false })
    }

    if (showBookBottomSheet && selectedBook != null) {
        val book = selectedBook!!
        BookshelfBookBottomSheet(
            title = book.title,
            author = book.author,
            genre = book.genre.trim('(', ')'),
            isRepresentative = book.isRepresentative,
            onDismiss = { showBookBottomSheet = false },
            onReviewClick = { showBookBottomSheet = false },
            onToggleRepresentativeClick = { showBookBottomSheet = false },
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
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp),
            ) {
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
private fun RepresentativeBookSection(onEditClick: () -> Unit) {
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
                    text = "7/7권",
                    style = BookiiBookiiTheme.typography.regular11,
                    color = BookiiBookiiTheme.colors.grey700,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = BookiiBookiiTheme.colors.grey200,
                modifier = Modifier.clickable { onEditClick() }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            mockSpineBooks.forEach { book ->
                BookSpineItemLocal(title = book.title, isOrange = book.isOrange)
            }
        }
    }
}

@Composable
private fun BookSpineItemLocal(title: String, isOrange: Boolean) {
    val bgColor = if (isOrange) BookiiBookiiTheme.colors.uiMain150 else BookiiBookiiTheme.colors.uiMainSubPale
    val textColor = if (isOrange) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.uiMainSub
    val archHeight = 15.dp

    Box(contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier
                .padding(top = archHeight / 2)
                .width(42.dp)
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
        Canvas(modifier = Modifier.width(42.dp).height(archHeight)) {
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
    onAddLifeBookClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
) {
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
                    text = "1/3권",
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
            // 등록된 책
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(119f / 170f)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BookiiBookiiTheme.colors.grey200),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f))
                            .clickable { onEditClick() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit),
                            contentDescription = "수정",
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
                    Text(text = "프로젝트 헤일메리", style = BookiiBookiiTheme.typography.semibold14, color = BookiiBookiiTheme.colors.grey900, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "앤디 위어", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey700, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            // 빈 슬롯 1
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAddLifeBookClick() },
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
            // 빈 슬롯 2
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAddLifeBookClick() },
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
                            .background(BookiiBookiiTheme.colors.grey300),
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

@Composable
private fun FilterBar(
    viewMode: BookViewMode,
    sortOrder: SortOrder,
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
            Icon(painter = painterResource(iconRes), contentDescription = "뷰 모드 전환", tint = BookiiBookiiTheme.colors.grey900, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "9권", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
        Spacer(modifier = Modifier.weight(1f))
        val sortOptions = listOf(SortOrder.LATEST to "최신순", SortOrder.OLDEST to "과거순", SortOrder.RATING to "별점순", SortOrder.TITLE to "제목순")
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
private fun BookGridView(books: List<MockBookItem>, onBookClick: (MockBookItem) -> Unit = {}) {
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
                    BookGridItem(book = book, modifier = Modifier.weight(1f), onClick = { onBookClick(book) })
                }
                repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BookGridItem(book: MockBookItem, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(modifier = modifier.clickable { onClick() }) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(119f / 170f)) {
            Box(modifier = Modifier.matchParentSize().clip(RoundedCornerShape(10.dp)).background(BookiiBookiiTheme.colors.grey200))
            if (book.isRepresentative) {
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
        if (book.date != null) {
            Text(text = book.date, style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey700)
        }
        Text(text = book.title, style = BookiiBookiiTheme.typography.semibold14, color = BookiiBookiiTheme.colors.grey900, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = "${book.author} ${book.genre}", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey700, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(4.dp))
        StarRatingBookshelf(rating = book.rating)
    }
}

@Composable
private fun BookListView(books: List<MockBookItem>, onBookClick: (MockBookItem) -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        books.forEach { book -> BookListItem(book = book, onClick = { onBookClick(book) }) }
    }
}

@Composable
private fun BookListItem(book: MockBookItem, onClick: () -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = BookiiBookiiTheme.colors.white,
        border = BorderStroke(1.dp, BookiiBookiiTheme.colors.grey100),
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (book.isRepresentative) {
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
                    Text(text = book.title, style = BookiiBookiiTheme.typography.semibold14, color = BookiiBookiiTheme.colors.grey900, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                StarRatingBookshelf(rating = book.rating)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${book.author} ${book.genre}", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey700)
                if (book.date != null) {
                    Text(text = book.date, style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey500)
                }
            }
        }
    }
}

@Composable
private fun StarRatingBookshelf(rating: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy((-2).dp)) {
        for (i in 1..5) {
            Icon(painter = painterResource(R.drawable.ic_star), contentDescription = null, tint = if (i <= rating) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey200, modifier = Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyBookshelfScreenPreview() {
    MyBookshelfScreen()
}
