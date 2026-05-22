package com.bookiibookii.bookiibookii.tracker.ui.detail.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
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

// 택배 배송 정보 확인 다이얼로그
@Composable
fun TrackerDeliveryInfoDialog(
    partnerNickname: String,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TrackerDeliveryInfoDialogContent(
            partnerNickname = partnerNickname,
            onDismiss = onDismiss,
            onEditClick = onEditClick,
            onConfirmClick = onConfirmClick,
        )
    }
}

@Composable
private fun TrackerDeliveryInfoDialogContent(
    partnerNickname: String,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

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
                text = "배송 정보 확인",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
            )
            CloseButton(onClick = onDismiss)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TabItem(
                text = "나",
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
            )
            TabItem(
                text = partnerNickname,
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
            )
        }

        InfoField(label = "수령인", value = "장우영")
        InfoField(label = "연락처", value = "010-1111-1111")
        InfoField(label = "주소", value = "서울 용산구 한강로2가 426 101동 202호")

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
    }
}

@Composable
private fun TabItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 4.dp),
            style = BookiiBookiiTheme.typography.medium18,
            color = if (selected) {
                BookiiBookiiTheme.colors.uiMain
            } else {
                BookiiBookiiTheme.colors.grey400
            },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    if (selected) BookiiBookiiTheme.colors.uiMain else Color.Transparent,
                ),
        )
    }
}

@Composable
private fun InfoField(label: String, value: String) {
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
                .border(
                    width = 1.dp,
                    color = BookiiBookiiTheme.colors.grey200,
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
private fun TrackerDeliveryInfoDialogPreview() {
    BookiiPreview {
        TrackerDeliveryInfoDialogContent(
            partnerNickname = "noshel",
            onDismiss = {},
            onEditClick = {},
            onConfirmClick = {},
        )
    }
}
