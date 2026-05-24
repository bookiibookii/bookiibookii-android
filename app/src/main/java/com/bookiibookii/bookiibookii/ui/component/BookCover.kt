package com.bookiibookii.bookiibookii.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 책 표지 영역. 사이즈는 호출처에서 modifier로 지정
@Composable
fun BookCover(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
) {
    val shape = BookiiBookiiTheme.shape.round8
    Box(
        modifier = modifier
            .clip(shape)
            .background(BookiiBookiiTheme.colors.uiBg)
            .border(
                width = 1.dp,
                color = BookiiBookiiTheme.colors.grey100,
                shape = shape,
            ),
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
