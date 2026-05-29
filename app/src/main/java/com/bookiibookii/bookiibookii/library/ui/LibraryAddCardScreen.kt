package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AddCardMode { TEXT, PHOTO }

private const val QUOTE_MAX = 140
private const val TEXT_MEMO_MAX = 110
private const val PHOTO_MEMO_MAX = 150

@Composable
fun LibraryAddCardScreen(
    mode: AddCardMode = AddCardMode.TEXT,
    bookTitle: String = "나는 당신을 편애합니다",
    username: String = "foryxxng",
    onBackClick: () -> Unit = {},
    onSubmit: () -> Unit = {},
) {
    var quote by remember { mutableStateOf("") }
    var page by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    var quoteError by remember { mutableStateOf(false) }
    var pageError by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }

    val memoMax = if (mode == AddCardMode.TEXT) TEXT_MEMO_MAX else PHOTO_MEMO_MAX

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg)
            .imePadding(),
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "뒤로 가기",
                        tint = BookiiBookiiTheme.colors.grey900,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text = "독서카드 추가",
                    style = BookiiBookiiTheme.typography.medium20,
                    color = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.size(40.dp))
            }
            HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 0.5.dp)
        }

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            when (mode) {
                AddCardMode.TEXT -> {
                    // 인용구 (필수)
                    AddCardField(
                        label = "인용구",
                        required = true,
                        value = quote,
                        onValueChange = {
                            if (it.length <= QUOTE_MAX) {
                                quote = it
                                quoteError = false
                            }
                        },
                        placeholder = "인상 깊은 문장을 작성해주세요.",
                        maxLength = QUOTE_MAX,
                        errorText = if (quoteError) "인용구를 입력해주세요" else null,
                    )
                    // 페이지 (필수)
                    AddCardField(
                        label = "페이지",
                        required = true,
                        value = page,
                        onValueChange = {
                            page = it
                            pageError = false
                        },
                        placeholder = "페이지를 입력해주세요.",
                        singleLine = true,
                        keyboardType = KeyboardType.Number,
                        errorText = if (pageError) "페이지를 입력해주세요" else null,
                    )
                    // 메모
                    AddCardField(
                        label = "메모",
                        required = false,
                        value = memo,
                        onValueChange = { if (it.length <= memoMax) memo = it },
                        placeholder = "어떤 책을 읽어볼까요?",
                        maxLength = memoMax,
                    )
                }
                AddCardMode.PHOTO -> {
                    // 사진 업로드 영역
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(BookiiBookiiTheme.colors.grey200)
                                .border(1.dp, BookiiBookiiTheme.colors.grey300, RoundedCornerShape(20.dp))
                                .clickable { /* 갤러리 열기 */ },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_upload),
                                    contentDescription = null,
                                    tint = BookiiBookiiTheme.colors.grey600,
                                    modifier = Modifier.size(24.dp),
                                )
                                Text(
                                    text = "사진 업로드",
                                    style = BookiiBookiiTheme.typography.regular14,
                                    color = BookiiBookiiTheme.colors.grey600,
                                )
                            }
                        }
                    }
                    // 페이지 (필수)
                    AddCardField(
                        label = "페이지",
                        required = true,
                        value = page,
                        onValueChange = {
                            page = it
                            pageError = false
                        },
                        placeholder = "페이지를 입력해주세요.",
                        singleLine = true,
                        keyboardType = KeyboardType.Number,
                        errorText = if (pageError) "페이지를 입력해주세요" else null,
                    )
                    // 메모
                    AddCardField(
                        label = "메모",
                        required = false,
                        value = memo,
                        onValueChange = { if (it.length <= memoMax) memo = it },
                        placeholder = "어떤 책을 읽어볼까요?",
                        maxLength = memoMax,
                    )
                }
            }
        }

        // Footer 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BookiiBookiiTheme.colors.grey200)
                    .clickable { showPreview = true },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "미리보기",
                    style = BookiiBookiiTheme.typography.medium16,
                    color = BookiiBookiiTheme.colors.grey700,
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BookiiBookiiTheme.colors.grey900)
                    .clickable {
                        var valid = true
                        if (mode == AddCardMode.TEXT && quote.isBlank()) {
                            quoteError = true
                            valid = false
                        }
                        if (page.isBlank()) {
                            pageError = true
                            valid = false
                        }
                        if (valid) onSubmit()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "등록하기",
                    style = BookiiBookiiTheme.typography.medium16,
                    color = BookiiBookiiTheme.colors.white,
                )
            }
        }
    }

    // 미리보기 다이얼로그
    if (showPreview) {
        LibraryCardPreviewDialog(
            mode = mode,
            bookTitle = bookTitle,
            username = username,
            quote = quote,
            memo = memo,
            onDismiss = { showPreview = false },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AddCardField(
    label: String,
    required: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int? = null,
    errorText: String? = null,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.bringIntoViewRequester(bringIntoViewRequester)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = label, style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
                if (required) {
                    Text(text = " *", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.uiPointRed)
                }
            }
            if (maxLength != null) {
                Text(
                    text = "${value.length}/$maxLength",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey400,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = BookiiBookiiTheme.typography.regular15.copy(color = BookiiBookiiTheme.colors.grey900),
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (singleLine) Modifier.height(54.dp) else Modifier)
                .clip(RoundedCornerShape(20.dp))
                .background(BookiiBookiiTheme.colors.white)
                .border(
                    width = 1.dp,
                    color = if (errorText != null) BookiiBookiiTheme.colors.uiPointRed else BookiiBookiiTheme.colors.grey300,
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .onFocusEvent { state ->
                    if (state.isFocused) {
                        scope.launch {
                            delay(300)
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = BookiiBookiiTheme.typography.regular15, color = BookiiBookiiTheme.colors.grey500)
                    }
                    inner()
                }
            },
        )
        if (errorText != null) {
            Text(
                text = errorText,
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.uiPointRed,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun LibraryAddCardTextPreview() {
    LibraryAddCardScreen(mode = AddCardMode.TEXT)
}

@Preview(showBackground = true)
@Composable
private fun LibraryAddCardPhotoPreview() {
    LibraryAddCardScreen(mode = AddCardMode.PHOTO)
}
