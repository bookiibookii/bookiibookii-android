package com.bookiibookii.bookiibookii.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
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
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun HomeTopBar(
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
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
            // 왼쪽: 프로필 아이콘
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

            // 가운데: 화면 제목
            Text(
                text = "탐색",
                style = typography.medium20,
                color = colors.grey900,
            )

            // 오른쪽: 빈 공간(좌측과 균형) + 알림 아이콘
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(40.dp)) // 균형용 빈 슬롯
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
