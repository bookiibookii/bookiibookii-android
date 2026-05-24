package com.bookiibookii.bookiibookii.group.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.data.model.group.BookItem
import com.bookiibookii.bookiibookii.ui.component.BookCover
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 도서 검색 결과 드롭다운
@Composable
fun BookSearchDropdown(
    books: List<BookItem>,
    onBookClick: (BookItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = BookiiBookiiTheme.shape.round16
    LazyColumn(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = shape)
            .clip(shape)
            .background(BookiiBookiiTheme.colors.white)
            .border(width = 1.dp, color = BookiiBookiiTheme.colors.grey200, shape = shape),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(books) { book ->
            BookCard(book = book, onClick = { onBookClick(book) })
        }
    }
}

@Composable
private fun BookCard(
    book: BookItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        BookCover(
            modifier = Modifier
                .width(48.dp)
                .height(60.dp),
            imageUrl = book.image,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = book.title,
                style = BookiiBookiiTheme.typography.medium16,
                color = BookiiBookiiTheme.colors.grey800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${book.author} (${book.categoryLabel})",
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(widthDp = 320, showBackground = true, backgroundColor = 0xFFF6F6F6)
@Composable
private fun BookSearchDropdownPreview() {
    BookiiPreview {
        BookSearchDropdown(
            books = List(6) {
                BookItem(
                    title = "살인자의 기억법",
                    author = "김영하",
                    image = "",
                    publisher = "문학동네",
                    isbn13 = "9788954636049",
                    category = "novel",
                    categoryLabel = "한국소설",
                    link = "",
                )
            },
            onBookClick = {},
            modifier = Modifier.heightIn(max = 320.dp),
        )
    }
}
