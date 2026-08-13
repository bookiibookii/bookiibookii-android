package com.bookiibookii.bookiibookii.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
fun CommonDialog(
    title: String,
    content: String,
    confirmBtnText: String,
    confirmBtnColor: Color,
    onConfirmClick: () -> Unit,
    subtitle: String = "",
    onDismiss: () -> Unit = {},
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(BookiiBookiiTheme.colors.white, RoundedCornerShape(16.dp))
                .padding(24.dp),
        ) {
            // 타이틀 + 닫기 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = title,
                    style = BookiiBookiiTheme.typography.bold20,
                    color = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.weight(1f),
                )
                Image(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onDismiss),
                )
            }

            // 서브타이틀 (비어있으면 숨김)
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey800,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // 본문
            Spacer(Modifier.height(20.dp))
            Text(
                text = content,
                style = BookiiBookiiTheme.typography.regular16,
                color = BookiiBookiiTheme.colors.grey700,
            )

            // 취소 / 확인 버튼
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(BookiiBookiiTheme.colors.grey200, RoundedCornerShape(16.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "취소",
                        style = BookiiBookiiTheme.typography.semibold16,
                        color = BookiiBookiiTheme.colors.grey900,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(confirmBtnColor, RoundedCornerShape(16.dp))
                        .clickable(onClick = { onDismiss(); onConfirmClick() }),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = confirmBtnText,
                        style = BookiiBookiiTheme.typography.semibold16,
                        color = BookiiBookiiTheme.colors.white,
                    )
                }
            }
        }
    }
}

@Preview(name = "공통 다이얼로그 - 서브타이틀 있음", showBackground = true)
@Composable
private fun CommonDialogWithSubtitlePreview() {
    BookiiPreview {
        CommonDialog(
            title = "그룹 탈퇴",
            subtitle = "2025 여름 독서 모임",
            content = "그룹에서 탈퇴하시겠어요?",
            confirmBtnText = "탈퇴",
            confirmBtnColor = BookiiBookiiTheme.colors.uiPointRed,
            onConfirmClick = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "공통 다이얼로그 - 서브타이틀 없음", showBackground = true)
@Composable
private fun CommonDialogPreview() {
    BookiiPreview {
        CommonDialog(
            title = "로그아웃",
            content = "정말 로그아웃 하시겠어요?",
            confirmBtnText = "로그아웃",
            confirmBtnColor = BookiiBookiiTheme.colors.grey900,
            onConfirmClick = {},
            onDismiss = {},
        )
    }
}
