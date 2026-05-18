package com.bookiibookii.bookiibookii.ui.component

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 임시로 만들어둔 거임 수정 필요
@Composable
fun BookiiPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = BookiiBookiiTheme.shape.round20,
        colors = ButtonDefaults.buttonColors(
            containerColor = BookiiBookiiTheme.colors.uiMain,
            contentColor = BookiiBookiiTheme.colors.white,
        ),
        modifier = modifier.height(52.dp),
    ) {
        Text(text, style = BookiiBookiiTheme.typography.medium16)
    }
}