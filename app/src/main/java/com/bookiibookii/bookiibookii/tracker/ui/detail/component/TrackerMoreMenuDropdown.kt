package com.bookiibookii.bookiibookii.tracker.ui.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

// 트래커 상세 더보기 드롭다운 (택배/직접교환 공통)
// 게스트일 때는 "독서 기간 수정" 항목이 표시되지 않음
@Composable
fun TrackerMoreMenuDropdown(
    isHost: Boolean,
    onEditPeriodClick: () -> Unit,
    onGoToLibraryClick: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(IntrinsicSize.Max)
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
        if (isHost) {
            MenuItem(
                text = "독서 기간 수정",
                iconRes = R.drawable.ic_edit,
                onClick = onEditPeriodClick,
            )
        }
        MenuItem(
            text = "서재로 이동",
            iconRes = R.drawable.ic_book,
            onClick = onGoToLibraryClick,
        )
        MenuItem(
            text = "신고",
            iconRes = R.drawable.ic_report_32,
            onClick = onReportClick,
        )
    }
}

@Composable
private fun MenuItem(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.medium14,
            color = BookiiBookiiTheme.colors.grey700,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = BookiiBookiiTheme.colors.grey700,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Preview(widthDp = 200, showBackground = true)
@Composable
private fun TrackerMoreMenuDropdownHostPreview() {
    BookiiPreview {
        TrackerMoreMenuDropdown(
            isHost = true,
            onEditPeriodClick = {},
            onGoToLibraryClick = {},
            onReportClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(widthDp = 200, showBackground = true)
@Composable
private fun TrackerMoreMenuDropdownGuestPreview() {
    BookiiPreview {
        TrackerMoreMenuDropdown(
            isHost = false,
            onEditPeriodClick = {},
            onGoToLibraryClick = {},
            onReportClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
