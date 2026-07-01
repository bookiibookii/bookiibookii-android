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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

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
    bookAuthor: String = "",
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
            bookAuthor = bookAuthor,
        )
    }
}

@Composable
private fun LibraryCardPreviewContent(
    mode: AddCardMode,
    quote: String,
    memo: String,
    imageUri: android.net.Uri? = null,
    bookTitle: String = "",
    bookAuthor: String = "",
) {
    Box(
        modifier = Modifier
            .width(320.dp)
            .height(520.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white),
    ) {
        when (mode) {
            AddCardMode.TEXT -> QuoteCardPreview(
                quote = quote,
                memo = memo,
                bookTitle = bookTitle,
                bookAuthor = bookAuthor,
            )
            AddCardMode.PHOTO -> PhotoCardPreview(
                memo = memo,
                imageUri = imageUri,
                bookTitle = bookTitle,
                bookAuthor = bookAuthor,
            )
        }
    }
}

internal enum class BookTitleChipStyle { SOLID, PALE_FILL, WHITE_STROKE, MAIN_PALE_STROKE }

@Composable
internal fun BookTitleChip(title: String, style: BookTitleChipStyle = BookTitleChipStyle.SOLID) {
    val isPaleStyle = style == BookTitleChipStyle.PALE_FILL || style == BookTitleChipStyle.MAIN_PALE_STROKE
    val bgModifier: Modifier = when (style) {
        BookTitleChipStyle.SOLID           -> Modifier.background(BookiiBookiiTheme.colors.uiMain)
        BookTitleChipStyle.PALE_FILL       -> Modifier.background(BookiiBookiiTheme.colors.uiMainPale)
        BookTitleChipStyle.WHITE_STROKE    -> Modifier.border(1.dp, Color.White, RoundedCornerShape(8.dp))
        BookTitleChipStyle.MAIN_PALE_STROKE -> Modifier.border(1.dp, BookiiBookiiTheme.colors.uiMainPale, RoundedCornerShape(8.dp))
    }
    val contentColor = if (isPaleStyle) BookiiBookiiTheme.colors.uiMain else Color.White

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .then(bgModifier)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isPaleStyle) {
            Icon(
                painter = painterResource(R.drawable.ic_logo_symbol),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = title,
            style = BookiiBookiiTheme.typography.medium16,
            color = BookiiBookiiTheme.colors.white,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun QuoteCardPreview(
    quote: String,
    memo: String,
    bookTitle: String,
    bookAuthor: String = "",
) {
    val displayQuote = if (quote.isBlank()) "인용구를 입력해주세요." else quote
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
                    BookTitleChip(title = bookTitle, style = BookTitleChipStyle.MAIN_PALE_STROKE)
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_quote),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                    Text(
                        text = "“$displayQuote”",
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
            if (memo.isNotBlank()) {
                Text(
                    text = memo,
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey800,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.TopStart),
                )
            }
            if (bookAuthor.isNotBlank()) {
                Text(
                    text = "by. $bookAuthor",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey400,
                    modifier = Modifier.align(Alignment.BottomEnd),
                )
            }
        }
    }
}

@Composable
private fun PhotoCardPreview(
    memo: String,
    imageUri: android.net.Uri? = null,
    bookTitle: String = "",
    bookAuthor: String = "",
) {
    Column(modifier = Modifier.fillMaxSize()) {
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
                    BookTitleChip(title = bookTitle)
                }
            }
        }
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
                    modifier = Modifier.align(Alignment.TopStart),
                )
            }
            if (bookAuthor.isNotBlank()) {
                Text(
                    text = "by. $bookAuthor",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey400,
                    modifier = Modifier.align(Alignment.BottomEnd),
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
