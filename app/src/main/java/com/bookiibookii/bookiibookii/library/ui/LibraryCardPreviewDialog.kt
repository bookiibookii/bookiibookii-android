package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun LibraryCardPreviewDialog(
    mode: AddCardMode,
    quote: String,
    memo: String,
    onDismiss: () -> Unit,
    imageUri: android.net.Uri? = null,
    bookTitle: String = "",
    nickname: String = "",
) {
    Dialog(onDismissRequest = onDismiss) {
        val dialogWindowProvider = LocalView.current.parent as? DialogWindowProvider
        SideEffect {
            dialogWindowProvider?.window?.setDimAmount(0.8f)
        }
        LibraryCardPreviewContent(
            mode = mode,
            quote = quote,
            memo = memo,
            imageUri = imageUri,
            bookTitle = bookTitle,
            nickname = nickname,
        )
    }
}

/**
 * 카드 작성 미리보기 (Figma LIB-04-02). 공유 카드와 같은 레이아웃을 쓴다.
 * 글 모드 = 주황 인용구 카드, 사진 모드 = 사진 + 하단 글 카드.
 */
@Composable
private fun LibraryCardPreviewContent(
    mode: AddCardMode,
    quote: String,
    memo: String,
    imageUri: android.net.Uri? = null,
    bookTitle: String = "",
    nickname: String = "",
) {
    Box(
        modifier = Modifier
            .width(320.dp)
            .height(520.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white),
    ) {
        when (mode) {
            AddCardMode.TEXT -> QuoteCardLayout(
                bookTitle = bookTitle,
                quotation = quote.ifBlank { "인용구를 입력해주세요." },
                content = memo,
                username = nickname,
                isLight = false,
            )
            AddCardMode.PHOTO -> PhotoSplitCardLayout(
                bookTitle = bookTitle,
                content = memo,
                username = nickname,
                image = {
                    if (imageUri != null) {
                        coil.compose.AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.matchParentSize(),
                        )
                    }
                },
            )
        }
    }
}

internal enum class BookTitleChipStyle { SOLID, SOLID_PALE_STROKE, PALE_FILL, WHITE_STROKE, MAIN_PALE_STROKE }

@Composable
internal fun BookTitleChip(title: String, style: BookTitleChipStyle = BookTitleChipStyle.SOLID) {
    // PALE_FILL(사진 오버레이 카드)만 연한 배경 + 주황 글자 + 아이콘 없음, 나머지는 흰 글자 + 로고 아이콘
    val isPaleStyle = style == BookTitleChipStyle.PALE_FILL
    val chipShape = RoundedCornerShape(8.dp)
    val bgModifier: Modifier = when (style) {
        BookTitleChipStyle.SOLID           -> Modifier.background(BookiiBookiiTheme.colors.uiMain)
        BookTitleChipStyle.SOLID_PALE_STROKE -> Modifier
            .background(BookiiBookiiTheme.colors.uiMain)
            .border(1.dp, BookiiBookiiTheme.colors.uiMainPale, chipShape)
        BookTitleChipStyle.PALE_FILL       -> Modifier.background(BookiiBookiiTheme.colors.uiMainPale)
        BookTitleChipStyle.WHITE_STROKE    -> Modifier.border(1.dp, Color.White, chipShape)
        BookTitleChipStyle.MAIN_PALE_STROKE -> Modifier.border(1.dp, BookiiBookiiTheme.colors.uiMainPale, chipShape)
    }
    val contentColor = if (isPaleStyle) BookiiBookiiTheme.colors.uiMain else Color.White

    Row(
        modifier = Modifier
            .clip(chipShape)
            .then(bgModifier)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isPaleStyle) {
            Icon(
                painter = painterResource(R.drawable.ic_logo_symbol_card),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(width = 18.587.dp, height = 16.dp),
            )
        }
        Text(
            text = title,
            style = BookiiBookiiTheme.typography.medium16,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LibraryCardPreviewContentTextPreview() {
    BookiiPreview {
        LibraryCardPreviewContent(
            mode = AddCardMode.TEXT,
            quote = "새는 알에서 나오려고 싸운다. 알은 세상이다. 태어나려는 자는 한 세계를 파괴해야 한다.",
            memo = "책을 쓰지 않고 한 우물만 팠다면, 나는 그들이 원하는 자리에 앉아 행복했을까. 나는 오히려 우물을 나와서 많이 느낀다.",
            bookTitle = "나는 당신을 편애합니다",
            nickname = "foryxxng",
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
            memo = "책을 쓰지 않고 한 우물만 팠다면, 나는 그들이 원하는 자리에 앉아 행복했을까.",
            bookTitle = "나는 당신을 편애합니다",
            nickname = "foryxxng",
        )
    }
}
