package com.bookiibookii.bookiibookii.mypage.ui.main

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.ExchangeTypeChip
import com.bookiibookii.bookiibookii.ui.component.ReviewTypeChip
import com.bookiibookii.bookiibookii.mypage.ui.detail.verticalRotation
// ── Mock data ─────────────────────────────────────────────────────────────────
data class MockBook(val title: String, val isOrange: Boolean)

data class MockWrittenReview(
    val title: String,
    val author: String,
    val rating: Int,
    val isTogether: Boolean,
    val comment: String,
    val date: String,
)

data class MockReceivedReview(
    val nickname: String,
    val isGood: Boolean,
    val comment: String,
    val date: String,
)

private val mockBooks = listOf(
    MockBook("없어질 행성에서 씁니다", true),
    MockBook("어린 개가 왔다", true),
    MockBook("고쳐 쓰는 마음", false),
    MockBook("모국어는 차라리 침묵", true),
    MockBook("괴테는 모든 것을 말했다", true),
    MockBook("인생을 위한 최소한의 생각", false),
    MockBook("어린 개가 왔다", false),
)

private val mockWrittenReviews = listOf(
    MockWrittenReview("채식주의자", "한강", 5, true,
        "폭력과 욕망, 인간의 원초적 본성에 대한 날카로운 탐구. 영혜의 침묵은 어느 웅변보다 강렬하게 독자를 압도한다.", "2026. 04. 05."),
    MockWrittenReview("프로젝트 헤일메리", "앤디 위어", 4, false,
        "영화 볼 땐 그레이스랑 스트라트랑 가능?이라고 생각했는데 책 읽으니까 #죄송합니다 이렇게 됨... 거의 700페이지인데 이틀 만에 다 읽어버림", "2026. 04. 05."),
)

private val mockReceivedReviews = listOf(
    MockReceivedReview("sayo", true, "글씨짱예쁘심..", "2026. 04. 05."),
    MockReceivedReview("무스쨩", true, "덕분에 재밌게 완독했어요~ 감사합니다 다음에도 또 교환하고 싶어요", "2026. 04. 05."),
    MockReceivedReview("Hailey", false, "책이 파손되어 있었어요", "2026. 04. 05."),
)

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun MypageScreen(
    onBackClick: () -> Unit = {},
    onSettingClick: () -> Unit = {},
    onBookshelfClick: () -> Unit = {},
    onWrittenReviewClick: () -> Unit = {},
    onReceivedReviewClick: () -> Unit = {},
    onProfileSettingClick: () -> Unit = {},
    onAddressManagementClick: () -> Unit = {},
) {
    var isMottoEditing by remember { mutableStateOf(false) }
    var mottoText by remember { mutableStateOf("역시나 누군가를 사랑하고 사랑해야 할 당신을 위해") }
    var mottoInput by remember { mutableStateOf(mottoText) }
    var showShareDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.uiBg)) {
            // TopBar fixed — status bar inset handled here so it doesn't scroll away
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BookiiBookiiTheme.colors.white),
            ) {
                MypTopBar(onBackClick = onBackClick, onSettingClick = onSettingClick)
            }

            // Scrollable content below TopBar
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                // White card: profile + motto + books
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BookiiBookiiTheme.colors.white)
                        .padding(top = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    ProfileSection(
                        onProfileSettingClick = onProfileSettingClick,
                        onAddressManagementClick = onAddressManagementClick,
                        onProfileShareClick = { showShareDialog = true },
                    )

                    MottoSection(
                        motto = mottoText,
                        isEditing = isMottoEditing,
                        editText = mottoInput,
                        onEditTextChange = { mottoInput = it },
                        onEditClick = { mottoInput = mottoText; isMottoEditing = true },
                        onCancelClick = { isMottoEditing = false },
                        onSaveClick = { mottoText = mottoInput; isMottoEditing = false },
                    )

                    RepresentativeBooksSection(books = mockBooks, onArrowClick = onBookshelfClick)
                }

                Spacer(modifier = Modifier.height(16.dp))
                WrittenReviewsSection(reviews = mockWrittenReviews, onArrowClick = onWrittenReviewClick)

                Spacer(modifier = Modifier.height(16.dp))
                ReceivedReviewsSection(reviews = mockReceivedReviews, onArrowClick = onReceivedReviewClick)

                // Bottom padding accounting for system navigation bar
                Spacer(modifier = Modifier.height(24.dp))
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }

        if (showShareDialog) {
            ProfileShareDialog(onDismiss = { showShareDialog = false })
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────
@Composable
private fun MypTopBar(onBackClick: () -> Unit, onSettingClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(32.dp),
                )
            }
            Text(
                text = "마이페이지",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
            )
            IconButton(onClick = onSettingClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_gear),
                    contentDescription = "설정",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 0.5.dp)
    }
}

