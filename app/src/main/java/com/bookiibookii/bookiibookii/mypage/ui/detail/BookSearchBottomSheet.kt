package com.bookiibookii.bookiibookii.mypage.ui.detail

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview

private data class MockSearchBook(val title: String, val authorAndGenre: String)

private val mockSearchResults = listOf(
    MockSearchBook("사요가 바로 살인자", "장우영 (소설)"),
    MockSearchBook("헤일리가 살인자?", "김하늘 (과학)"),
    MockSearchBook("사실 무스가 살인자", "무스 (IT)"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSearchBottomSheet(
    onDismiss: () -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BookiiBookiiTheme.colors.white,
        dragHandle = null,
    ) {
        BookSearchSheetContent(onDismiss = onDismiss)
    }
}

@Composable
private fun BookSearchSheetContent(
    onDismiss: () -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(20.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "나의 인생 책",
                style = BookiiBookiiTheme.typography.semibold20,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BookiiBookiiTheme.colors.grey100)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = "닫기",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BookiiBookiiTheme.colors.white)
                .border(1.dp, BookiiBookiiTheme.colors.grey300, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = "검색",
                tint = BookiiBookiiTheme.colors.grey700,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                textStyle = BookiiBookiiTheme.typography.medium15.copy(color = BookiiBookiiTheme.colors.grey800),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "책 제목을 검색하세요",
                            style = BookiiBookiiTheme.typography.medium15,
                            color = BookiiBookiiTheme.colors.grey400,
                        )
                    }
                    innerTextField()
                },
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Results list
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(mockSearchResults) { index, book ->
                if (index > 0) {
                    HorizontalDivider(color = BookiiBookiiTheme.colors.grey100, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                SearchResultItem(book = book)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SearchResultItem(book: MockSearchBook) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(95.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BookiiBookiiTheme.colors.grey200),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = book.title,
                style = BookiiBookiiTheme.typography.medium18,
                color = BookiiBookiiTheme.colors.grey800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = book.authorAndGenre,
                style = BookiiBookiiTheme.typography.regular16,
                color = BookiiBookiiTheme.colors.grey600,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun BookSearchSheetContentPreview() {
    BookiiPreview {
        BookSearchSheetContent()
    }
}
