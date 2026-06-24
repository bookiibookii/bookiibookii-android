package com.bookiibookii.bookiibookii.tracker.ui.detail.direct

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bookiibookii.bookiibookii.ui.component.CardButton
import com.bookiibookii.bookiibookii.ui.component.CardButtonStyle
import com.bookiibookii.bookiibookii.ui.component.CloseButton
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 직접교환 약속 잡기 3/3 - 약속 확인
@Composable
fun TrackerDirectMeetingConfirmDialog(
    scheduledAt: String,   // raw ISO date-time, 예: 2026-05-20T14:30:00
    address: String,
    addressDetail: String,
    onDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TrackerDirectMeetingConfirmDialogContent(
            scheduledAt = scheduledAt,
            address = address,
            addressDetail = addressDetail,
            onDismiss = onDismiss,
            onConfirmClick = onConfirmClick,
        )
    }
}

@Composable
private fun TrackerDirectMeetingConfirmDialogContent(
    scheduledAt: String,
    address: String,
    addressDetail: String,
    onDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round24,
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StepChip(text = "3/3")
                Text(
                    text = "약속을 확인해주세요!",
                    style = BookiiBookiiTheme.typography.bold24,
                    color = BookiiBookiiTheme.colors.grey900,
                )
            }
            CloseButton(onClick = onDismiss)
        }

        ReadOnlyField(label = "일시", value = formatScheduledAt(scheduledAt))
        ReadOnlyField(
            label = "장소",
            value = if (addressDetail.isBlank()) address else "$address $addressDetail",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CardButton(
                text = "이전",
                style = CardButtonStyle.Grey,
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            CardButton(
                text = "등록",
                style = CardButtonStyle.Main,
                onClick = onConfirmClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// offset 포함(+09:00)/UTC Z 모두 받아 KST "2026. 05. 20. 14:30" 로 표시 (파싱 실패 시 원본)
private fun formatScheduledAt(scheduledAt: String): String =
    DateUtils.formatKstDateTime(scheduledAt)

@Composable
private fun StepChip(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round8,
            )
            .border(
                width = 1.dp,
                color = BookiiBookiiTheme.colors.grey200,
                shape = BookiiBookiiTheme.shape.round8,
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.medium14,
            color = BookiiBookiiTheme.colors.grey900,
        )
    }
}

@Composable
private fun ReadOnlyField(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey900,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    color = BookiiBookiiTheme.colors.grey100,
                    shape = BookiiBookiiTheme.shape.round20,
                )
                .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = value,
                style = BookiiBookiiTheme.typography.medium16,
                color = BookiiBookiiTheme.colors.grey900,
                maxLines = 1,
            )
        }
    }
}

@Preview(widthDp = 412, showBackground = true)
@Composable
private fun TrackerDirectMeetingConfirmDialogPreview() {
    BookiiPreview {
        TrackerDirectMeetingConfirmDialogContent(
            scheduledAt = "2026-05-20T14:30:00",
            address = "서울특별시 강남구 강남대로 396",
            addressDetail = "2층 창가 자리",
            onDismiss = {},
            onConfirmClick = {},
        )
    }
}
