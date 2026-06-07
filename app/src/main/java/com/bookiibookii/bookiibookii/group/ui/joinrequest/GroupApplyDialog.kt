package com.bookiibookii.bookiibookii.group.ui.joinrequest

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.group.BookItem
import com.bookiibookii.bookiibookii.group.ui.component.BookSearchDropdown
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import java.text.Normalizer

// 그룹 참여 신청 다이얼로그
// 활성화: isbn13 != null && applyMsg 비어있지 않음
@Composable
fun GroupApplyDialog(
    bookSearchQuery: String,
    bookSearchResults: List<BookItem>,
    applyMsg: String,
    canSubmit: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onClearClick: () -> Unit,
    onBookSelect: (BookItem) -> Unit,
    onApplyMsgChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round24)
            .background(BookiiBookiiTheme.colors.white)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // 헤더: 타이틀, 닫기 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "그룹 참여 신청",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BookiiBookiiTheme.colors.grey100)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = "닫기",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        // 책 검색 필드 + 결과 Popup
        BookSearchField(
            query = bookSearchQuery,
            results = bookSearchResults,
            onQueryChange = onQueryChange,
            onSearchClick = onSearchClick,
            onClearClick = onClearClick,
            onBookSelect = onBookSelect,
        )
        // 신청 한 마디 (0/50 카운터)
        ApplyMessageField(
            value = applyMsg,
            onValueChange = onApplyMsgChange,
        )
        // 풋터 — "완료" (책 선택 + 한 마디 입력 시 활성)
        ApplyFooterButton(
            text = "완료",
            enabled = canSubmit,
            onClick = onSubmit,
        )
    }
}

// 책 검색 입력 + 결과 드롭다운
@Composable
private fun BookSearchField(
    query: String,
    results: List<BookItem>,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onClearClick: () -> Unit,
    onBookSelect: (BookItem) -> Unit,
) {
    var fieldSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    // 커서 제어를 위해 TextFieldValue 사용
    var fieldValue by remember { mutableStateOf(TextFieldValue(query)) }
    LaunchedEffect(query) {
        if (fieldValue.text != query) {
            fieldValue = TextFieldValue(text = query, selection = TextRange(query.length))
        }
    }
    // 도서 선택 시 검색 필드를 한 번 하이라이트
    var selectTick by remember { mutableStateOf(0) }
    val highlight = remember { Animatable(0f) }
    LaunchedEffect(selectTick) {
        if (selectTick == 0) return@LaunchedEffect
        highlight.snapTo(1f) // 메인색으로 즉시 점등
        highlight.animateTo(0f, animationSpec = tween(durationMillis = 600)) // 부드럽게 원복
    }
    val fraction = highlight.value
    val fieldBorderColor = lerp(BookiiBookiiTheme.colors.grey200, BookiiBookiiTheme.colors.uiMain, fraction)
    val fieldBgColor = lerp(BookiiBookiiTheme.colors.white, BookiiBookiiTheme.colors.uiMainPale, fraction)
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .onSizeChanged { fieldSize = it }
                .clip(BookiiBookiiTheme.shape.round20)
                .background(fieldBgColor)
                .border(
                    width = 1.dp,
                    color = fieldBorderColor,
                    shape = BookiiBookiiTheme.shape.round20,
                )
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = "검색",
                tint = BookiiBookiiTheme.colors.grey500,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onSearchClick() },
            )
            BasicTextField(
                value = fieldValue,
                onValueChange = { newValue ->
                    val oldLen = Normalizer.normalize(fieldValue.text, Normalizer.Form.NFD).length
                    val newLen = Normalizer.normalize(newValue.text, Normalizer.Form.NFD).length
                    if (newLen < oldLen) {
                        // 한 글자라도 삭제하면 전부 삭제
                        fieldValue = TextFieldValue("")
                        onClearClick()
                    } else {
                        fieldValue = newValue
                        onQueryChange(newValue.text)
                    }
                },
                singleLine = true,
                textStyle = BookiiBookiiTheme.typography.regular16.copy(
                    color = BookiiBookiiTheme.colors.grey900,
                ),
                cursorBrush = SolidColor(BookiiBookiiTheme.colors.uiMain),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchClick() }),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box {
                        if (fieldValue.text.isEmpty()) {
                            Text(
                                text = "어떤 책을 읽어볼까요?",
                                style = BookiiBookiiTheme.typography.regular16,
                                color = BookiiBookiiTheme.colors.grey500,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
        if (results.isNotEmpty()) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(
                    x = 0,
                    y = fieldSize.height + with(density) { 8.dp.roundToPx() },
                ),
                onDismissRequest = {},
                properties = PopupProperties(focusable = false),
            ) {
                BookSearchDropdown(
                    books = results,
                    onBookClick = { book ->
                        selectTick++
                        onBookSelect(book)
                    },
                    modifier = Modifier
                        .width(with(density) { fieldSize.width.toDp() })
                        .heightIn(max = 240.dp),
                )
            }
        }
    }
}

