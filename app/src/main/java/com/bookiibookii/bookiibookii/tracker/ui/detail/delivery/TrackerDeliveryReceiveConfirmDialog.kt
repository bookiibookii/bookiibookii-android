package com.bookiibookii.bookiibookii.tracker.ui.detail.delivery

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.CardButton
import com.bookiibookii.bookiibookii.ui.component.CardButtonStyle
import com.bookiibookii.bookiibookii.ui.component.CloseButton
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 책 수령 확인 다이얼로그
@Composable
fun TrackerDeliveryReceiveConfirmDialog(
    onDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TrackerDeliveryReceiveConfirmDialogContent(
            onDismiss = onDismiss,
            onConfirmClick = onConfirmClick,
        )
    }
}

@Composable
private fun TrackerDeliveryReceiveConfirmDialogContent(
    onDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isChecked by remember { mutableStateOf(false) }

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
                text = "책을 받았나요?",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
            )
            CloseButton(onClick = onDismiss)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            CheckBox(
                checked = isChecked,
                onToggle = { isChecked = !isChecked },
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "책의 상태를 확인했습니다",
                    style = BookiiBookiiTheme.typography.regular15,
                    color = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.clickable(onClick = { isChecked = !isChecked }),
                )
                Text(
                    text = "파손, 훼손, 낙서 등이 있다면 즉시 상대방에게\n댓글로 알려주세요.",
                    style = BookiiBookiiTheme.typography.regular15,
                    color = BookiiBookiiTheme.colors.grey600,
                )
            }
        }

        CardButton(
            text = "받았어요",
            style = if (isChecked) CardButtonStyle.Main else CardButtonStyle.Grey,
            onClick = if (isChecked) onConfirmClick else {{}},
            modifier = Modifier.fillMaxWidth(),
            shape = BookiiBookiiTheme.shape.round16,
            textStyle = BookiiBookiiTheme.typography.regular15,
        )
    }
}

@Composable
private fun CheckBox(
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(
                color = if (checked) {
                    BookiiBookiiTheme.colors.uiMainPale
                } else {
                    BookiiBookiiTheme.colors.grey200
                },
                shape = BookiiBookiiTheme.shape.round5,
            )
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            tint = if (checked) {
                BookiiBookiiTheme.colors.uiMain
            } else {
                BookiiBookiiTheme.colors.white
            },
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(widthDp = 412, showBackground = true)
@Composable
private fun TrackerDeliveryReceiveConfirmDialogPreview() {
    BookiiPreview {
        TrackerDeliveryReceiveConfirmDialogContent(
            onDismiss = {},
            onConfirmClick = {},
        )
    }
}
