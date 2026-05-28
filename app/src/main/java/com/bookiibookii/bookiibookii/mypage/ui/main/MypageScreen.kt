package com.bookiibookii.bookiibookii.mypage.ui.main

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.data.model.mypage.BookReviewSummaryDto
import com.bookiibookii.bookiibookii.data.model.mypage.ReceivedMemberReviewDto
import com.bookiibookii.bookiibookii.data.model.mypage.UserBookDto
import com.bookiibookii.bookiibookii.data.model.mypage.UserProfileResDTO
import com.bookiibookii.bookiibookii.ui.component.ExchangeTypeChip
import com.bookiibookii.bookiibookii.ui.component.ReviewTypeChip
import com.bookiibookii.bookiibookii.mypage.ui.detail.verticalRotation

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun MypageScreen(
    profile: UserProfileResDTO? = null,
    onSaveIntroduction: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onSettingClick: () -> Unit = {},
    onBookshelfClick: () -> Unit = {},
    onWrittenReviewClick: () -> Unit = {},
    onReceivedReviewClick: () -> Unit = {},
    onProfileSettingClick: () -> Unit = {},
    onAddressManagementClick: () -> Unit = {},
    onInstagramShareClick: () -> Unit = {},
) {
    var isMottoEditing by remember { mutableStateOf(false) }
    var mottoInput by remember(profile?.introduction) { mutableStateOf(profile?.introduction ?: "") }
    var showShareDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.uiBg)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BookiiBookiiTheme.colors.white),
            ) {
                MypTopBar(onBackClick = onBackClick, onSettingClick = onSettingClick)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BookiiBookiiTheme.colors.white)
                        .padding(top = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    ProfileSection(
                        nickname = profile?.nickname ?: "",
                        profileImageUrl = profile?.profileImageUrl,
                        onProfileSettingClick = onProfileSettingClick,
                        onAddressManagementClick = onAddressManagementClick,
                        onProfileShareClick = { showShareDialog = true },
                    )

                    MottoSection(
                        motto = profile?.introduction ?: "",
                        isEditing = isMottoEditing,
                        editText = mottoInput,
                        onEditTextChange = { mottoInput = it },
                        onEditClick = { mottoInput = profile?.introduction ?: ""; isMottoEditing = true },
                        onCancelClick = { isMottoEditing = false },
                        onSaveClick = {
                            onSaveIntroduction(mottoInput)
                            isMottoEditing = false
                        },
                    )

                    RepresentativeBooksSection(
                        books = profile?.userBooks ?: emptyList(),
                        onArrowClick = onBookshelfClick,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                WrittenReviewsSection(
                    reviewCount = profile?.bookReviewCount ?: 0,
                    reviews = profile?.recentBookReviews,
                    onArrowClick = onWrittenReviewClick,
                )

                Spacer(modifier = Modifier.height(16.dp))
                ReceivedReviewsSection(
                    boomUpCount = profile?.boomUpCount ?: 0,
                    nickname = profile?.nickname ?: "",
                    reviews = profile?.recentReceivedReviews,
                    onArrowClick = onReceivedReviewClick,
                )

                Spacer(modifier = Modifier.height(24.dp))
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }

        if (showShareDialog) {
            ProfileShareDialog(
                name = profile?.nickname ?: "",
                motto = profile?.introduction ?: "",
                imageUrl = profile?.profileImageUrl,
                representativeBooks = profile?.userBooks ?: emptyList(),
                onDismiss = { showShareDialog = false },
                onInstagramClick = { showShareDialog = false; onInstagramShareClick() },
            )
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
    nickname: String,
    profileImageUrl: String? = null,
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
            ProfilePlaceholder(modifier = Modifier.size(52.dp), imageUrl = profileImageUrl)
            Text(
                text = nickname,
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
                Icon(
                    painter = painterResource(R.drawable.ic_quote),
                    contentDescription = null,
                    tint = if (motto.isBlank()) BookiiBookiiTheme.colors.grey300 else BookiiBookiiTheme.colors.uiMain,
                    modifier = Modifier.size(28.dp),
                )
                if (motto.isBlank()) {
                    Text(
                        text = "나를 대표하는 문구가 없어요",
                        style = BookiiBookiiTheme.typography.regular15,
                        color = BookiiBookiiTheme.colors.grey400,
                    )
                } else {
                    Text(
                        text = motto,
                        style = BookiiBookiiTheme.typography.medium15,
                        color = BookiiBookiiTheme.colors.grey700,
                    )
                }
            }
        }
    }
}

