package com.bookiibookii.bookiibookii.group.ui.joinrequest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.ui.component.CardButton
import com.bookiibookii.bookiibookii.ui.component.CardButtonStyle
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 주소 미등록 상태에서 그룹 참여 시도 시 안내 다이얼로그
@Composable
fun GroupAddressRequiredDialog(
    onDismiss: () -> Unit,
    onManageAddress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round24)
            .background(BookiiBookiiTheme.colors.white)
            .padding(20.dp),
    ) {
        Text(
            text = "그룹에 참여하려면 먼저 배송지 또는 희망 교환 장소를 등록해주세요.",
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey900,
        )
        Row(
            modifier = Modifier.padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CardButton(
                text = "닫기",
                style = CardButtonStyle.Grey,
                height = 48.dp,
                textStyle = BookiiBookiiTheme.typography.regular15,
                contentColorOverride = BookiiBookiiTheme.colors.grey900,
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            CardButton(
                text = "주소지 관리",
                style = CardButtonStyle.Main,
                height = 48.dp,
                textStyle = BookiiBookiiTheme.typography.regular15,
                onClick = onManageAddress,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun GroupAddressRequiredDialogPreview() {
    BookiiPreview {
        Column(
            modifier = Modifier
                .background(BookiiBookiiTheme.colors.uiBg)
                .padding(24.dp),
        ) {
            GroupAddressRequiredDialog(
                onDismiss = {},
                onManageAddress = {},
            )
        }
    }
}
