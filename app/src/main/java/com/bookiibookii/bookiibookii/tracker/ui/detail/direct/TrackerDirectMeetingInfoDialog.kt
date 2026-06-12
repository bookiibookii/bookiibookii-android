package com.bookiibookii.bookiibookii.tracker.ui.detail.direct

import androidx.compose.foundation.background
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
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// 직접교환 약속 확인 - 등록된 약속 조회 (StepChip 없음)
@Composable
fun TrackerDirectMeetingInfoDialog(
    scheduledAt: String,
    address: String,
    addressDetail: String,
    isHost: Boolean,
    onDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TrackerDirectMeetingInfoDialogContent(
            scheduledAt = scheduledAt,
            address = address,
            addressDetail = addressDetail,
            isHost = isHost,
            onDismiss = onDismiss,
            onConfirmClick = onConfirmClick,
            onEditClick = onEditClick,
        )
    }
}

@Composable
private fun TrackerDirectMeetingInfoDialogContent(
    scheduledAt: String,
    address: String,
    addressDetail: String,
    isHost: Boolean,
    onDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
    onEditClick: () -> Unit,
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
            Text(
                text = "약속을 확인해주세요!",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
            )
            CloseButton(onClick = onDismiss)
        }

        ReadOnlyField(label = "일시", value = formatMeetingScheduledAt(scheduledAt))
        ReadOnlyField(
            label = "장소",
            value = if (addressDetail.isBlank()) address else "$address $addressDetail",
        )

        // 호스트 + 약속까지 12시간 이상 남음 → [수정][확인]. 게스트이거나 12시간 미만이면 [확인] 단일 버튼
        if (isHost && isMeetingEditable(scheduledAt)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CardButton(
                    text = "수정",
                    style = CardButtonStyle.White,
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                )
                CardButton(
                    text = "확인",
                    style = CardButtonStyle.Main,
                    onClick = onConfirmClick,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            CardButton(
                text = "확인",
                style = CardButtonStyle.Main,
                onClick = onConfirmClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// 현재 시각 기준 약속까지 12시간 이상 남았는지(수정 가능 여부). 파싱 실패 시 수정 불가로 간주.
private fun isMeetingEditable(scheduledAt: String): Boolean =
    runCatching {
        Duration.between(LocalDateTime.now(), LocalDateTime.parse(scheduledAt)).toHours() >= 12
    }.getOrDefault(false)

private val MEETING_INFO_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm")

// "2026-05-20T14:30:00" → "2026. 05. 20. 14:30" (파싱 실패 시 원본 그대로)
private fun formatMeetingScheduledAt(scheduledAt: String): String =
    runCatching {
        LocalDateTime.parse(scheduledAt).format(MEETING_INFO_FORMATTER)
    }.getOrDefault(scheduledAt)

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
private fun TrackerDirectMeetingInfoDialogPreview() {
    BookiiPreview {
        TrackerDirectMeetingInfoDialogContent(
            scheduledAt = "2026-05-20T14:30:00",
            address = "서울특별시 강남구 강남대로 396",
            addressDetail = "2층 창가 자리",
            isHost = true,
            onDismiss = {},
            onConfirmClick = {},
            onEditClick = {},
        )
    }
}
