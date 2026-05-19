package com.bookiibookii.bookiibookii.group.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
fun SearchInputButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "그룹명, 도서명, 저자로 검색",
) {
    val shape = RoundedCornerShape(
        topStart = 20.dp,
        bottomStart = 20.dp,
        topEnd = 30.dp,
        bottomEnd = 30.dp,
    )
    Row(
        modifier = modifier
            .clip(shape)
            .background(BookiiBookiiTheme.colors.white)
            .border(width = 1.dp, color = BookiiBookiiTheme.colors.grey200, shape = shape)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = hint,
            style = BookiiBookiiTheme.typography.regular15,
            color = BookiiBookiiTheme.colors.grey500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = BookiiBookiiTheme.colors.grey300,
                    shape = BookiiBookiiTheme.shape.round50,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.white,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Preview
@Composable
private fun SearchInputButtonPreview() {
    BookiiPreview {
        SearchInputButton(onClick = {})
    }
}
