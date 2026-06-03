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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
    initialRatings: List<Double> = emptyList(),
    initialComments: List<String> = emptyList(),
    onBackClick: () -> Unit = {},
    onSubmit: (ratings: List<Double>, bookComments: List<String>, isPartnerGood: Boolean?, partnerComment: String) -> Unit = { _, _, _, _ -> },
) {
    // 0.0 / 0.5 / 1.0 / ... / 5.0 (0.5 단위) — 기존 후기 있으면 초기값으로 채움
    val ratings = remember(books) {
        mutableStateListOf(*Array(books.size) { i -> initialRatings.getOrElse(i) { 0.0 } })
    }
    val bookComments = remember(books) {
        mutableStateListOf(*Array(books.size) { i -> initialComments.getOrElse(i) { "" } })
    }
    var isPartnerGood by remember { mutableStateOf<Boolean?>(null) }
    var partnerComment by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── 본문 스크롤 영역 ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BookiiBookiiTheme.colors.uiBg),
        ) {
            // 헤더
            Column(modifier = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "뒤로 가기",
                            tint = BookiiBookiiTheme.colors.grey900,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    Text(text = "후기 수정", style = BookiiBookiiTheme.typography.medium20, color = BookiiBookiiTheme.colors.grey900)
                    Spacer(modifier = Modifier.size(40.dp))
                }
                HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 0.5.dp)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 그룹 정보 카드
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(BookiiBookiiTheme.colors.white)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = groupName, style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey800)
                    Text(text = dateRange, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
                }

                // 도서별 평가 카드
                books.forEachIndexed { index, book ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(BookiiBookiiTheme.colors.white)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // 책 커버 (중앙 상단)
                        Box(
                            modifier = Modifier
                                .width(122.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BookiiBookiiTheme.colors.grey200),
                        )

                        // "도서명에 대한 평가를 남겨주세요!"
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) { append(book.title) }
                                withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey900)) { append("에 대한 평가를 남겨주세요!") }
                            },
                            style = BookiiBookiiTheme.typography.medium16,
                        )

                        // 별점 3상태: 0=빈별(grey) / 0.5=sub_pale채움+sub stroke / 1=sub채움
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (i in 1..5) {
                                val rating = ratings[index]
                                val isFull = i.toDouble() <= rating
                                val isHalf = (i - 0.5) == rating
                                Box(
                                    modifier = Modifier.size(40.dp).clickable {
                                        ratings[index] = if (rating == (i - 0.5)) i.toDouble() else i - 0.5
                                    },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isFull) {
                                        Icon(painter = painterResource(R.drawable.ic_star_fill), contentDescription = null, tint = BookiiBookiiTheme.colors.uiMainSub, modifier = Modifier.size(36.dp))
                                    } else if (isHalf) {
                                        Icon(painter = painterResource(R.drawable.ic_star_fill), contentDescription = null, tint = BookiiBookiiTheme.colors.uiMainSubPale, modifier = Modifier.size(36.dp))
                                        Icon(painter = painterResource(R.drawable.ic_star), contentDescription = null, tint = BookiiBookiiTheme.colors.uiMainSub, modifier = Modifier.size(36.dp))
                                    } else {
                                        Icon(painter = painterResource(R.drawable.ic_star), contentDescription = null, tint = BookiiBookiiTheme.colors.grey200, modifier = Modifier.size(36.dp))
                                    }
                                }
                            }
                        }

                        // 감상평 입력
                        BasicTextField(
                            value = bookComments[index],
                            onValueChange = { bookComments[index] = it },
                            textStyle = BookiiBookiiTheme.typography.regular16.copy(color = BookiiBookiiTheme.colors.grey900),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 160.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(reviewInputBg)
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            decorationBox = { inner ->
                                Box(contentAlignment = Alignment.TopStart) {
                                    if (bookComments[index].isEmpty()) {
                                        Text("감상평을 자유롭게 남겨주세요.", style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey400)
                                    }
                                    inner()
                                }
                            },
                        )
                    }
                }

                // 파트너 평가 카드
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(BookiiBookiiTheme.colors.white)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // 파트너명 + 질문
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProfilePlaceholder(modifier = Modifier.size(20.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) { append(partnerName) }
                                withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey900)) { append("님과의 교환독서는 어떠셨나요?") }
                            },
                            style = BookiiBookiiTheme.typography.medium16,
                        )
                    }

                    // 좋았어요 / 별로였어요 버튼
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        EvaluationButton(
                            text = "좋았어요",
                            iconRes = R.drawable.ic_hand_thumbs_up,
                            isSelected = isPartnerGood == true,
                            onClick = { isPartnerGood = true },
                            modifier = Modifier.weight(1f),
                        )
                        EvaluationButton(
                            text = "별로였어요",
                            iconRes = R.drawable.ic_hand_thumbs_down,
                            isSelected = isPartnerGood == false,
                            onClick = { isPartnerGood = false },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // 파트너 후기 입력
                    BasicTextField(
                        value = partnerComment,
                        onValueChange = { partnerComment = it },
                        textStyle = BookiiBookiiTheme.typography.regular16.copy(color = BookiiBookiiTheme.colors.grey900),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 120.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(reviewInputBg)
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.TopStart) {
                                if (partnerComment.isEmpty()) {
                                    Text("부키메이트에 대한 솔직한 후기를 남겨주세요.", style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey400)
                                }
                                inner()
                            }
                        },
                    )
                }
            }
        }

        // ── 하단 수정 버튼 (고정) ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .height(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(BookiiBookiiTheme.colors.grey900)
                .clickable { onSubmit(ratings.toList(), bookComments.toList(), isPartnerGood, partnerComment) },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "수정", style = BookiiBookiiTheme.typography.medium18, color = BookiiBookiiTheme.colors.white)
        }
    }
}

// ── 평가 버튼 (좋았어요 / 별로였어요) ─────────────────────────────────────────

@Composable
private fun EvaluationButton(
    text: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor     = if (isSelected) BookiiBookiiTheme.colors.uiMainPale else BookiiBookiiTheme.colors.white
    val borderColor = if (isSelected) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey200
    val textColor   = if (isSelected) BookiiBookiiTheme.colors.uiMain     else BookiiBookiiTheme.colors.grey500
    val iconTint    = if (isSelected) BookiiBookiiTheme.colors.uiMain     else BookiiBookiiTheme.colors.grey500

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
            Text(text = text, style = BookiiBookiiTheme.typography.regular16, color = textColor)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ReviewEditScreenPreview() {
    BookiiBookiiTheme {
        ReviewEditScreen(
            groupName       = "김영하 도장깨기 하실 분",
            dateRange       = "2025. 12. 18. ~ 2026. 01. 12.",
            partnerName     = "김스카이",
            books           = listOf(
                ReviewBookInfo("프로젝트 헤일메리", "앤디 위어", "SF"),
                ReviewBookInfo("나는 당신을 편애합니다", "김영하", "소설"),
            ),
            initialRatings  = listOf(3.0, 3.5),
            initialComments = listOf("미쳤어요.", ""),
        )
    }
}
