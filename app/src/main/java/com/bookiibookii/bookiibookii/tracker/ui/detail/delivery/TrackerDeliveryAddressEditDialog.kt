package com.bookiibookii.bookiibookii.tracker.ui.detail.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

private data class SavedAddress(val title: String, val address: String)

private val DUMMY_ADDRESSES = listOf(
    SavedAddress(title = "자취방", address = "서울 용산구 한강로2가 426"),
    SavedAddress(title = "회사", address = "서울 강남구 테헤란로 123"),
)

// 택배 배송 정보 수정 다이얼로그 (택배교환 전용)
@Composable
fun TrackerDeliveryAddressEditDialog(
    onDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TrackerDeliveryAddressEditDialogContent(
            onDismiss = onDismiss,
            onConfirmClick = onConfirmClick,
        )
    }
}

@Composable
private fun TrackerDeliveryAddressEditDialogContent(
    onDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedIndex by remember { mutableIntStateOf(0) }

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
                text = "배송 정보 수정",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
            )
            CloseButton(onClick = onDismiss)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "나의 배송지",
                style = BookiiBookiiTheme.typography.regular16,
                color = BookiiBookiiTheme.colors.grey900,
            )
            DUMMY_ADDRESSES.forEachIndexed { index, address ->
                AddressButton(
                    title = address.title,
                    address = address.address,
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "직접 입력",
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                AddressInputBox(placeholder = "건물명, 도로명, 지번으로 검색")
            }
            AddressInputBox(placeholder = "상세 주소")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CardButton(
                text = "이전",
                style = CardButtonStyle.White,
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                height = 48.dp,
            )
            CardButton(
                text = "확인",
                style = CardButtonStyle.Main,
                onClick = onConfirmClick,
                modifier = Modifier.weight(1f),
                height = 48.dp,
            )
        }
    }
}

@Composable
private fun AddressButton(
    title: String,
    address: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (selected) {
        BookiiBookiiTheme.colors.uiMainPale
    } else {
        BookiiBookiiTheme.colors.white
    }
    val borderColor = if (selected) {
        BookiiBookiiTheme.colors.uiMain150
    } else {
        BookiiBookiiTheme.colors.grey200
    }
    val titleColor = if (selected) {
        BookiiBookiiTheme.colors.uiMain
    } else {
        BookiiBookiiTheme.colors.grey700
    }
    val addressColor = if (selected) {
        BookiiBookiiTheme.colors.grey600
    } else {
        BookiiBookiiTheme.colors.grey500
    }
    val iconTint = if (selected) {
        BookiiBookiiTheme.colors.uiMain
    } else {
        BookiiBookiiTheme.colors.grey500
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(color = backgroundColor, shape = BookiiBookiiTheme.shape.round20)
            .border(width = 1.dp, color = borderColor, shape = BookiiBookiiTheme.shape.round20)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_map),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp),
        )
        Column {
            Text(
                text = title,
                style = BookiiBookiiTheme.typography.medium16,
                color = titleColor,
            )
            Text(
                text = address,
                style = BookiiBookiiTheme.typography.regular14,
                color = addressColor,
            )
        }
    }
}

@Composable
private fun AddressInputBox(placeholder: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round16,
            )
            .border(
                width = 1.dp,
                color = BookiiBookiiTheme.colors.grey300,
                shape = BookiiBookiiTheme.shape.round16,
            )
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = placeholder,
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey500,
        )
    }
}

@Preview(widthDp = 412, showBackground = true)
@Composable
private fun TrackerDeliveryAddressEditDialogPreview() {
    BookiiPreview {
        TrackerDeliveryAddressEditDialogContent(
            onDismiss = {},
            onConfirmClick = {},
        )
    }
}
