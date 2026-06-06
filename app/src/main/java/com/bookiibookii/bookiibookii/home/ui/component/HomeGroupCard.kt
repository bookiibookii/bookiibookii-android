package com.bookiibookii.bookiibookii.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.bookiibookii.bookiibookii.data.model.group.GroupItem
import com.bookiibookii.bookiibookii.ui.component.BookCover
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun HomeGroupCard(
    group: GroupItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round20)
            .background(colors.white)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BookCover(
            imageUrl = group.bookImage,
            modifier = Modifier.size(width = 72.dp, height = 100.dp),
        )

        Column(
            modifier = Modifier
                .height(100.dp)
                .weight(1f),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // 상단: 교환방식 칩 + 책 제목 + 저자(장르)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val badgeLabel = tradeTypeLabel(group.tradeType)
                    if (badgeLabel.isNotBlank()) {
                        HomeExchangeBadge(text = badgeLabel)
                    }
                    Text(
                        text = group.title.orEmpty(),
                        style = typography.medium16,
                        color = colors.grey900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
                val authorGenre = buildString {
                    group.author?.let { append(it) }
                    group.genre?.let { append("($it)") }
                }
                if (authorGenre.isNotBlank()) {
                    Text(
                        text = authorGenre,
                        style = typography.regular14,
                        color = colors.grey500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // 하단: 예상 독서 기간 + 호스트 정보
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // 피그마: "예상 독서 기간"(grey700) + "7"(grey800) + "일"(grey700) — 3개 분리
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "예상 독서 기간",
                        style = typography.regular14,
                        color = colors.grey700,
                    )
                    Row {
                        Text(
                            text = group.readingPeriod.toString(),
                            style = typography.regular14,
                            color = colors.grey800,
                        )
                        Text(
                            text = "일",
                            style = typography.regular14,
                            color = colors.grey700,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfilePlaceholder(
                        modifier = Modifier.size(20.dp),
                        imageUrl = group.hostProfileImageUrl,
                    )
                    Text(
                        text = group.hostNickname.orEmpty(),
                        style = typography.regular15,
                        color = colors.grey700,
                    )
                    Text(
                        text = "·",
                        style = typography.regular15,
                        color = colors.grey700,
                    )
                    Text(
                        text = group.groupName.orEmpty(),
                        style = typography.regular15,
                        color = colors.grey500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

    }
}

@Composable
private fun HomeExchangeBadge(
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

private fun tradeTypeLabel(tradeType: String?): String = when (tradeType) {
    "DIRECT" -> "직접"
    "DELIVERY" -> "택배"
    else -> tradeType.orEmpty()
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

private val mockGroupRecruiting = GroupItem(
    groupId = 1L,
    groupName = "책과 함께 그룹",
    title = "살인자의 기억법",
    author = "김영하",
    genre = "한국소설",
    bookImage = null,
    hostNickname = "부키유저",
    hostProfileImageUrl = null,
    groupStatus = "RECRUITING",
    currentCount = 2,
    maxCapacity = 5,
    waitingCount = 1,
    isHot = false,
    tradeType = "직접",
    readingPeriod = 7,
    pictureBadge = null,
)

@Preview(showBackground = true, name = "HomeGroupCard")
@Composable
private fun HomeGroupCardPreview() {
    BookiiPreview {
        HomeGroupCard(
            group = mockGroupRecruiting,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "HomeExchangeBadge - 직접")
@Composable
private fun HomeExchangeBadgeDirectPreview() {
    BookiiPreview {
        Box(Modifier.padding(8.dp)) {
            HomeExchangeBadge(text = "직접")
        }
    }
}
