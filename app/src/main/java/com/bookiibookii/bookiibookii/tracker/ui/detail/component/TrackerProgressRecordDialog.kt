package com.bookiibookii.bookiibookii.tracker.ui.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

// 진행률 기록 다이얼로그 (택배/직접교환 공통)
@Composable
fun TrackerProgressRecordDialog(
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TrackerProgressRecordDialogContent(onDismiss = onDismiss)
    }
}

@Composable
private fun TrackerProgressRecordDialogContent(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pageInput by remember { mutableStateOf("") }
    var isAllRead by remember { mutableStateOf(false) }

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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "진행률 기록",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
            )
            CloseButton(onClick = onDismiss)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "몇 페이지까지 읽었나요?",
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                Text(
                    text = "*",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.uiMain,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        color = BookiiBookiiTheme.colors.grey100,
                        shape = BookiiBookiiTheme.shape.round20,
                    )
                    .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (pageInput.isEmpty()) "숫자만 입력해주세요" else pageInput,
                    style = BookiiBookiiTheme.typography.medium16,
                    color = if (pageInput.isEmpty()) {
                        BookiiBookiiTheme.colors.grey500
                    } else {
                        BookiiBookiiTheme.colors.grey900
                    },
                )
                if (isAllRead) {
                    Text(
                        text = "다 읽었어요!",
                        style = BookiiBookiiTheme.typography.regular16,
                        color = BookiiBookiiTheme.colors.uiMain,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CardButton(
                text = "다 읽었어요",
                style = if (isAllRead) CardButtonStyle.Grey else CardButtonStyle.White,
                onClick = { isAllRead = !isAllRead },
                modifier = Modifier.weight(1f),
            )
            CardButton(
                text = "완료",
                style = CardButtonStyle.Main,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(widthDp = 412, showBackground = true)
@Composable
private fun TrackerProgressRecordDialogPreview() {
    BookiiPreview {
        TrackerProgressRecordDialogContent(onDismiss = {})
    }
}