// ── Profile section ───────────────────────────────────────────────────────────
@Composable
private fun ProfileSection(
    onProfileSettingClick: () -> Unit = {},
    onAddressManagementClick: () -> Unit = {},
    onProfileShareClick: () -> Unit = {},
) {
    val btnShape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(BookiiBookiiTheme.colors.grey200),
            )
            Text(
                text = "김스카이",
                style = BookiiBookiiTheme.typography.semibold20,
                color = BookiiBookiiTheme.colors.grey900,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(btnShape)
                    .background(BookiiBookiiTheme.colors.white)
                    .border(1.dp, BookiiBookiiTheme.colors.grey200, btnShape)
                    .clickable(onClick = onProfileSettingClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "프로필 수정",
                    style = BookiiBookiiTheme.typography.semibold15,
                    color = BookiiBookiiTheme.colors.grey900,
                    maxLines = 1,
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(btnShape)
                    .background(BookiiBookiiTheme.colors.white)
                    .border(1.dp, BookiiBookiiTheme.colors.grey200, btnShape)
                    .clickable(onClick = onAddressManagementClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "주소지 관리",
                    style = BookiiBookiiTheme.typography.semibold15,
                    color = BookiiBookiiTheme.colors.grey900,
                    maxLines = 1,
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(btnShape)
                    .background(BookiiBookiiTheme.colors.white)
                    .border(1.dp, BookiiBookiiTheme.colors.grey200, btnShape)
                    .clickable(onClick = onProfileShareClick)
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_share),
                    contentDescription = "공유",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

// ── Motto section ─────────────────────────────────────────────────────────────
@Composable
private fun MottoSection(
    motto: String,
    isEditing: Boolean,
    editText: String,
    onEditTextChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "나를 대표하는 문구",
                modifier = Modifier.weight(1f),
                style = BookiiBookiiTheme.typography.semibold16,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BookiiBookiiTheme.colors.grey200)
                    .clickable(onClick = onEditClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "수정",
                    style = BookiiBookiiTheme.typography.medium11,
                    color = BookiiBookiiTheme.colors.grey700,
                )
            }
        }

        if (isEditing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BookiiBookiiTheme.colors.grey100)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { if (it.length <= 100) onEditTextChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    placeholder = {
                        Text(
                            text = "나만의 인용구를 입력하세요...",
                            style = BookiiBookiiTheme.typography.regular15,
                            color = BookiiBookiiTheme.colors.grey500,
                        )
                    },
                    textStyle = BookiiBookiiTheme.typography.regular15.copy(color = BookiiBookiiTheme.colors.grey900),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    maxLines = 4,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${editText.length}/100",
                        style = BookiiBookiiTheme.typography.regular12,
                        color = BookiiBookiiTheme.colors.grey500,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onCancelClick,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BookiiBookiiTheme.colors.grey200),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = BookiiBookiiTheme.colors.white),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "취소",
                                style = BookiiBookiiTheme.typography.regular15,
                                color = BookiiBookiiTheme.colors.grey900,
                            )
                        }
                        Button(
                            onClick = onSaveClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BookiiBookiiTheme.colors.grey900),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "저장",
                                style = BookiiBookiiTheme.typography.regular15,
                                color = BookiiBookiiTheme.colors.white,
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BookiiBookiiTheme.colors.grey100)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // ic_comma 아이콘 사용 (피그마: Icon/quote, 28dp)
                Icon(
                    painter = painterResource(R.drawable.ic_quote),
                    contentDescription = null,
                    tint = BookiiBookiiTheme.colors.uiMain,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = motto,
                    style = BookiiBookiiTheme.typography.medium15,
                    color = BookiiBookiiTheme.colors.grey700,
                )
            }
        }
    }
}

