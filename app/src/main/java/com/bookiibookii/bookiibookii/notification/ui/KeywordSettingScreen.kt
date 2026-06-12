package com.bookiibookii.bookiibookii.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.notification.model.KeywordUiModel
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 키워드 알림 설정 화면 (풀스크린, stateless)
// - 헤더: 뒤로가기 / "키워드 알림 설정"
// - 입력칸: 키워드 입력 + 등록 버튼
// - 리스트: 등록된 키워드 카드 + 휴지통
@Composable
fun KeywordSettingScreen(
    keywordInput: String,
    keywords: List<KeywordUiModel>,
    onBackClick: () -> Unit,
    onKeywordInputChange: (String) -> Unit,
    onAddKeyword: () -> Unit,
    onDeleteKeyword: (KeywordUiModel) -> Unit,
    modifier: Modifier = Modifier,
    maxCount: Int = 10,
) {
    Scaffold(
        topBar = { KeywordSettingHeader(onBackClick = onBackClick) },
        containerColor = BookiiBookiiTheme.colors.uiBg,
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            KeywordInputSection(
                keywordInput = keywordInput,
                count = keywords.size,
                maxCount = maxCount,
                onKeywordInputChange = onKeywordInputChange,
                onAddKeyword = onAddKeyword,
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(keywords, key = { it.id }) { item ->
                    KeywordCard(item = item, onDelete = { onDeleteKeyword(item) })
                }
            }
        }
    }
}

// 헤더 — 뒤로가기(좌) / "키워드 알림 설정"(중앙)
@Composable
private fun KeywordSettingHeader(onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .background(BookiiBookiiTheme.colors.white)
                .padding(horizontal = 16.dp),
        ) {
            BookiiBackButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            Text(
                text = "키워드 알림 설정",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 48.dp),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BookiiBookiiTheme.colors.grey200),
        )
    }
}

// 입력칸 + 카운터
@Composable
private fun KeywordInputSection(
    keywordInput: String,
    count: Int,
    maxCount: Int,
    onKeywordInputChange: (String) -> Unit,
    onAddKeyword: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        KeywordInputField(
            value = keywordInput,
            onValueChange = onKeywordInputChange,
            onAddKeyword = onAddKeyword,
            modifier = Modifier.padding(top = 12.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = count.toString(),
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey500,
            )
            Text(
                text = "/$maxCount 개",
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey500,
            )
        }
    }
}

// 키워드 입력 필드
@Composable
private fun KeywordInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onAddKeyword: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inputShape = RoundedCornerShape(
        topStart = 20.dp,
        bottomStart = 20.dp,
        topEnd = 30.dp,
        bottomEnd = 30.dp,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(inputShape)
            .background(BookiiBookiiTheme.colors.white)
            .border(width = 1.dp, color = BookiiBookiiTheme.colors.grey200, shape = inputShape)
            .padding(start = 20.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = BookiiBookiiTheme.typography.regular16.copy(
                color = BookiiBookiiTheme.colors.grey900,
            ),
            cursorBrush = SolidColor(BookiiBookiiTheme.colors.uiMain),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = "알림 받을 키워드를 입력해주세요",
                        style = BookiiBookiiTheme.typography.regular16,
                        color = BookiiBookiiTheme.colors.grey500,
                    )
                }
                innerTextField()
            },
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(BookiiBookiiTheme.colors.grey100)
                .clickable(onClick = onAddKeyword),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = "키워드 추가",
                tint = BookiiBookiiTheme.colors.grey500,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// 등록된 키워드 카드 — 키워드 텍스트 + 휴지통 삭제
@Composable
private fun KeywordCard(
    item: KeywordUiModel,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round20)
            .background(BookiiBookiiTheme.colors.white)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.keyword,
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey800,
        )
        Icon(
            painter = painterResource(R.drawable.ic_trash),
            contentDescription = "키워드 삭제",
            tint = BookiiBookiiTheme.colors.grey500,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onDelete),
        )
    }
}

@Preview(widthDp = 412, heightDp = 917, showBackground = true)
@Composable
private fun KeywordSettingScreenPreview() {
    BookiiPreview {
        KeywordSettingScreen(
            keywordInput = "",
            keywords = previewKeywords,
            onBackClick = {},
            onKeywordInputChange = {},
            onAddKeyword = {},
            onDeleteKeyword = {},
        )
    }
}

// Preview용 더미
private val previewKeywords: List<KeywordUiModel> = listOf(
    KeywordUiModel(id = 1, keyword = "키워드"),
    KeywordUiModel(id = 2, keyword = "키워드"),
    KeywordUiModel(id = 3, keyword = "키워드"),
    KeywordUiModel(id = 4, keyword = "키워드"),
)
