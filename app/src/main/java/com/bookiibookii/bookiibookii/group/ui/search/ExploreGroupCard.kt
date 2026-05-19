package com.bookiibookii.bookiibookii.group.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme


// 그룹 검색 메인 카드 아이템
@Composable
fun ExploreGroupCard(
    title: String,
    author: String,
    category: String,
    exchangeType: String,
    expectedDays: Int,
    nickname: String,
    groupName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round20)
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 책 썸네일 placeholder
        Box(
            modifier = Modifier
                .size(width = 72.dp, height = 100.dp)
                .clip(BookiiBookiiTheme.shape.round8)
                .background(BookiiBookiiTheme.colors.uiBg),
        )

        Column(
            modifier = Modifier
                .height(100.dp)
                .weight(1f),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ExchangeTypeBadge(text = exchangeType)
                    Text(
                        text = title,
                        style = BookiiBookiiTheme.typography.medium16,
                        color = BookiiBookiiTheme.colors.grey900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = "$author($category)",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "예상 독서 기간",
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey700,
                    )
                    Row {
                        Text(
                            text = "$expectedDays",
                            style = BookiiBookiiTheme.typography.regular14,
                            color = BookiiBookiiTheme.colors.grey800,
                        )
                        Text(
                            text = "일",
                            style = BookiiBookiiTheme.typography.regular14,
                            color = BookiiBookiiTheme.colors.grey700,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 프로필 이미지 placeholder
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(BookiiBookiiTheme.shape.round50)
                            .background(BookiiBookiiTheme.colors.uiBg),
                    )
                    Text(
                        text = nickname,
                        style = BookiiBookiiTheme.typography.regular15,
                        color = BookiiBookiiTheme.colors.grey700,
                    )
                    Text(
                        text = "·",
                        style = BookiiBookiiTheme.typography.regular15,
                        color = BookiiBookiiTheme.colors.grey700,
                    )
                    Text(
                        text = groupName,
                        style = BookiiBookiiTheme.typography.regular15,
                        color = BookiiBookiiTheme.colors.grey500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// 카드 내부 교환 방식 라벨 배지
@Composable
private fun ExchangeTypeBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(BookiiBookiiTheme.shape.round8)
            .background(BookiiBookiiTheme.colors.uiMainPale)
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.medium11,
            color = BookiiBookiiTheme.colors.uiMain,
        )
    }
}

@Preview
@Composable
private fun ExploreGroupCardPreview() {
    BookiiPreview {
        ExploreGroupCard(
            title = "살인자의 기억법",
            author = "김영하",
            category = "한국소설",
            exchangeType = "직접",
            expectedDays = 7,
            nickname = "닉네임",
            groupName = "그룹명",
        )
    }
}
