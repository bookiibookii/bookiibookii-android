package com.bookiibookii.bookiibookii.group.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.ui.component.BottomSheetBtnStyle
import com.bookiibookii.bookiibookii.ui.component.BottomSheetChip
import com.bookiibookii.bookiibookii.ui.component.BottomSheetTwoBtnShort
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private const val ALL = "전체"
private const val LITERATURE = "문학"
private const val NON_LITERATURE = "비문학"

private val categories = listOf(ALL, LITERATURE, NON_LITERATURE)

private val literatureGenres = listOf(
    "한국소설", "세계소설", "장르소설", "로맨스", "역사소설", "시/에세이", "희곡/문학", "기타",
)
private val nonLiteratureGenres = listOf(
    "경제/경영", "과학/IT", "인문/역사", "가정/취미", "예술/문화", "자기계발", "정치/사회", "기타",
)

private fun genresOf(category: String?): List<String> = when (category) {
    LITERATURE -> literatureGenres
    NON_LITERATURE -> nonLiteratureGenres
    else -> emptyList()
}

private fun headSummary(category: String?, subs: Set<String>, orderedGenres: List<String>): String {
    if (category == null) return ""
    if (category == ALL || subs.isEmpty()) return category
    val ordered = orderedGenres.filter { it in subs }
    val body = if (ordered.size <= 3) {
        ordered.joinToString(" · ")
    } else {
        ordered.take(3).joinToString(" · ") + " 외 ${ordered.size - 3}개"
    }
    return "$category | $body"
}

// 장르 필터 바텀시트
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenreBottomSheet(
    modifier: Modifier = Modifier,
) {
    val sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedSubs by remember { mutableStateOf(setOf<String>()) }

    val subGenres = genresOf(selectedCategory)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = BookiiBookiiTheme.colors.white, shape = sheetShape)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 44.dp, height = 4.dp)
                    .background(
                        color = BookiiBookiiTheme.colors.grey200,
                        shape = BookiiBookiiTheme.shape.round50,
                    ),
            )
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "장르",
                    style = BookiiBookiiTheme.typography.semibold20,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                Text(
                    text = headSummary(selectedCategory, selectedSubs, subGenres),
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.uiMain,
                    maxLines = 1,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                categories.forEachIndexed { index, category ->
                    BottomSheetChip(
                        text = category,
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                            selectedSubs = emptySet()
                        },
                    )
                    if (index < categories.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(BookiiBookiiTheme.colors.grey200),
                        )
                    }
                }
            }

            if (subGenres.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 4,
                ) {
                    subGenres.forEach { genre ->
                        GenreSubChip(
                            text = genre,
                            selected = genre in selectedSubs,
                            onClick = {
                                selectedSubs = if (genre in selectedSubs) {
                                    selectedSubs - genre
                                } else {
                                    selectedSubs + genre
                                }
                            },
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BottomSheetTwoBtnShort(
                text = "취소",
                style = BottomSheetBtnStyle.White,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            BottomSheetTwoBtnShort(
                text = "적용",
                style = BottomSheetBtnStyle.Dark,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// 하위 장르 칩. 선택 시 pale 스타일
@Composable
private fun GenreSubChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = BookiiBookiiTheme.shape.round50
    Box(
        modifier = modifier
            .height(46.dp)
            .widthIn(min = 60.dp)
            .clip(shape)
            .background(
                if (selected) {
                    BookiiBookiiTheme.colors.uiMainPale
                } else {
                    BookiiBookiiTheme.colors.white
                },
            )
            .border(
                width = 1.dp,
                color = if (selected) {
                    BookiiBookiiTheme.colors.uiMain150
                } else {
                    BookiiBookiiTheme.colors.grey200
                },
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.medium16,
            color = if (selected) {
                BookiiBookiiTheme.colors.uiMain
            } else {
                BookiiBookiiTheme.colors.grey900
            },
            maxLines = 1,
        )
    }
}

@Preview(widthDp = 412, showBackground = true)
@Composable
private fun GenreBottomSheetPreview() {
    BookiiPreview {
        GenreBottomSheet()
    }
}
