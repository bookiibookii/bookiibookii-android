package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 독서카드 상세의 v2 레이아웃과 동일한 비율
private const val TOP_WEIGHT = 336f
private const val BOTTOM_WEIGHT = 128f

@Composable
internal fun LibraryCardPreviewDialog(
    mode: AddCardMode,
    quote: String,
    memo: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(320.dp)
                .height(520.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(BookiiBookiiTheme.colors.white),
        ) {
            when (mode) {
                AddCardMode.TEXT -> QuoteCardPreview(quote = quote, memo = memo)
                AddCardMode.PHOTO -> PhotoCardPreview(memo = memo)
            }
        }
    }
}

// 인용구 카드 v2: 선명한 오렌지 그라데이션 + 하단 메모 영역
@Composable
private fun QuoteCardPreview(quote: String, memo: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(TOP_WEIGHT / (TOP_WEIGHT + BOTTOM_WEIGHT))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFF4E18), Color(0xFFFF7618), Color(0xFFFFC9A4)),
                        start = Offset(0f, Float.POSITIVE_INFINITY),
                        end = Offset(Float.POSITIVE_INFINITY, 0f),
                    )
                ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 40.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_quote),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = "\"${quote.ifBlank { "인용구를 입력해주세요." }}\"",
                    style = BookiiBookiiTheme.typography.semibold20,
                    color = Color.White,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(BOTTOM_WEIGHT / (TOP_WEIGHT + BOTTOM_WEIGHT))
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = memo.ifBlank { "메모를 입력해주세요." },
                style = BookiiBookiiTheme.typography.regular16,
                color = BookiiBookiiTheme.colors.grey800,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// 사진 카드 v2: 사진 상단 배경 + 하단 메모 영역
@Composable
private fun PhotoCardPreview(memo: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 사진 영역 (업로드된 사진 또는 placeholder)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(TOP_WEIGHT / (TOP_WEIGHT + BOTTOM_WEIGHT))
                .background(BookiiBookiiTheme.colors.grey300),
        )
        // 메모 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(BOTTOM_WEIGHT / (TOP_WEIGHT + BOTTOM_WEIGHT))
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = memo.ifBlank { "메모를 입력해주세요." },
                style = BookiiBookiiTheme.typography.regular16,
                color = BookiiBookiiTheme.colors.grey800,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
