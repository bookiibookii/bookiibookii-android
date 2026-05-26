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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// "삭제" 한 줄짜리 dropdown
// - 사용처: 댓글 long-press 시 본인 댓글 아래에 나타남
// - 클릭 영역 전체에서 onDeleteClick 호출
@Composable
fun DeletePopover(
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = shape)
            .clip(shape)
            .background(BookiiBookiiTheme.colors.white)
            .border(width = 1.dp, color = BookiiBookiiTheme.colors.grey200, shape = shape)
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDeleteClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
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
