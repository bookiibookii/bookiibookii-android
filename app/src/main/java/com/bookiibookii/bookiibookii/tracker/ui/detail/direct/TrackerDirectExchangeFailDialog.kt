package com.bookiibookii.bookiibookii.tracker.ui.detail.direct

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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

// 직접교환 실패 안내 다이얼로그
@Composable
fun TrackerDirectExchangeFailDialog(
    onDismiss: () -> Unit,
    onReportClick: () -> Unit,
    onGoToCommentsClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TrackerDirectExchangeFailDialogContent(
            onDismiss = onDismiss,
            onReportClick = onReportClick,
            onGoToCommentsClick = onGoToCommentsClick,
        )
    }
}

@Composable
private fun TrackerDirectExchangeFailDialogContent(
    onDismiss: () -> Unit,
    onReportClick: () -> Unit,
    onGoToCommentsClick: () -> Unit,
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
                text = "책을 교환하지 못했나요?",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
            )
            CloseButton(onClick = onDismiss)
        }

        Text(
            text = "상대방과 연락하여 약속을 다시 잡거나, 일방적인 노쇼라면 신고를 진행해 주세요.",
            style = BookiiBookiiTheme.typography.medium16,
            color = BookiiBookiiTheme.colors.grey700,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CardButton(
                text = "신고하기",
                style = CardButtonStyle.White,
                onClick = onReportClick,
                modifier = Modifier.weight(1f),
            )
            CardButton(
                text = "메시지",
                style = CardButtonStyle.Main,
                onClick = onGoToCommentsClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(widthDp = 412, showBackground = true)
@Composable
private fun TrackerDirectExchangeFailDialogPreview() {
    BookiiPreview {
        TrackerDirectExchangeFailDialogContent(
            onDismiss = {},
            onReportClick = {},
            onGoToCommentsClick = {},
        )
    }
}
