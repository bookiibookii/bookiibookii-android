package com.bookiibookii.bookiibookii.tracker.ui.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bookiibookii.bookiibookii.ui.component.CardButton
import com.bookiibookii.bookiibookii.ui.component.CardButtonStyle
import com.bookiibookii.bookiibookii.ui.component.CloseButton
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
fun TrackerProgressRecordDialog(
    totalPages: Int,
    onDismiss: () -> Unit,
    onConfirm: (currentPage: Int) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TrackerProgressRecordDialogContent(
            totalPages = totalPages,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
        )
    }
}

@Composable
private fun TrackerProgressRecordDialogContent(
    totalPages: Int,
    onDismiss: () -> Unit,
    onConfirm: (currentPage: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pageInput by remember { mutableStateOf("") }
    val pageInt = pageInput.toIntOrNull() ?: 0
    val isAllRead = totalPages > 0 && pageInt >= totalPages

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
                Box(modifier = Modifier.weight(1f)) {
                    if (pageInput.isEmpty()) {
                        Text(
                            text = "숫자만 입력해주세요",
                            style = BookiiBookiiTheme.typography.medium16,
                            color = BookiiBookiiTheme.colors.grey500,
                        )
                    }
                    BasicTextField(
                        value = pageInput,
                        onValueChange = { raw ->
                            val digits = raw.filter { it.isDigit() }
                            val asInt = digits.toIntOrNull()
                            pageInput = when {
                                digits.isEmpty() -> ""
                                totalPages > 0 && asInt != null && asInt > totalPages -> totalPages.toString()
                                else -> digits
                            }
                        },
                        textStyle = BookiiBookiiTheme.typography.medium16.copy(
                            color = BookiiBookiiTheme.colors.grey900,
                        ),
                        cursorBrush = SolidColor(BookiiBookiiTheme.colors.uiMain),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
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
                onClick = {
                    pageInput = if (isAllRead) "" else totalPages.toString()
                },
                modifier = Modifier.weight(1f),
            )
            CardButton(
                text = "완료",
                style = CardButtonStyle.Main,
                onClick = {
                    onConfirm(pageInt)
                    onDismiss()
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(widthDp = 412, showBackground = true)
@Composable
private fun TrackerProgressRecordDialogPreview() {
    BookiiPreview {
        TrackerProgressRecordDialogContent(
            totalPages = 400,
            onDismiss = {},
            onConfirm = {},
        )
    }
}