// ── Representative books ──────────────────────────────────────────────────────
@Composable
private fun RepresentativeBooksSection(books: List<UserBookDto>, onArrowClick: () -> Unit = {}) {
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
                        text = "${books.size}/7권",
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

        // 7권 기준 너비 a = (전체너비 - gap*6) / 7, 권수에 상관없이 각 아이템은 너비 a 고정
        // 높이는 가장 긴 제목 기준으로 결정되며, 짧은 책은 영역 바닥에 정렬
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            val gap = 8.dp
            val itemWidth = (maxWidth - gap * 6) / 7

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.Bottom,
            ) {
                books.forEachIndexed { index, book ->
                    BookSpineItem(
                        title = book.title,
                        isOrange = index % 2 == 0,
                        modifier = Modifier.width(itemWidth),
                    )
                }
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
private fun WrittenReviewsSection(
    reviewCount: Int,
    reviews: List<BookReviewSummaryDto>?,
    onArrowClick: () -> Unit = {},
) {
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
                    append("$reviewCount")
                }
                withStyle(BookiiBookiiTheme.typography.regular16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey700)) {
                    append("권의 책에 후기를 남겼어요")
                }
            }
            Text(text = bookCountText)
        }

        if (reviews.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BookiiBookiiTheme.colors.white)
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "작성한 후기가 없어요",
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey600,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                reviews.forEach { WrittenReviewCard(it) }
            }
        }
    }
}

@Composable
private fun WrittenReviewCard(review: BookReviewSummaryDto) {
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
                        text = review.bookTitle,
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
                        text = review.bookAuthor,
                        style = BookiiBookiiTheme.typography.semibold16,
                        color = BookiiBookiiTheme.colors.grey900,
                    )
                }
                StarRating(rating = review.rating.toInt().coerceIn(0, 5))
            }
            ExchangeTypeChip(isDelivery = review.tradeType == "DELIVERY")
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200)
        Text(
            text = review.comment ?: "",
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey700,
        )
        Text(
            text = DateUtils.formatDate(review.reviewDate),
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey500,
        )
    }
}

// ── Received reviews ──────────────────────────────────────────────────────────
@Composable
private fun ReceivedReviewsSection(
    boomUpCount: Int,
    nickname: String,
    reviews: List<ReceivedMemberReviewDto>?,
    onArrowClick: () -> Unit = {},
) {
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
            val summaryText = buildAnnotatedString {
                withStyle(BookiiBookiiTheme.typography.medium16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey900)) {
                    append("$boomUpCount")
                }
                withStyle(BookiiBookiiTheme.typography.regular16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey700)) {
                    append("명의 부키메이트가 ")
                }
                withStyle(BookiiBookiiTheme.typography.medium16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey900)) {
                    append(nickname)
                }
                withStyle(BookiiBookiiTheme.typography.regular16.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey700)) {
                    append("님을 좋아합니다.")
                }
            }
            Text(text = summaryText, modifier = Modifier.weight(1f))
        }

        if (reviews.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BookiiBookiiTheme.colors.white)
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "받은 후기가 없어요",
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey600,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                reviews.forEach { ReceivedReviewCard(it) }
            }
        }
    }
}

@Composable
private fun ReceivedReviewCard(review: ReceivedMemberReviewDto) {
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
                ProfilePlaceholder(modifier = Modifier.size(32.dp))
                Text(
                    text = review.reviewerNickname,
                    style = BookiiBookiiTheme.typography.medium16,
                    color = BookiiBookiiTheme.colors.grey800,
                )
            }
            ReviewTypeChip(isGood = review.reaction == "BOOM_UP", modifier = Modifier)
        }
        Text(
            text = review.comment ?: "",
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey700,
        )
        Text(
            text = DateUtils.formatDate(review.createdAt),
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
