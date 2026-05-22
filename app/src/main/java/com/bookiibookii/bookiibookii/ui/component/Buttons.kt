package com.bookiibookii.bookiibookii.ui.component

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

enum class BottomSheetBtnStyle { White, Dark, Orange, Grey, Red }
@Composable
fun BottomSheetTwoBtnShort(
    text: String,
    style: BottomSheetBtnStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = BookiiBookiiTheme.typography.medium16,
) {
    val shape = BookiiBookiiTheme.shape.round16
    val containerColor: Color
    val contentColor: Color
    val borderColor: Color?
    when (style) {
        BottomSheetBtnStyle.White -> {
            containerColor = BookiiBookiiTheme.colors.white
            contentColor = BookiiBookiiTheme.colors.grey900
            borderColor = BookiiBookiiTheme.colors.grey200
        }
        BottomSheetBtnStyle.Dark -> {
            containerColor = BookiiBookiiTheme.colors.grey900
            contentColor = BookiiBookiiTheme.colors.white
            borderColor = null
        }
        BottomSheetBtnStyle.Orange -> {
            containerColor = BookiiBookiiTheme.colors.uiMain
            contentColor = BookiiBookiiTheme.colors.white
            borderColor = null
        }
        BottomSheetBtnStyle.Grey -> {
            containerColor = BookiiBookiiTheme.colors.grey200
            contentColor = BookiiBookiiTheme.colors.grey500
            borderColor = null
        }
        BottomSheetBtnStyle.Red -> {
            containerColor = BookiiBookiiTheme.colors.uiPointRed
            contentColor = BookiiBookiiTheme.colors.white
            borderColor = null
        }
    }
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .background(containerColor)
            .then(
                if (borderColor != null) {
                    Modifier.border(width = 1.dp, color = borderColor, shape = shape)
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = textStyle,
            color = contentColor,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun BottomSheetTwoBtnShortPreview() {
    BookiiPreview {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BottomSheetTwoBtnShort(
                text = "취소",
                style = BottomSheetBtnStyle.White,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            BottomSheetTwoBtnShort(
                text = "적용",
                style = BottomSheetBtnStyle.Dark,
                onClick = {},
                modifier = Modifier.weight(1f),
                textStyle = BookiiBookiiTheme.typography.regular16
            )
        }
    }
}

@Composable
fun FooterButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = BookiiBookiiTheme.shape.round20
    val containerColor = if (enabled) {
        BookiiBookiiTheme.colors.grey900
    } else {
        BookiiBookiiTheme.colors.grey200
    }
    val contentColor = if (enabled) {
        BookiiBookiiTheme.colors.white
    } else {
        BookiiBookiiTheme.colors.grey500
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(shape)
            .background(containerColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.medium18,
            color = contentColor,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun FooterButtonPreview() {
    BookiiPreview {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FooterButton(
                text = "그룹 만들기",
                onClick = {},
            )
            FooterButton(
                text = "그룹 만들기",
                onClick = {},
                enabled = false,
            )
        }
    }
}

enum class CardButtonStyle { Main, MainPale, Grey, White }

@Composable
fun CardButton(
    text: String,
    style: CardButtonStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    shape: Shape = BookiiBookiiTheme.shape.round16,
    textStyle: TextStyle = BookiiBookiiTheme.typography.regular16,
) {
    val containerColor: Color
    val contentColor: Color
    val borderColor: Color?
    when (style) {
        CardButtonStyle.Main -> {
            containerColor = BookiiBookiiTheme.colors.uiMain
            contentColor = BookiiBookiiTheme.colors.white
            borderColor = null
        }
        CardButtonStyle.MainPale -> {
            containerColor = BookiiBookiiTheme.colors.uiMainPale
            contentColor = BookiiBookiiTheme.colors.uiMain
            borderColor = null
        }
        CardButtonStyle.Grey -> {
            containerColor = BookiiBookiiTheme.colors.grey200
            contentColor = BookiiBookiiTheme.colors.grey500
            borderColor = null
        }
        CardButtonStyle.White -> {
            containerColor = BookiiBookiiTheme.colors.white
            contentColor = BookiiBookiiTheme.colors.grey900
            borderColor = BookiiBookiiTheme.colors.grey200
        }
    }
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(containerColor)
            .then(
                if (borderColor != null) {
                    Modifier.border(width = 1.dp, color = borderColor, shape = shape)
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = textStyle,
            color = contentColor,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun CardButtonPreview() {
    BookiiPreview {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CardButton(
                text = "Main",
                style = CardButtonStyle.Main,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            CardButton(
                text = "MainPale",
                style = CardButtonStyle.MainPale,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun CloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "닫기",
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(BookiiBookiiTheme.colors.grey100)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_x),
            contentDescription = contentDescription,
            tint = BookiiBookiiTheme.colors.grey900,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview
@Composable
private fun CloseButtonPreview() {
    BookiiPreview {
        CloseButton(onClick = {})
    }
}
