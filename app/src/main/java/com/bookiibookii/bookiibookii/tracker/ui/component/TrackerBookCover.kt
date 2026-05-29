package com.bookiibookii.bookiibookii.tracker.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.ui.component.BookCover
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
fun TrackerBookCover(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    isOwnerBook: Boolean = false,
) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {
        BookCover(
            modifier = Modifier.matchParentSize(),
            imageUrl = imageUrl,
        )
        if (isOwnerBook) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(BookiiBookiiTheme.shape.round4)
                    .background(BookiiBookiiTheme.colors.grey200.copy(alpha = 0.75f))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "내 책",
                    style = BookiiBookiiTheme.typography.regular10,
                    color = BookiiBookiiTheme.colors.grey900,
                )
            }
        }
    }
}
