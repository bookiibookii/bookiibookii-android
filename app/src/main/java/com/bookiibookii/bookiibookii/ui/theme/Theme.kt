package com.bookiibookii.bookiibookii.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun BookiiBookiiTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalBookiiColors provides bookiiColors,
        LocalBookiiTypography provides bookiiTypography,
        LocalBookiiShape provides bookiiShape,
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
