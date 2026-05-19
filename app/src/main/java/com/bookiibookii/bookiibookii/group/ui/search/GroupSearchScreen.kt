package com.bookiibookii.bookiibookii.group.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.FilterChip
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 그룹 검색 화면
@Composable
fun GroupSearchScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron),
                        contentDescription = "뒤로가기",
                        tint = BookiiBookiiTheme.colors.grey900,
                    )
                }
                SearchInputButton(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(text = "교환 방식", selected = false, onClick = {})
                FilterChip(text = "지역별", selected = false, onClick = {})
                FilterChip(text = "분야별", selected = false, onClick = {})
            }
        }

        // 더미 데이터
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "3 권",
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey900,
            )
            ExploreGroupCard(
                title = "살인자의 기억법",
                author = "김영하",
                category = "한국소설",
                exchangeType = "직접",
                expectedDays = 7,
                nickname = "닉네임",
                groupName = "그룹명",
            )
            ExploreGroupCard(
                title = "참을 수 없는 존재의 가벼움",
                author = "밀란 쿤데라",
                category = "세계소설",
                exchangeType = "택배",
                expectedDays = 7,
                nickname = "kanghunsim",
                groupName = "자연과 함께 하는 삶",
            )
            ExploreGroupCard(
                title = "작별인사",
                author = "김영하",
                category = "한국소설",
                exchangeType = "직접",
                expectedDays = 7,
                nickname = "sayo",
                groupName = "김영하 도장깨기 하실 분",
            )
        }
    }
}

@Preview(widthDp = 412, heightDp = 917, showBackground = true)
@Composable
private fun GroupSearchScreenPreview() {
    BookiiPreview {
        GroupSearchScreen()
    }
}
