package com.bookiibookii.bookiibookii.mypage.ui.detail

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// BottomSheet for a book selected from the bookshelf list.
// isRepresentative = false → "대표 도서로 등록" (Main orange)
// isRepresentative = true  → "대표 도서 등록 해제" (PointRed)
@Composable
fun BookshelfBookBottomSheet(
    title: String = "괴테는 모든 것을 말했다",
    author: String = "스즈키 유이",
    genre: String = "소설",
    isRepresentative: Boolean = false,
    onDismiss: () -> Unit = {},
    onReviewClick: () -> Unit = {},
    onToggleRepresentativeClick: () -> Unit = {},
    onLibraryClick: () -> Unit = {},
    onAladinClick: () -> Unit = {},
) {
    val sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    Box(modifier = Modifier.fillMaxSize()) {
        // Dimming background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x73000000))
                .clickable { onDismiss() },
        )

        // Bottom sheet card
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = sheetShape,
                    ambientColor = Color(0x1A000000),
                    spotColor = Color(0x1A000000),
                )
                .background(BookiiBookiiTheme.colors.white, sheetShape)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Part 1: drag handle + book header
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Drag handle
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(4.dp)
                            .background(BookiiBookiiTheme.colors.grey200, RoundedCornerShape(300.dp)),
                    )
                }

                // Book info with bottom divider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = title,
                            style = BookiiBookiiTheme.typography.semibold20,
                            color = BookiiBookiiTheme.colors.grey900,
                        )
                        Text(
                            text = "$author ($genre)",
                            style = BookiiBookiiTheme.typography.regular16,
                            color = BookiiBookiiTheme.colors.grey700,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BookiiBookiiTheme.colors.grey200)
                }
            }

            // Part 2: menu items
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BottomSheetMenuItem(
                    label = "교환독서 리뷰 확인",
                    color = BookiiBookiiTheme.colors.grey800,
                    onClick = onReviewClick,
                )
                BottomSheetMenuItem(
                    label = if (isRepresentative) "대표 도서 등록 해제" else "대표 도서 등록",
                    color = if (isRepresentative) BookiiBookiiTheme.colors.uiPointRed else BookiiBookiiTheme.colors.uiMain,
                    onClick = onToggleRepresentativeClick,
                )
                BottomSheetMenuItem(
                    label = "서재로 이동",
                    color = BookiiBookiiTheme.colors.grey800,
                    onClick = onLibraryClick,
                )
                BottomSheetMenuItem(
                    label = "알라딘으로 이동",
                    color = BookiiBookiiTheme.colors.grey800,
                    onClick = onAladinClick,
                )
            }
        }
    }
}

@Composable
private fun BottomSheetMenuItem(
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = BookiiBookiiTheme.typography.regular18,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
    )
}

@Preview(showBackground = true)
@Composable
private fun BookshelfBookBottomSheetNotRepresentativePreview() {
    BookshelfBookBottomSheet(isRepresentative = false)
}
