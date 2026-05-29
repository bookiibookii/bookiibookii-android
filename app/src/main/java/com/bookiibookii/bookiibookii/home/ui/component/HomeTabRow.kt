package com.bookiibookii.bookiibookii.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.home.HomeTab
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private val tabLabels = listOf(
    HomeTab.RECOMMEND to "추천",
    HomeTab.MY_GROUPS to "내 그룹",
    HomeTab.APPLIED to "신청한 그룹",
)

@Composable
internal fun HomeTabRow(
    selectedTab: HomeTab,
    onTabSelect: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography

    // 피그마: 외부 컨테이너 border-bottom grey/100, overflow-clip, px=16dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.white),
    ) {
        // grey/100 구분선 — 뒤에 그려지므로 orange underline이 위로 올라옴
        HorizontalDivider(
            thickness = 1.dp,
            color = colors.grey100,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        // 피그마 내부 Row: gap=12dp, pt=12dp, left-aligned
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            tabLabels.forEach { (tab, label) ->
                val isSelected = selectedTab == tab
                // orange color captured for drawBehind lambda (non-composable context)
                val indicatorColor: Color = if (isSelected) colors.uiMain else Color.Transparent

                // drawBehind → padding 순서:
                //   drawBehind 캔버스 = Text + padding 전체 크기
                //   → size.width = 텍스트 너비 + px*2 (피그마 border-b와 동일)
                //   → line at size.height - 1dp = 바닥 끝 (pb=16dp 포함)
                Text(
                    text = label,
                    style = typography.medium18,
                    color = if (isSelected) colors.uiMain else colors.grey400,
                    modifier = Modifier
                        .clickable { onTabSelect(tab) }
                        .drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            drawLine(
                                color = indicatorColor,
                                start = Offset(0f, size.height - strokeWidth / 2f),
                                end = Offset(size.width, size.height - strokeWidth / 2f),
                                strokeWidth = strokeWidth,
                            )
                        }
                        .padding(start = 4.dp, end = 4.dp, bottom = 16.dp),
                )
            }
        }
    }
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "HomeTabRow - 추천 선택")
@Composable
private fun HomeTabRowRecommendPreview() {
    BookiiPreview {
        HomeTabRow(selectedTab = HomeTab.RECOMMEND, onTabSelect = {})
    }
}

@Preview(showBackground = true, name = "HomeTabRow - 내 그룹 선택")
@Composable
private fun HomeTabRowMyGroupsPreview() {
    BookiiPreview {
        HomeTabRow(selectedTab = HomeTab.MY_GROUPS, onTabSelect = {})
    }
}

@Preview(showBackground = true, name = "HomeTabRow - 신청한 그룹 선택")
@Composable
private fun HomeTabRowAppliedPreview() {
    BookiiPreview {
        HomeTabRow(selectedTab = HomeTab.APPLIED, onTabSelect = {})
    }
}
