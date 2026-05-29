package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private val reviewInputBg = Color(0xFFF4F3F1)

data class ReviewBookInfo(val title: String, val author: String, val genre: String)

@Composable
fun ReviewEditScreen(
    groupName: String = "",
    dateRange: String = "",
    partnerName: String = "",
    books: List<ReviewBookInfo> = emptyList(),
    onBackClick: () -> Unit = {},
    onSubmit: () -> Unit = {},
) {
    val ratings = remember(books) { mutableStateListOf(*IntArray(books.size) { 0 }.toTypedArray()) }
    val bookComments = remember { mutableStateListOf(*Array(books.size) { "" }) }
    var isPartnerGood by remember { mutableStateOf<Boolean?>(null) }
    var partnerComment by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BookiiBookiiTheme.colors.uiBg),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BookiiBookiiTheme.colors.white)
                    .border(width = 1.dp, color = BookiiBookiiTheme.colors.grey200)
                    .height(68.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron),
                        contentDescription = "뒤로 가기",
                        tint = BookiiBookiiTheme.colors.grey900,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = "교환독서 리뷰", style = BookiiBookiiTheme.typography.medium20, color = BookiiBookiiTheme.colors.grey900)
                }
                Spacer(modifier = Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(BookiiBookiiTheme.colors.white)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = groupName, style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900)
                    Text(text = dateRange, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
                }

                books.forEachIndexed { index, book ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(BookiiBookiiTheme.colors.white)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(122.dp)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BookiiBookiiTheme.colors.grey200),
                            )
                            Column(
                                modifier = Modifier.weight(1f).height(180.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(text = book.title, style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900)
                                Text(text = "${book.author} (${book.genre})", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey600)
                            }
                        }

                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) { append(book.title) }
                                append("에 대한 평가를 남겨주세요!")
                            },
                            style = BookiiBookiiTheme.typography.regular14,
                            color = BookiiBookiiTheme.colors.grey900,
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 1..5) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_star),
                                    contentDescription = null,
                                    tint = if (i <= ratings[index]) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey200,
                                    modifier = Modifier.size(40.dp).clickable { ratings[index] = i },
                                )
                            }
                        }

                        BasicTextField(
                            value = bookComments[index],
                            onValueChange = { bookComments[index] = it },
                            textStyle = BookiiBookiiTheme.typography.regular14.copy(color = BookiiBookiiTheme.colors.grey900),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(reviewInputBg)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            decorationBox = { inner ->
                                Box(contentAlignment = Alignment.TopStart) {
                                    if (bookComments[index].isEmpty()) {
                                        Text(text = "메모를 입력해주세요.", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey400)
                                    }
                                    inner()
                                }
                            },
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(BookiiBookiiTheme.colors.white)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProfilePlaceholder(modifier = Modifier.size(36.dp))
                        Text(
                            text = "${partnerName}님과의 교환독서는 어떠셨나요?",
                            style = BookiiBookiiTheme.typography.semibold14,
                            color = BookiiBookiiTheme.colors.grey900,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        EvaluationButton(
                            text = "👍  좋았어요",
                            isSelected = isPartnerGood == true,
                            onClick = { isPartnerGood = true },
                            modifier = Modifier.weight(1f),
                        )
                        EvaluationButton(
                            text = "👎  별로였어요",
                            isSelected = isPartnerGood == false,
                            onClick = { isPartnerGood = false },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    BasicTextField(
                        value = partnerComment,
                        onValueChange = { partnerComment = it },
                        textStyle = BookiiBookiiTheme.typography.regular14.copy(color = BookiiBookiiTheme.colors.grey900),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 120.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(reviewInputBg)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.TopStart) {
                                if (partnerComment.isEmpty()) {
                                    Text(text = "교환독서 후기를 남겨주세요.", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey400)
                                }
                                inner()
                            }
                        },
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .height(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(BookiiBookiiTheme.colors.grey900)
                .clickable { onSubmit() },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "수정하기", style = BookiiBookiiTheme.typography.medium18, color = BookiiBookiiTheme.colors.white)
        }
    }
}

@Composable
private fun EvaluationButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) BookiiBookiiTheme.colors.uiMainPale else BookiiBookiiTheme.colors.white)
            .border(1.dp, if (isSelected) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey200, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.medium14,
            color = if (isSelected) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey500,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewEditScreenPreview() {
    ReviewEditScreen()
}