// 신청 한 마디 세션
@Composable
private fun ApplyMessageField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "신청 한 마디",
            style = BookiiBookiiTheme.typography.medium16,
            color = BookiiBookiiTheme.colors.grey900,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(BookiiBookiiTheme.shape.round20)
                .background(BookiiBookiiTheme.colors.white)
                .border(
                    width = 1.dp,
                    color = BookiiBookiiTheme.colors.grey200,
                    shape = BookiiBookiiTheme.shape.round20,
                )
                .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            BasicTextField(
                value = value,
                // 50자 초과 입력 방지 (서버 검증과 일치)
                onValueChange = { if (it.length <= APPLY_MSG_MAX) onValueChange(it) },
                textStyle = BookiiBookiiTheme.typography.medium16.copy(
                    color = BookiiBookiiTheme.colors.grey900,
                ),
                cursorBrush = SolidColor(BookiiBookiiTheme.colors.uiMain),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (value.isEmpty()) {
                            Text(
                                text = "한 마디를 입력해주세요",
                                style = BookiiBookiiTheme.typography.medium16,
                                color = BookiiBookiiTheme.colors.grey300,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            Text(
                text = "${value.length}/$APPLY_MSG_MAX",
                style = BookiiBookiiTheme.typography.medium16,
                color = BookiiBookiiTheme.colors.grey300,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// 풋터 버튼
@Composable
private fun ApplyFooterButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val container = if (enabled) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey200
    val labelColor = if (enabled) BookiiBookiiTheme.colors.white else BookiiBookiiTheme.colors.grey500
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(BookiiBookiiTheme.shape.round16)
            .background(container)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.regular15,
            color = labelColor,
        )
    }
}

private const val APPLY_MSG_MAX = 50

@Preview(widthDp = 364, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun GroupApplyDialogEmptyPreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(12.dp)) {
            GroupApplyDialog(
                bookSearchQuery = "",
                bookSearchResults = emptyList(),
                applyMsg = "",
                canSubmit = false,
                onQueryChange = {},
                onSearchClick = {},
                onClearClick = {},
                onBookSelect = {},
                onApplyMsgChange = {},
                onSubmit = {},
                onDismiss = {},
            )
        }
    }
}

@Preview(widthDp = 364, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun GroupApplyDialogFilledPreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(12.dp)) {
            GroupApplyDialog(
                bookSearchQuery = "녹나무의 여신",
                bookSearchResults = emptyList(),
                applyMsg = "안녕하세요! 끝까지 완독할 자신 있습니다.",
                canSubmit = true,
                onQueryChange = {},
                onSearchClick = {},
                onClearClick = {},
                onBookSelect = {},
                onApplyMsgChange = {},
                onSubmit = {},
                onDismiss = {},
            )
        }
    }
}
