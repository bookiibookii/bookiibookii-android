package com.bookiibookii.bookiibookii.group.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 인라인 검색 입력바. 제출(키보드 검색 액션 / 돋보기 아이콘) 시 onSearch 호출
@Composable
fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "그룹명, 도서명, 저자로 검색",
) {
    val shape = RoundedCornerShape(
        topStart = 20.dp,
        bottomStart = 20.dp,
        topEnd = 30.dp,
        bottomEnd = 30.dp,
    )
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        onSearch()
    }
    Row(
        modifier = modifier
            .clip(shape)
            .background(BookiiBookiiTheme.colors.white)
            .border(width = 1.dp, color = BookiiBookiiTheme.colors.grey200, shape = shape)
            .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = BookiiBookiiTheme.typography.regular15.copy(
                color = BookiiBookiiTheme.colors.grey900,
            ),
            cursorBrush = SolidColor(BookiiBookiiTheme.colors.uiMain),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { submit() }),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = hint,
                            style = BookiiBookiiTheme.typography.regular15,
                            color = BookiiBookiiTheme.colors.grey500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
            },
        )
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(BookiiBookiiTheme.shape.round50)
                .background(
                    color = if (query.isEmpty()) {
                        BookiiBookiiTheme.colors.grey300
                    } else {
                        BookiiBookiiTheme.colors.grey900
                    },
                )
                .clickable { submit() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = "검색",
                tint = BookiiBookiiTheme.colors.white,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Preview
@Composable
private fun SearchInputFieldPreview() {
    BookiiPreview {
        SearchInputField(query = "", onQueryChange = {}, onSearch = {})
    }
}
