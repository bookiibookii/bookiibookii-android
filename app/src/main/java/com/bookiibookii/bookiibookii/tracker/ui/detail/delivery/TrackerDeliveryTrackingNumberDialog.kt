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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.tracker.model.DeliveryCompany
import com.bookiibookii.bookiibookii.ui.component.CloseButton
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
fun TrackerDeliveryTrackingNumberDialog(
    onDismiss: () -> Unit,
    onConfirm: (deliveryCompany: String, trackingNumber: String) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TrackerDeliveryTrackingNumberDialogContent(
            onDismiss = onDismiss,
            onConfirm = onConfirm,
        )
    }
}

@Composable
private fun TrackerDeliveryTrackingNumberDialogContent(
    onDismiss: () -> Unit,
    onConfirm: (deliveryCompany: String, trackingNumber: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var selectedCompany by remember { mutableStateOf<DeliveryCompany?>(null) }
    var trackingInput by remember { mutableStateOf("") }
    val canSubmit = selectedCompany != null && trackingInput.isNotBlank()

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
                text = "운송장 등록",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
            )
            CloseButton(onClick = onDismiss)
        }

        DeliveryCompanyField(
            selected = selectedCompany,
            onSelect = { selectedCompany = it },
        )

        TrackingNumberField(
            value = trackingInput,
            onValueChange = { raw -> trackingInput = raw.filter { it.isDigit() } },
        )

        SubmitButton(
            enabled = canSubmit,
            onClick = {
                val company = selectedCompany ?: return@SubmitButton
                if (trackingInput.length < 10) {
                    context.showCustomToast(
                        message = "운송장 번호를 10자 이상 입력해주세요",
                        isSuccess = false,
                    )
                    return@SubmitButton
                }
                onConfirm(company.apiValue, trackingInput)
                onDismiss()
            },
        )
    }
}

@Composable
private fun DeliveryCompanyField(
    selected: DeliveryCompany?,
    onSelect: (DeliveryCompany) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var triggerWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val triggerWidthDp = with(density) { triggerWidthPx.toDp() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RequiredLabel(text = "택배사")
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .onSizeChanged { triggerWidthPx = it.width }
                    .clip(BookiiBookiiTheme.shape.round20)
                    .background(BookiiBookiiTheme.colors.grey100)
                    .border(
                        width = 1.dp,
                        color = BookiiBookiiTheme.colors.grey200,
                        shape = BookiiBookiiTheme.shape.round20,
                    )
                    .clickable { expanded = true }
                    .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = selected?.label ?: "택배사를 선택해주세요",
                    style = BookiiBookiiTheme.typography.medium16,
                    color = if (selected == null) {
                        BookiiBookiiTheme.colors.grey500
                    } else {
                        BookiiBookiiTheme.colors.grey900
                    },
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron),
                    contentDescription = null,
                    tint = BookiiBookiiTheme.colors.grey500,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(-90f),
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(triggerWidthDp),
                shape = BookiiBookiiTheme.shape.round20,
                containerColor = BookiiBookiiTheme.colors.white,
                shadowElevation = 4.dp,
                tonalElevation = 0.dp,
            ) {
                DeliveryCompany.entries.forEach { company ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = company.label,
                                style = BookiiBookiiTheme.typography.medium16,
                                color = BookiiBookiiTheme.colors.grey900,
                            )
                        },
                        onClick = {
                            onSelect(company)
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        colors = MenuDefaults.itemColors(
                            textColor = BookiiBookiiTheme.colors.grey900,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackingNumberField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RequiredLabel(text = "운송장 번호")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    color = BookiiBookiiTheme.colors.grey100,
                    shape = BookiiBookiiTheme.shape.round20,
                )
                .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text(
                    text = "숫자만 입력해주세요",
                    style = BookiiBookiiTheme.typography.medium16,
                    color = BookiiBookiiTheme.colors.grey500,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = BookiiBookiiTheme.typography.medium16.copy(
                    color = BookiiBookiiTheme.colors.grey900,
                ),
                cursorBrush = SolidColor(BookiiBookiiTheme.colors.uiMain),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SubmitButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(BookiiBookiiTheme.shape.round20)
            .background(
                if (enabled) BookiiBookiiTheme.colors.uiMain
                else BookiiBookiiTheme.colors.grey200,
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "완료",
            style = BookiiBookiiTheme.typography.regular16,
            color = if (enabled) {
                BookiiBookiiTheme.colors.white
            } else {
                BookiiBookiiTheme.colors.grey500
            },
        )
    }
}

@Composable
private fun RequiredLabel(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey900,
        )
        Text(
            text = "*",
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.uiMain,
        )
    }
}

@Preview(widthDp = 412, showBackground = true)
@Composable
private fun TrackerDeliveryTrackingNumberDialogPreview() {
    BookiiPreview {
        TrackerDeliveryTrackingNumberDialogContent(
            onDismiss = {},
            onConfirm = { _, _ -> },
        )
    }
}
