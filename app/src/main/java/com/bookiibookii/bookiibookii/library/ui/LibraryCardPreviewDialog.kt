package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
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
    imageUri: android.net.Uri? = null,
    bookTitle: String = "",
) {
    Dialog(onDismissRequest = onDismiss) {
        // 기본 다이얼로그 스크림보다 배경을 더 어둡게
        val dialogWindowProvider = LocalView.current.parent as? DialogWindowProvider
        SideEffect {
            dialogWindowProvider?.window?.setDimAmount(0.8f)
        }
        LibraryCardPreviewContent(mode = mode, quote = quote, memo = memo, imageUri = imageUri, bookTitle = bookTitle)
    }
}

// 카드 본문 — Dialog 래퍼와 분리해 @Preview 대상이 되도록 함
@Composable
private fun LibraryCardPreviewContent(
    mode: AddCardMode,
    quote: String,
    memo: String,
    imageUri: android.net.Uri? = null,
    bookTitle: String = "",
) {
    Box(
        modifier = Modifier
            .width(320.dp)
            .height(520.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white),
    ) {
        when (mode) {
            AddCardMode.TEXT -> QuoteCardPreview(quote = quote, memo = memo, bookTitle = bookTitle)
            AddCardMode.PHOTO -> PhotoCardPreview(memo = memo, imageUri = imageUri, bookTitle = bookTitle)
        }
    }
}

// 좌상단 책제목 칩 — [B 심볼] + 책제목. 미리보기·공유 카드 공용
// solidBackground=true(이미지 카드): 주황 채움 / false(텍스트 카드): 투명 + main_pale 테두리
@Composable
internal fun BookTitleChip(title: String, solidBackground: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (solidBackground) {
                    Modifier.background(BookiiBookiiTheme.colors.uiMain)
                } else {
                    Modifier.border(1.dp, BookiiBookiiTheme.colors.uiMainPale, RoundedCornerShape(8.dp))
                }
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_logo_symbol),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = title,
            style = BookiiBookiiTheme.typography.medium16,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// 인용구 카드 v2: 선명한 오렌지 그라데이션 + 하단 메모 영역
@Composable
private fun QuoteCardPreview(quote: String, memo: String, bookTitle: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(TOP_WEIGHT / (TOP_WEIGHT + BOTTOM_WEIGHT))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFF4E18), Color(0xFFFF7618), Color(0xFFFFC9A4)),
                        start  = Offset(0f, 0f),
                        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    )
                ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                if (bookTitle.isNotBlank()) {
                    BookTitleChip(title = bookTitle, solidBackground = false)
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_quote),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                    Text(
                        text = "“${quote.ifBlank { "인용구를 입력해주세요." }}”",
                        style = BookiiBookiiTheme.typography.semibold20,
                        color = Color.White,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(BOTTOM_WEIGHT / (TOP_WEIGHT + BOTTOM_WEIGHT))
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            // 메모가 없으면 아무것도 표시하지 않음
            if (memo.isNotBlank()) {
                Text(
                    text = memo,
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey800,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// 사진 카드 v2: 사진 상단 배경 + 하단 메모 영역
@Composable
private fun PhotoCardPreview(memo: String, imageUri: android.net.Uri? = null, bookTitle: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 사진 영역 (업로드된 사진 또는 placeholder)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(TOP_WEIGHT / (TOP_WEIGHT + BOTTOM_WEIGHT))
                .background(BookiiBookiiTheme.colors.grey300),
        ) {
            if (imageUri != null) {
                coil.compose.AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (bookTitle.isNotBlank()) {
                Box(modifier = Modifier.align(Alignment.TopStart).padding(20.dp)) {
                    BookTitleChip(title = bookTitle, solidBackground = true)
                }
            }
        }
        // 메모 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(BOTTOM_WEIGHT / (TOP_WEIGHT + BOTTOM_WEIGHT))
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            if (memo.isNotBlank()) {
                Text(
                    text = memo,
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey800,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LibraryCardPreviewContentTextPreview() {
    BookiiPreview {
        LibraryCardPreviewContent(
            mode = AddCardMode.TEXT,
            quote = "내 안에서 솟아 나오려는 것, 바로 그것을 나는 살아 보려고 했다.",
            memo = "헤르만 헤세의 데미안 중에서",
            bookTitle = "데미안",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LibraryCardPreviewContentPhotoPreview() {
    BookiiPreview {
        LibraryCardPreviewContent(
            mode = AddCardMode.PHOTO,
            quote = "",
            memo = "오늘 읽은 페이지의 한 장면",
            bookTitle = "데미안",
        )
    }
}
