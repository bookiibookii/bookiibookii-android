package com.bookiibookii.bookiibookii.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 선택 가능한 주소/장소 카드. ic_map + 제목 + 주소, 선택 시 주황 강조
@Composable
fun AddressButton(
    title: String,
    address: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
        modifier = modifier
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

@Preview
@Composable
private fun AddressButtonPreview() {
    BookiiPreview {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AddressButton(
                title = "자취방",
                address = "서울 용산구 한강로2가 426",
                selected = true,
                onClick = {},
            )
            AddressButton(
                title = "본가",
                address = "서울 서초구 신반포로 270 116동 2903호",
                selected = false,
                onClick = {},
            )
        }
    }
}