// ── Representative books ──────────────────────────────────────────────────────
@Composable
private fun RepresentativeBooksSection(books: List<MockBook>, onArrowClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "나를 대표하는 책",
                    style = BookiiBookiiTheme.typography.semibold16,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BookiiBookiiTheme.colors.grey200, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "${books.size}/${books.size}권",
                        style = BookiiBookiiTheme.typography.medium11,
                        color = BookiiBookiiTheme.colors.grey900,
                    )
                }
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { scaleX = -1f }
                    .clickable { onArrowClick() },
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            // ✅ 추가: 책 높이가 제각각이어도 모두 바닥으로 정렬되게 맞춤
            verticalAlignment = Alignment.Bottom
        ) {
            books.forEach { book ->
                BookSpineItem(
                    title = book.title,
                    isOrange = book.isOrange,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BookSpineItem(title: String, isOrange: Boolean, modifier: Modifier = Modifier) {
    val bgColor   = if (isOrange) BookiiBookiiTheme.colors.uiMain150 else BookiiBookiiTheme.colors.uiMainSubPale
    val textColor = if (isOrange) BookiiBookiiTheme.colors.uiMain     else BookiiBookiiTheme.colors.uiMainSub

    val archHeight = 15.dp

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        // [1] 본문 사각형 (글자 포함) — arch 높이 절반만큼 아래로 내려 arch 하단이 덮이게 함
        Box(
            modifier = Modifier
                .padding(top = archHeight / 2)
                .fillMaxWidth()
                .background(
                    color = bgColor,
                    shape = RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp),
                )
                .padding(top = 24.dp, bottom = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                style = BookiiBookiiTheme.typography.medium16,
                color = textColor,
                maxLines = 1,
                modifier = Modifier.verticalRotation(),
            )
        }

        // [2] 상단 half-ellipse arch — Canvas로 뾰족한 아치 그리기
        Canvas(modifier = Modifier.fillMaxWidth().height(archHeight)) {
            val path = Path().apply {
                arcTo(
                    rect = Rect(0f, 0f, size.width, size.height),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = true,
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, color = bgColor)
        }
    }
}

// ── Written reviews ───────────────────────────────────────────────────────────
@Composable
private fun WrittenReviewsSection(reviews: List<MockWrittenReview>, onArrowClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "작성한 후기",
                style = BookiiBookiiTheme.typography.semibold16,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { scaleX = -1f }
                    .clickable { onArrowClick() },
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BookiiBookiiTheme.colors.white)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_book),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.uiMain,
                modifier = Modifier.size(24.dp),
            )
            val bookCountText = buildAnnotatedString {
                withStyle(BookiiBookiiTheme.typography.medium16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey900)) {
                    append("45")
                }
                withStyle(BookiiBookiiTheme.typography.regular16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey700)) {
                    append("권의 책에 후기를 남겼어요")
                }
            }
            Text(text = bookCountText)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            reviews.forEach { WrittenReviewCard(it) }
        }
    }
}

@Composable
private fun WrittenReviewCard(review: MockWrittenReview) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = review.title,
                        style = BookiiBookiiTheme.typography.semibold16,
                        color = BookiiBookiiTheme.colors.grey900,
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(15.dp)
                            .background(BookiiBookiiTheme.colors.grey200),
                    )
                    Text(
                        text = review.author,
                        style = BookiiBookiiTheme.typography.semibold16,
                        color = BookiiBookiiTheme.colors.grey900,
                    )
                }
                StarRating(rating = review.rating)
            }
            ExchangeTypeChip(isDelivery = review.isTogether)
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200)
        Text(
            text = review.comment,
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey700,
        )
        Text(
            text = review.date,
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey500,
        )
    }
}

// ── Received reviews ──────────────────────────────────────────────────────────
@Composable
private fun ReceivedReviewsSection(reviews: List<MockReceivedReview>, onArrowClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "받은 후기",
                style = BookiiBookiiTheme.typography.semibold16,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { scaleX = -1f }
                    .clickable { onArrowClick() },
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BookiiBookiiTheme.colors.white)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_hand_thumbs_up),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.uiMain,
                modifier = Modifier.size(24.dp),
            )
            // Single annotated string prevents awkward line breaking
            val summaryText = buildAnnotatedString {
                withStyle(BookiiBookiiTheme.typography.medium16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey900)) {
                    append("38")
                }
                withStyle(BookiiBookiiTheme.typography.regular16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey700)) {
                    append("명의 부키메이트가 ")
                }
                withStyle(BookiiBookiiTheme.typography.medium16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey900)) {
                    append("김스카이")
                }
                withStyle(BookiiBookiiTheme.typography.regular16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey700)) {
                    append("님을 좋아합니다.")
                }
            }
            Text(text = summaryText, modifier = Modifier.weight(1f))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            reviews.forEach { ReceivedReviewCard(it) }
        }
    }
}

@Composable
private fun ReceivedReviewCard(review: MockReceivedReview) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BookiiBookiiTheme.colors.grey200),
                )
                Text(
                    text = review.nickname,
                    style = BookiiBookiiTheme.typography.medium16,
                    color = BookiiBookiiTheme.colors.grey800,
                )
            }
            ReviewTypeChip(isGood = review.isGood, modifier = Modifier)
        }
        Text(
            text = review.comment,
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey700,
        )
        Text(
            text = review.date,
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey500,
        )
    }
}

// ── Shared components ─────────────────────────────────────────────────────────
@Composable
private fun StarRating(rating: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy((-2).dp)
    ) {
        repeat(5) { index ->
            Icon(
                painter = painterResource(R.drawable.ic_star),
                contentDescription = null,
                tint = if (index < rating) BookiiBookiiTheme.colors.uiMainSub else BookiiBookiiTheme.colors.grey200,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}


// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, widthDp = 412)
@Composable
fun MypageScreenPreview() {
    MypageScreen()
}
