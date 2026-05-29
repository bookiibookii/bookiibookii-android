package com.bookiibookii.bookiibookii.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private val searchFieldShape = RoundedCornerShape(
    topStart = 20.dp, topEnd = 30.dp,
    bottomStart = 20.dp, bottomEnd = 30.dp,
)

@Composable
internal fun HomeSearchCreateRow(
    onSearchClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.white)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 검색 input — 피그마: tl=20 tr=30 bl=20 br=30, h=56dp, border grey/200
        Row(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(searchFieldShape)
                .background(colors.white)
                .border(1.dp, colors.grey200, searchFieldShape)
                .clickable(onClick = onSearchClick)
                .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "그룹을 검색해보세요",
                style = typography.regular16,
                color = colors.grey500,
                modifier = Modifier.weight(1f),
            )
            // 검색 버튼 — 피그마: grey/300 bg, 44dp, rounded=30dp
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(colors.grey300),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "검색",
                    tint = colors.white,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // 그룹 생성 버튼 — 피그마: orange, h=56dp, rounded=999dp
        Box(
            modifier = Modifier
                .height(56.dp)
                .clip(BookiiBookiiTheme.shape.round50)
                .background(colors.uiMain)
                .clickable(onClick = onCreateGroupClick)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "그룹 생성",
                style = typography.regular16,
                color = colors.white,
            )
        }
    }
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "HomeSearchCreateRow")
@Composable
private fun HomeSearchCreateRowPreview() {
    BookiiPreview {
        HomeSearchCreateRow(
            onSearchClick = {},
            onCreateGroupClick = {},
        )
    }
}
