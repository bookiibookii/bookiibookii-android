package com.bookiibookii.bookiibookii.mypage.ui.setting

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bookiibookii.bookiibookii.R

@Composable
fun BookiiDialog(
    title: String,
    body: String,
    confirmText: String,
    confirmColor: Color = BookiiBookiiTheme.colors.grey900,
    onConfirm: () -> Unit,
    cancelText: String? = null,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BookiiBookiiTheme.colors.white),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = BookiiBookiiTheme.typography.semibold20,
                        color = BookiiBookiiTheme.colors.grey900,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BookiiBookiiTheme.colors.grey100),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_x),
                            contentDescription = "닫기",
                            tint = BookiiBookiiTheme.colors.grey900,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = body,
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey700,
                )
                Spacer(modifier = Modifier.height(20.dp))
                if (cancelText != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .border(1.dp, BookiiBookiiTheme.colors.grey200, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BookiiBookiiTheme.colors.white,
                                contentColor = BookiiBookiiTheme.colors.grey900,
                            ),
                        ) {
                            Text(cancelText, style = BookiiBookiiTheme.typography.regular15)
                        }
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = confirmColor,
                                contentColor = BookiiBookiiTheme.colors.white,
                            ),
                        ) {
                            Text(confirmText, style = BookiiBookiiTheme.typography.regular15)
                        }
                    }
                } else {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = confirmColor,
                            contentColor = BookiiBookiiTheme.colors.white,
                        ),
                    ) {
                        Text(confirmText, style = BookiiBookiiTheme.typography.regular15)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BookiiDialogTwoButtonPreview() {
    BookiiPreview {
        BookiiDialog(
            title = "로그아웃",
            body = "로그아웃 하시겠어요?",
            confirmText = "로그아웃",
            confirmColor = BookiiBookiiTheme.colors.uiPointRed,
            onConfirm = {},
            cancelText = "취소",
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BookiiDialogOneButtonPreview() {
    BookiiPreview {
        BookiiDialog(
            title = "탈퇴 불가",
            body = "진행 중인 그룹이 모두 종료되어야\n탈퇴 가능합니다.",
            confirmText = "닫기",
            confirmColor = BookiiBookiiTheme.colors.grey900,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
