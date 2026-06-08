package com.bookiibookii.bookiibookii.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookiiBookiiTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalBookiiColors provides bookiiColors,
        LocalBookiiTypography provides bookiiTypography,
        LocalBookiiShape provides bookiiShape,
        // null = 리플(눌렀을 때 dim) 전역 비활성화
        LocalRippleConfiguration provides null,
    ) {
        MaterialTheme(content = content)
    }
}

object BookiiBookiiTheme {
    val colors: BookiiColors
        @Composable get() = LocalBookiiColors.current
    val typography: BookiiTypography
        @Composable get() = LocalBookiiTypography.current
    val shape: BookiiShape
        @Composable get() = LocalBookiiShape.current
}
