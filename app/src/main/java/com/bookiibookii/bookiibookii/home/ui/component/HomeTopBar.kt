package com.bookiibookii.bookiibookii.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
internal fun HomeTopBar(
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    hasNewNotification: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography
    androidx.compose.foundation.layout.Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .background(colors.white)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // 왼쪽: 프로필 아이콘 — 피그마: w=88dp (오른쪽과 동일 너비로 중앙 정렬 보장)
            Row(
                modifier = Modifier.width(88.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_person_fill),
                        contentDescription = "프로필",
                        tint = androidx.compose.ui.graphics.Color.Unspecified,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            // 가운데: 화면 제목
            Text(
                text = "탐색",
                style = typography.medium20,
                color = colors.grey900,
            )

            // 오른쪽: 알림 아이콘 — 피그마: w=88dp (빈 슬롯 40dp + gap 8dp + 알림 40dp)
            Row(
                modifier = Modifier.width(88.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_alert_32),
                            contentDescription = "알림",
                            tint = androidx.compose.ui.graphics.Color.Unspecified,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    if (hasNewNotification) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .offset(x = (-4).dp, y = 4.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(colors.uiMain),
                        )
                    }
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.grey200)
    }
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "HomeTopBar")
@Composable
private fun HomeTopBarPreview() {
    BookiiPreview {
        HomeTopBar(
            onNotificationClick = {},
            onProfileClick = {},
        )
    }
}
