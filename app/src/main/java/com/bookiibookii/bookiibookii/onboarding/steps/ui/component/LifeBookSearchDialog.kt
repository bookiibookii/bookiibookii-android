package com.bookiibookii.bookiibookii.onboarding.steps.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.group.BookItem
import com.bookiibookii.bookiibookii.onboarding.steps.model.BookSearchState
import com.bookiibookii.bookiibookii.common.stripBookSubtitle
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun LifeBookSearchDialog(
    bookSearchState: BookSearchState?,
    onQueryChange: (String) -> Unit,
    onBookSelected: (BookItem) -> Unit,
    onDismiss: () -> Unit,
    onSearch: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        LifeBookSearchDialogContent(
            bookSearchState = bookSearchState,
            onQueryChange = onQueryChange,
            onBookSelected = onBookSelected,
            onDismiss = onDismiss,
            onSearch = onSearch,
        )
    }
}

@Composable
private fun LifeBookSearchDialogContent(
    bookSearchState: BookSearchState?,
    onQueryChange: (String) -> Unit,
    onBookSelected: (BookItem) -> Unit,
    onDismiss: () -> Unit,
    onSearch: () -> Unit = {},
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.white)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "나의 인생 책",
                style = typography.semibold20,
                color = colors.grey900
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.grey100)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = "닫기",
                    tint = colors.grey700,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 검색 입력
        SearchInputField(
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            modifier = Modifier.fillMaxWidth()
        )

        // 검색 결과
        when (bookSearchState) {
            is BookSearchState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colors.uiMain,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            is BookSearchState.Success -> {
                Column {
                    HorizontalDivider(thickness = 1.dp, color = colors.grey100)
                    if (bookSearchState.books.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "검색 결과가 없습니다.",
                                style = typography.regular16,
                                color = colors.grey600
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 360.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp)
                        ) {
                            items(bookSearchState.books) { book ->
                                BookResultItem(
                                    book = book,
                                    onClick = { onBookSelected(book) }
                                )
                            }
                        }
                    }
                }
            }
            is BookSearchState.Error -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "검색 중 오류가 발생했습니다.\n다시 시도해주세요.",
                        style = typography.regular16,
                        color = colors.grey600,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun SearchInputField(
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography
    val focusManager = LocalFocusManager.current

    var query by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, colors.grey300, RoundedCornerShape(16.dp))
            .background(colors.white)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = "검색",
            tint = colors.grey500,
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    focusManager.clearFocus()
                    onSearch()
                }
        )
        BasicTextField(
            value = query,
            onValueChange = { new ->
                query = new
                onQueryChange(new)
            },
            modifier = Modifier.weight(1f),
            textStyle = typography.regular16.copy(color = colors.grey900),
            cursorBrush = SolidColor(colors.uiMain),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                onSearch()
            }),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "도서명, 저자명 검색",
                        style = typography.regular16,
                        color = colors.grey500
                    )
                }
                innerTextField()
            }
        )
        if (query.isNotEmpty()) {
            Icon(
                painter = painterResource(R.drawable.ic_x),
                contentDescription = "지우기",
                tint = colors.grey500,
                modifier = Modifier
                    .size(16.dp)
                    .clickable {
                        query = ""
                        onQueryChange("")
                    }
            )
        }
    }
}

@Composable
private fun BookResultItem(
    book: BookItem,
    onClick: () -> Unit,
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = book.image,
            contentDescription = book.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(width = 64.dp, height = 95.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.white)
                .border(1.dp, colors.grey100, RoundedCornerShape(8.dp))
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = book.title.stripBookSubtitle(),
                style = typography.medium18,
                color = colors.grey800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${book.author} (${book.categoryLabel})",
                style = typography.regular16,
                color = colors.grey600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "검색 입력 필드")
@Composable
private fun SearchInputFieldPreview() {
    BookiiBookiiTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SearchInputField(onQueryChange = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview(showBackground = true, name = "검색 결과 아이템")
@Composable
private fun BookResultItemPreview() {
    BookiiBookiiTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            BookResultItem(
                book = BookItem("프로젝트 헤일메리", "앤디 위어", "", "알에이치코리아", "9788925563602", "소설", "소설", ""),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "검색 다이얼로그 - 검색 전")
@Composable
private fun LifeBookSearchDialogIdlePreview() {
    BookiiBookiiTheme {
        LifeBookSearchDialogContent(
            bookSearchState = BookSearchState.Idle,
            onQueryChange = {},
            onBookSelected = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "검색 다이얼로그 - 결과 있음")
@Composable
private fun LifeBookSearchDialogResultPreview() {
    BookiiBookiiTheme {
        LifeBookSearchDialogContent(
            bookSearchState = BookSearchState.Success(
                listOf(
                    BookItem("사요가 바로 살인자", "장우영", "", "출판사", "9781234567890", "소설", "소설", ""),
                    BookItem("헤일리가 살인자?", "김하늘", "", "출판사", "9781234567891", "과학", "과학", ""),
                    BookItem("사실 무스가 살인자", "무스", "", "출판사", "9781234567892", "IT", "IT", ""),
                )
            ),
            onQueryChange = {},
            onBookSelected = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "검색 다이얼로그 - 결과 없음")
@Composable
private fun LifeBookSearchDialogEmptyPreview() {
    BookiiBookiiTheme {
        LifeBookSearchDialogContent(
            bookSearchState = BookSearchState.Success(emptyList()),
            onQueryChange = {},
            onBookSelected = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "검색 다이얼로그 - 오류")
@Composable
private fun LifeBookSearchDialogErrorPreview() {
    BookiiBookiiTheme {
        LifeBookSearchDialogContent(
            bookSearchState = BookSearchState.Error("네트워크 오류가 발생했습니다."),
            onQueryChange = {},
            onBookSelected = {},
            onDismiss = {}
        )
    }
}
