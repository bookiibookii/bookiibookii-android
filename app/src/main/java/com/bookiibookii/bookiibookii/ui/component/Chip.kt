package com.bookiibookii.bookiibookii.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = BookiiBookiiTheme.shape.round26
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(
                if (selected) BookiiBookiiTheme.colors.grey900 else BookiiBookiiTheme.colors.white,
            )
            .then(
                if (selected) {
                    Modifier
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = BookiiBookiiTheme.colors.grey200,
                        shape = shape,
                    )
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.regular14,
            color = if (selected) BookiiBookiiTheme.colors.white else BookiiBookiiTheme.colors.grey900,
            maxLines = 1,
        )
    }
}

//@Composable
//fun BottomSheetChip()

@Preview
@Composable
private fun FilterChipPreview() {
    BookiiPreview {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(text = "교환 방식", selected = false, onClick = {})
            FilterChip(text = "분야별", selected = true, onClick = {})
        }
    }
}
