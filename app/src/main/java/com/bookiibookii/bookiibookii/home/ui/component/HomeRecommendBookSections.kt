package com.bookiibookii.bookiibookii.home.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.data.model.group.HomeSectionItem
import com.bookiibookii.bookiibookii.ui.component.BookCover
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 캐러셀/그리드 공용. 너비는 호출처에서 modifier로 지정
@Composable
internal fun BookThumbnail(
    book: HomeSectionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography
    Column(
        modifier = modifier.clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BookCover(
            imageUrl = book.bookImage,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(120f / 168f),
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = book.title.orEmpty(),
                style = typography.regular14,
                color = colors.grey900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = book.author.orEmpty(),
                style = typography.regular14,
                color = colors.grey500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// 인기 도서 TOP 5 등 — 단순 가로 스크롤(그룹 카드 캐러셀과 달리 snap/페이지 도트 없음).
@Composable
internal fun RecommendBookRow(
    books: List<HomeSectionItem>,
    onBookClick: (HomeSectionItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(books) { book ->
            BookThumbnail(
                book = book,
                onClick = { onBookClick(book) },
                modifier = Modifier.width(120.dp),
            )
        }
    }
}

// 베스트셀러 등 — 스크롤 없는 3열 그리드(열/행 gap 8). 아이템 수만큼 자동 줄바꿈.
@Composable
internal fun RecommendBookGrid(
    books: List<HomeSectionItem>,
    onBookClick: (HomeSectionItem) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 3,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        books.chunked(columns).forEach { rowBooks ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowBooks.forEach { book ->
                    BookThumbnail(
                        book = book,
                        onClick = { onBookClick(book) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - rowBooks.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

private val mockBook = HomeSectionItem(
    isbn13 = "1", title = "소년이 온다", author = "한강", bookImage = null,
)
private val mockBooks = listOf(
    mockBook,
    mockBook.copy(isbn13 = "2", title = "채식주의자", author = "한강"),
    mockBook.copy(isbn13 = "3", title = "아몬드", author = "손원평"),
    mockBook.copy(isbn13 = "4", title = "달러구트 꿈 백화점", author = "이미예"),
    mockBook.copy(isbn13 = "5", title = "불편한 편의점", author = "김호연"),
)

@Preview(showBackground = true, name = "RecommendBookRow - 단순 스크롤")
@Composable
private fun RecommendBookRowPreview() {
    BookiiPreview {
        RecommendBookRow(books = mockBooks, onBookClick = {})
    }
}

@Preview(showBackground = true, name = "RecommendBookGrid - 그리드", widthDp = 412)
@Composable
private fun RecommendBookGridPreview() {
    BookiiPreview {
        RecommendBookGrid(
            books = mockBooks + mockBook.copy(isbn13 = "6", title = "파과", author = "구병모"),
            onBookClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
