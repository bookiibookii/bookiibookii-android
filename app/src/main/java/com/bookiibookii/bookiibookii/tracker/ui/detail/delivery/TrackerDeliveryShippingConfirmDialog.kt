package com.bookiibookii.bookiibookii.tracker.ui.detail.delivery

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
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 운송장 정보 확인 다이얼로그
@Composable
fun TrackerDeliveryShippingConfirmDialog(
    onDismiss: () -> Unit,
    onTrackingSearchClick: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TrackerDeliveryShippingConfirmDialogContent(
            onDismiss = onDismiss,
            onTrackingSearchClick = onTrackingSearchClick,
            onConfirmClick = onConfirmClick,
        )
    }
}

@Composable
private fun TrackerDeliveryShippingConfirmDialogContent(
    onDismiss: () -> Unit,
    onTrackingSearchClick: () -> Unit,
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
            Text(
                text = "운송장 정보 확인",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
            )
            CloseButton(onClick = onDismiss)
        }

        ReadOnlyField(label = "택배사", value = "CJ대한통운")
        ReadOnlyField(label = "운송장 번호", value = "790335274231")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CardButton(
                text = "배송 조회",
                style = CardButtonStyle.White,
                onClick = onTrackingSearchClick,
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
private fun ReadOnlyField(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = BookiiBookiiTheme.typography.regular16,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Text(
                text = "*",
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.uiMain,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    color = BookiiBookiiTheme.colors.white,
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
private fun TrackerDeliveryShippingConfirmDialogPreview() {
    BookiiPreview {
        TrackerDeliveryShippingConfirmDialogContent(
            onDismiss = {},
            onTrackingSearchClick = {},
            onConfirmClick = {},
        )
    }
}
