package com.bookiibookii.bookiibookii.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// "삭제" 한 줄 팝오버
@Composable
fun DeletePopover(
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = BookiiBookiiTheme.shape.round10)
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round10,
            )
            .border(
                width = 1.dp,
                color = BookiiBookiiTheme.colors.grey200,
                shape = BookiiBookiiTheme.shape.round10,
            )
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDeleteClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "삭제",
                style = BookiiBookiiTheme.typography.medium14,
                color = BookiiBookiiTheme.colors.grey700,
            )
            Icon(
                painter = painterResource(R.drawable.ic_trash),
                contentDescription = "삭제",
                tint = BookiiBookiiTheme.colors.grey700,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Preview
@Composable
private fun DeletePopoverPreview() {
    BookiiPreview {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            DeletePopover(onDeleteClick = {})
        }
    }
}
