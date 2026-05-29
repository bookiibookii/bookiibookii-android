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
import androidx.compose.foundation.layout.heightIn
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
// 표시용 주소 데이터
data class TrackerDeliveryAddressDisplay(
    val receiverName: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val addressDetail: String = "",
)

@Composable
fun TrackerDeliveryInfoDialog(
    partnerNickname: String,
    myAddress: TrackerDeliveryAddressDisplay,
    partnerAddress: TrackerDeliveryAddressDisplay,
    canEditMyAddress: Boolean,
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
            myAddress = myAddress,
            partnerAddress = partnerAddress,
            canEditMyAddress = canEditMyAddress,
            onDismiss = onDismiss,
            onEditClick = onEditClick,
            onConfirmClick = onConfirmClick,
        )
    }
}

@Composable
private fun TrackerDeliveryInfoDialogContent(
    partnerNickname: String,
    myAddress: TrackerDeliveryAddressDisplay,
    partnerAddress: TrackerDeliveryAddressDisplay,
    canEditMyAddress: Boolean,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val current = if (selectedTabIndex == 0) myAddress else partnerAddress
    // "나" 탭이면서 canEditMyAddress=true 일 때만 수정 가능
    val editEnabled = selectedTabIndex == 0 && canEditMyAddress

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

        InfoField(label = "수령인", value = current.receiverName)
        InfoField(label = "연락처", value = current.phoneNumber)
        InfoField(label = "주소", value = current.address)
        InfoField(label = "상세주소", value = current.addressDetail)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CardButton(
                text = "수정",
                style = if (editEnabled) CardButtonStyle.White else CardButtonStyle.Grey,
                onClick = { if (editEnabled) onEditClick() },
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
                .heightIn(min = 48.dp)
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
            myAddress = TrackerDeliveryAddressDisplay(
                receiverName = "장우영",
                phoneNumber = "010-1111-1111",
                address = "서울 용산구 한강로2가 426",
                addressDetail = "101동 202호",
            ),
            partnerAddress = TrackerDeliveryAddressDisplay(
                receiverName = "noshel",
                phoneNumber = "010-2222-2222",
                address = "서울 강남구 테헤란로 123",
                addressDetail = "10층",
            ),
            canEditMyAddress = true,
            onDismiss = {},
            onEditClick = {},
            onConfirmClick = {},
        )
    }
}
