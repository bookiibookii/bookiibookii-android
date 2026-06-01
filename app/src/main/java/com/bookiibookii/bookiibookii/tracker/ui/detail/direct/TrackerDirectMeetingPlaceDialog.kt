package com.bookiibookii.bookiibookii.tracker.ui.detail.direct

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
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

// 직접교환 약속 잡기 2/3 - 장소 선택
@Composable
fun TrackerDirectMeetingPlaceDialog(
    address: String,
    addressDetail: String,
    onAddressDetailChange: (String) -> Unit,
    onLoadMyPlaceClick: () -> Unit,
    onDismiss: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TrackerDirectMeetingPlaceDialogContent(
            address = address,
            addressDetail = addressDetail,
            onAddressDetailChange = onAddressDetailChange,
            onLoadMyPlaceClick = onLoadMyPlaceClick,
            onDismiss = onDismiss,
            onPreviousClick = onPreviousClick,
            onNextClick = onNextClick,
        )
    }
}

@Composable
private fun TrackerDirectMeetingPlaceDialogContent(
    address: String,
    addressDetail: String,
    onAddressDetailChange: (String) -> Unit,
    onLoadMyPlaceClick: () -> Unit,
    onDismiss: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StepChip(text = "2/3")
                Text(
                    text = "어디서 만날까요?",
                    style = BookiiBookiiTheme.typography.bold24,
                    color = BookiiBookiiTheme.colors.grey900,
                )
            }
            CloseButton(onClick = onDismiss)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SearchInput(placeholder = "지번, 도로명, 건물명으로 검색")

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PlaceField(value = address, placeholder = "교환 장소를 선택해주세요")
                DetailAddressField(
                    value = addressDetail,
                    onValueChange = onAddressDetailChange,
                    placeholder = "상세주소를 입력해주세요",
                )
            }

            LoadMyPlacesButton(
                text = "나의 희망교환장소 불러오기",
                onClick = onLoadMyPlaceClick,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CardButton(
                text = "이전",
                style = CardButtonStyle.Grey,
                onClick = onPreviousClick,
                modifier = Modifier.weight(1f),
            )
            CardButton(
                text = "다음",
                style = CardButtonStyle.Main,
                onClick = onNextClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StepChip(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round8,
            )
            .border(
                width = 1.dp,
                color = BookiiBookiiTheme.colors.grey200,
                shape = BookiiBookiiTheme.shape.round8,
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.medium14,
            color = BookiiBookiiTheme.colors.grey900,
        )
    }
}

@Composable
private fun SearchInput(placeholder: String) {
    Row(
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
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            tint = BookiiBookiiTheme.colors.grey500,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = placeholder,
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey300,
        )
    }
}

@Composable
private fun PlaceField(value: String, placeholder: String) {
    val isEmpty = value.isBlank()
    Row(
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = value.ifBlank { placeholder },
            style = BookiiBookiiTheme.typography.medium16,
            color = if (isEmpty) {
                BookiiBookiiTheme.colors.grey500
            } else {
                BookiiBookiiTheme.colors.grey900
            },
            maxLines = 1,
        )
        Box(modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun DetailAddressField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
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
                text = placeholder,
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
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun LoadMyPlacesButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = BookiiBookiiTheme.colors.uiMainPale,
                shape = BookiiBookiiTheme.shape.round16,
            )
            .border(
                width = 1.dp,
                color = BookiiBookiiTheme.colors.uiMain150,
                shape = BookiiBookiiTheme.shape.round16,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.regular15,
            color = BookiiBookiiTheme.colors.uiMain,
        )
    }
}

@Preview(widthDp = 412, showBackground = true)
@Composable
private fun TrackerDirectMeetingPlaceDialogPreview() {
    BookiiPreview {
        TrackerDirectMeetingPlaceDialogContent(
            address = "서울특별시 강남구 강남대로 396",
            addressDetail = "",
            onAddressDetailChange = {},
            onLoadMyPlaceClick = {},
            onDismiss = {},
            onPreviousClick = {},
            onNextClick = {},
        )
    }
}
