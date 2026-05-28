package com.bookiibookii.bookiibookii.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = BookiiBookiiTheme.shape.round26
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(
                if (selected) BookiiBookiiTheme.colors.grey900 else BookiiBookiiTheme.colors.white,
            )
            .then(
                if (selected) {
                    Modifier
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = BookiiBookiiTheme.colors.grey200,
                        shape = shape,
                    )
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.regular14,
            color = if (selected) BookiiBookiiTheme.colors.white else BookiiBookiiTheme.colors.grey900,
            maxLines = 1,
        )
    }
}

@Composable
fun BottomSheetChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = BookiiBookiiTheme.shape.round50
    Box(
        modifier = modifier
            .height(46.dp)
            .widthIn(min = 60.dp)
            .clip(shape)
            .background(
                if (selected) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.white,
            )
            .then(
                if (selected) {
                    Modifier
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = BookiiBookiiTheme.colors.grey200,
                        shape = shape,
                    )
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.medium16,
            color = if (selected) BookiiBookiiTheme.colors.white else BookiiBookiiTheme.colors.grey900,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun FilterChipPreview() {
    BookiiPreview {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(text = "교환 방식", selected = false, onClick = {})
            FilterChip(text = "분야별", selected = true, onClick = {})
        }
    }
}

@Preview
@Composable
private fun BottomSheetChipPreview() {
    BookiiPreview {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BottomSheetChip(text = "전체", selected = true, onClick = {}, modifier = Modifier.width(120.dp))
            BottomSheetChip(text = "직접 교환", selected = false, onClick = {})
        }
    }
}

@Composable
fun SmallChip(
    text: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
) {
    val shape = BookiiBookiiTheme.shape.round8
    Box(
        modifier = modifier
            .height(28.dp)
            .clip(shape)
            .background(bgColor)
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = BookiiBookiiTheme.typography.regular12, color = textColor, maxLines = 1)
    }
}

@Composable
fun ExchangeTypeChip(isDelivery: Boolean, modifier: Modifier = Modifier) {
    if (isDelivery) {
        SmallChip(
            text = "택배 교환",
            bgColor = BookiiBookiiTheme.colors.uiMainPale,
            textColor = BookiiBookiiTheme.colors.uiMain,
            modifier = modifier,
        )
    } else {
        SmallChip(
            text = "직접 교환",
            bgColor = BookiiBookiiTheme.colors.uiMainSubPale,
            textColor = BookiiBookiiTheme.colors.uiMainSub,
            modifier = modifier,
        )
    }
}

@Composable
fun ReviewTypeChip(isGood: Boolean, modifier: Modifier = Modifier) {
    val shape = BookiiBookiiTheme.shape.round8
    val bgColor = if (isGood) BookiiBookiiTheme.colors.uiMainPale else BookiiBookiiTheme.colors.white
    val borderColor = if (isGood) BookiiBookiiTheme.colors.uiMain150 else BookiiBookiiTheme.colors.grey200
    val textColor = if (isGood) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey500
    val iconRes = if (isGood) R.drawable.ic_hand_thumbs_up else R.drawable.ic_hand_thumbs_down
    val text = if (isGood) "좋았어요" else "별로였어요"

    Row(
        modifier = modifier
            .height(28.dp)
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.regular12,
            color = textColor,
            maxLines = 1,
        )
    }
}
