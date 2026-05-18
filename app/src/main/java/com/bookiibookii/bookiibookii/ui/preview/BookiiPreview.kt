package com.bookiibookii.bookiibookii.ui.preview

import androidx.compose.runtime.Composable
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// preview helper 기본 설정
@Composable
fun BookiiPreview(content: @Composable () -> Unit) {
    BookiiBookiiTheme {
        content()
    }
}
