package com.bookiibookii.bookiibookii.mypage.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.mypage.FavoriteBook
import com.bookiibookii.bookiibookii.data.model.mypage.RepresentativeBook
import com.bookiibookii.bookiibookii.onboarding.steps.model.BookSearchState
import com.bookiibookii.bookiibookii.onboarding.steps.ui.component.LifeBookSearchDialog
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun RepresentativeBookSection(
    books: List<RepresentativeBook>,
    onEditClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(text = "나를 대표하는 책", style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900)
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.dp, BookiiBookiiTheme.colors.grey200),
                color = BookiiBookiiTheme.colors.white,
            ) {
                Text(text = "${books.size}/7권", style = BookiiBookiiTheme.typography.regular11, color = BookiiBookiiTheme.colors.grey700, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Surface(shape = RoundedCornerShape(50.dp), color = BookiiBookiiTheme.colors.grey200, modifier = Modifier.clickable { onEditClick() }) {
                Text(text = "수정", style = BookiiBookiiTheme.typography.regular11, color = BookiiBookiiTheme.colors.grey700, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val gap = 8.dp
            val itemWidth = (maxWidth - gap * 6) / 7
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.Bottom) {
                books.forEachIndexed { index, book ->
                    BookSpineItemLocal(title = book.title, isOrange = index % 2 == 0, width = itemWidth)
                }
            }
        }
    }
}

@Composable
private fun BookSpineItemLocal(title: String, isOrange: Boolean, width: Dp) {
    val bgColor   = if (isOrange) BookiiBookiiTheme.colors.uiMain150 else BookiiBookiiTheme.colors.uiMainSubPale
    val textColor = if (isOrange) BookiiBookiiTheme.colors.uiMain     else BookiiBookiiTheme.colors.uiMainSub
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
            Text(text = title, style = BookiiBookiiTheme.typography.medium16, color = textColor, maxLines = 1, modifier = Modifier.verticalRotation())
        }
        Canvas(modifier = Modifier.width(width).height(archHeight)) {
            val path = Path().apply {
                arcTo(rect = Rect(0f, 0f, size.width, size.height), startAngleDegrees = 180f, sweepAngleDegrees = 180f, forceMoveTo = true)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, color = bgColor)
        }
    }
}

@Composable
internal fun LifeBookSection(
    lifeBooks: List<FavoriteBook>,
    bookSearchState: BookSearchState?,
    onSearchBooks: (String) -> Unit,
    onClearSearch: () -> Unit,
    onAddFavoriteBook: (String) -> Unit,
    onDeleteFavoriteBook: (Long) -> Unit,
    onReplaceFavoriteBook: (Long, String) -> Unit,
    onSearch: () -> Unit = {},
) {
    var showSearchDialog by remember { mutableStateOf(false) }
    var editingBook by remember { mutableStateOf<FavoriteBook?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "나의 인생 책", style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900)
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.dp, BookiiBookiiTheme.colors.grey200),
                color = BookiiBookiiTheme.colors.white,
            ) {
                Text(text = "${lifeBooks.size}/3권", style = BookiiBookiiTheme.typography.regular11, color = BookiiBookiiTheme.colors.grey700, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            for (slotIndex in 0..2) {
                val book = lifeBooks.getOrNull(slotIndex)
                if (book != null) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.fillMaxWidth().aspectRatio(119f / 170f)) {
                            if (book.image != null) {
                                AsyncImage(model = book.image, contentDescription = book.title, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(RoundedCornerShape(10.dp)))
                            } else {
                                Box(modifier = Modifier.matchParentSize().clip(RoundedCornerShape(10.dp)).background(BookiiBookiiTheme.colors.grey200))
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.9f))
                                    .clickable {
                                        if (lifeBooks.size == 1) { editingBook = book; showSearchDialog = true }
                                        else { onDeleteFavoriteBook(book.userBookId) }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(if (lifeBooks.size == 1) R.drawable.ic_edit else R.drawable.ic_x),
                                    contentDescription = if (lifeBooks.size == 1) "수정" else "삭제",
                                    tint = BookiiBookiiTheme.colors.grey700,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Column(modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(text = book.title, style = BookiiBookiiTheme.typography.semibold14, color = BookiiBookiiTheme.colors.grey900, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = book.author ?: "", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey700, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                } else {
                    Column(modifier = Modifier.weight(1f).clickable { editingBook = null; showSearchDialog = true }) {
                        Box(
                            modifier = Modifier.fillMaxWidth().aspectRatio(119f / 170f).clip(RoundedCornerShape(10.dp)).background(BookiiBookiiTheme.colors.grey100),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BookiiBookiiTheme.colors.grey200), contentAlignment = Alignment.Center) {
                                Icon(painter = painterResource(R.drawable.ic_plus), contentDescription = "추가", tint = BookiiBookiiTheme.colors.white, modifier = Modifier.size(24.dp))
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
            onSearch = onSearch,
            onBookSelected = { selectedBook ->
                val editing = editingBook
                if (editing != null) onReplaceFavoriteBook(editing.userBookId, selectedBook.isbn13)
                else onAddFavoriteBook(selectedBook.isbn13)
                showSearchDialog = false
                editingBook = null
                onClearSearch()
            },
            onDismiss = { showSearchDialog = false; editingBook = null; onClearSearch() },
        )
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun RepresentativeBookSectionPreview() {
    BookiiPreview {
        RepresentativeBookSection(
            books = listOf(
                RepresentativeBook(userBookId = 1L, title = "데미안", displayOrder = 0, isFavorite = true),
                RepresentativeBook(userBookId = 2L, title = "1984", displayOrder = 1, isFavorite = false),
                RepresentativeBook(userBookId = 3L, title = "사피엔스", displayOrder = 2, isFavorite = false),
            ),
            onEditClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun LifeBookSectionPreview() {
    BookiiPreview {
        LifeBookSection(
            lifeBooks = listOf(
                FavoriteBook(userBookId = 10L, title = "데미안", author = "헤르만 헤세", category = "(소설)", image = null),
                FavoriteBook(userBookId = 11L, title = "1984", author = "조지 오웰", category = "(소설)", image = null),
            ),
            bookSearchState = null,
            onSearchBooks = {},
            onClearSearch = {},
            onAddFavoriteBook = {},
            onDeleteFavoriteBook = {},
            onReplaceFavoriteBook = { _, _ -> },
        )
    }
}
