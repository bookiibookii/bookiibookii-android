package com.bookiibookii.bookiibookii.mypage.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.mypage.CompletedBook
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun BookGridView(
    books: List<CompletedBook>,
    representativeTitles: Set<String>,
    onBookClick: (CompletedBook) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        books.chunked(3).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { book ->
                    BookGridItem(book = book, isRepresentative = book.title in representativeTitles, modifier = Modifier.weight(1f), onClick = { onBookClick(book) })
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
                AsyncImage(model = book.image, contentDescription = book.title, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(RoundedCornerShape(10.dp)))
            } else {
                Box(modifier = Modifier.matchParentSize().clip(RoundedCornerShape(10.dp)).background(BookiiBookiiTheme.colors.grey200))
            }
            if (isRepresentative) {
                Box(
                    modifier = Modifier.padding(6.dp).clip(RoundedCornerShape(8.dp)).background(BookiiBookiiTheme.colors.uiMainPale).padding(horizontal = 8.dp, vertical = 4.dp).align(Alignment.TopStart),
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
        BookshelfStarRatingRow(rating = book.rating)
    }
}

@Composable
internal fun BookListView(
    books: List<CompletedBook>,
    representativeTitles: Set<String>,
    onBookClick: (CompletedBook) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        books.forEach { book ->
            BookListItem(book = book, isRepresentative = book.title in representativeTitles, onClick = { onBookClick(book) })
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (isRepresentative) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(BookiiBookiiTheme.colors.uiMainPale).padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(text = "대표", style = BookiiBookiiTheme.typography.regular11, color = BookiiBookiiTheme.colors.uiMain)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(text = book.title, style = BookiiBookiiTheme.typography.semibold14, color = BookiiBookiiTheme.colors.grey900, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                BookshelfStarRatingRow(rating = book.rating)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = listOfNotNull(book.author, book.category).joinToString(" "), style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey700)
                if (book.completedAt != null) {
                    Text(text = book.completedAt, style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey500)
                }
            }
        }
    }
}

@Composable
internal fun BookshelfStarRatingRow(rating: Double) {
    Row(horizontalArrangement = Arrangement.spacedBy((-2).dp)) {
        for (i in 1..5) {
            BookshelfStarIcon(starValue = (rating - (i - 1)).coerceIn(0.0, 1.0))
        }
    }
}

@Composable
private fun BookshelfStarIcon(starValue: Double) {
    val colors = BookiiBookiiTheme.colors
    when {
        starValue >= 0.75 -> Icon(painter = painterResource(R.drawable.ic_star_fill), contentDescription = null, tint = colors.uiMainSub, modifier = Modifier.size(16.dp))
        starValue >= 0.25 -> Box(modifier = Modifier.size(16.dp)) {
            Icon(painter = painterResource(R.drawable.ic_star_fill), contentDescription = null, tint = colors.uiMainSub150, modifier = Modifier.size(16.dp))
            Icon(painter = painterResource(R.drawable.ic_star), contentDescription = null, tint = colors.uiMainSub, modifier = Modifier.size(16.dp))
        }
        else -> Icon(painter = painterResource(R.drawable.ic_star), contentDescription = null, tint = colors.grey200, modifier = Modifier.size(16.dp))
    }
}
