package com.bookiibookii.bookiibookii.library.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun LibraryBookGridItem(book: LibraryBook, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(119f / 170f)
                .clip(RoundedCornerShape(10.dp))
                .background(BookiiBookiiTheme.colors.grey200),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = book.groupName, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey600, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = book.title, style = BookiiBookiiTheme.typography.semibold15, color = BookiiBookiiTheme.colors.grey900, maxLines = 1, overflow = TextOverflow.Ellipsis)
            when {
                book.progress != null -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50.dp)).background(BookiiBookiiTheme.colors.grey200)) {
                        Box(modifier = Modifier.fillMaxWidth(book.progress).height(6.dp).clip(RoundedCornerShape(50.dp)).background(BookiiBookiiTheme.colors.grey800))
                    }
                    Text(text = "${(book.progress * 100).toInt()}%", style = BookiiBookiiTheme.typography.semibold12, color = BookiiBookiiTheme.colors.grey800)
                }
                book.rating != null -> {
                    Row {
                        for (i in 1..5) {
                            Icon(
                                painter = painterResource(R.drawable.ic_star),
                                contentDescription = null,
                                tint = if (i <= book.rating) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey200,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun LibraryBookListItem(book: LibraryBook, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BookiiBookiiTheme.colors.white)
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = book.groupName, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey600, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = book.title, style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900, maxLines = 1, overflow = TextOverflow.Ellipsis)
        when {
            book.progress != null -> {
                Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50.dp)).background(BookiiBookiiTheme.colors.grey200)) {
                    Box(modifier = Modifier.fillMaxWidth(book.progress).height(6.dp).clip(RoundedCornerShape(50.dp)).background(BookiiBookiiTheme.colors.grey800))
                }
                Text(text = "${(book.progress * 100).toInt()}%", style = BookiiBookiiTheme.typography.semibold12, color = BookiiBookiiTheme.colors.grey800)
            }
            book.rating != null -> {
                Row {
                    for (i in 1..5) {
                        Icon(
                            painter = painterResource(R.drawable.ic_star),
                            contentDescription = null,
                            tint = if (i <= book.rating) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey200,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}
