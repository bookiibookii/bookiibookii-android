package com.bookiibookii.bookiibookii.tracker.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.tracker.vm.TrackerPartnerReviewViewModel
import com.bookiibookii.bookiibookii.ui.component.BookCover
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.component.FooterButton
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.common.stripBookSubtitle
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private const val COMMENT_MAX_LENGTH = 20

@Composable
fun TrackerPartnerReviewRoute(
    groupId: Long,
    onBackClick: () -> Unit,
    onSubmitDone: () -> Unit = onBackClick,
    viewModel: TrackerPartnerReviewViewModel = viewModel(
        factory = TrackerPartnerReviewViewModel.factory(groupId)
    ),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    // 바텀 네비 표시는 TrackerNavHost에서 현재 라우트 기준으로 일괄 제어 (여기서 토글하지 않음)
    TrackerPartnerReviewScreen(
        groupName = uiState.groupName,
        myNickname = uiState.myNickname,
        myBookTitle = uiState.myBookTitle,
        myBookCoverUrl = uiState.myBookCoverUrl,
        myProfileImageUrl = uiState.myProfileImageUrl,
        partnerNickname = uiState.partnerNickname,
        partnerBookTitle = uiState.partnerBookTitle,
        partnerBookCoverUrl = uiState.partnerBookCoverUrl,
        partnerProfileImageUrl = uiState.partnerProfileImageUrl,
        submitting = uiState.submitting,
        onBackClick = onBackClick,
        onSubmit = { reaction, comment ->
            viewModel.submitReview(reaction, comment, onSuccess = onSubmitDone)
        },
    )
}

@Composable
fun TrackerPartnerReviewScreen(
    groupName: String,
    myNickname: String,
    myBookTitle: String,
    myBookCoverUrl: String?,
    myProfileImageUrl: String?,
    partnerNickname: String,
    partnerBookTitle: String,
    partnerBookCoverUrl: String?,
    partnerProfileImageUrl: String?,
    onBackClick: () -> Unit,
    onSubmit: (reaction: String?, comment: String) -> Unit,
    submitting: Boolean = false,
) {
    var rating by remember { mutableStateOf(PartnerRating.NONE) }
    var commentInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TrackerPartnerReviewHeader(onBackClick = onBackClick) },
        bottomBar = {
            TrackerPartnerReviewFooter(
                enabled = commentInput.isNotBlank() && !submitting,
                onSubmit = { onSubmit(rating.toReaction(), commentInput) },
            )
        },
        containerColor = BookiiBookiiTheme.colors.uiBg,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                // imePadding을 verticalScroll '앞'에 둬서 스크롤 컨테이너 높이 자체를 키보드만큼 줄임
                // → 포커스된 입력창이 줄어든 뷰포트 아래로 밀려 bring-into-view로 자동 스크롤됨.
                // 버튼(bottomBar)은 하단 고정
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TrackerCard(
                groupName = groupName,
                myNickname = myNickname,
                myBookTitle = myBookTitle,
                myBookCoverUrl = myBookCoverUrl,
                myProfileImageUrl = myProfileImageUrl,
                partnerNickname = partnerNickname,
                partnerBookTitle = partnerBookTitle,
                partnerBookCoverUrl = partnerBookCoverUrl,
                partnerProfileImageUrl = partnerProfileImageUrl,
            )
            ReviewCard(
                partnerNickname = partnerNickname,
                partnerProfileImageUrl = partnerProfileImageUrl,
                rating = rating,
                onRatingChange = { rating = it },
                comment = commentInput,
                onCommentChange = { commentInput = it.take(COMMENT_MAX_LENGTH) },
            )
        }
    }
}

private enum class PartnerRating { NONE, GOOD, BAD }

// reaction: 좋았어요 → BOOM_UP, 별로였어요 → BOOM_DOWN, 미선택 → null
private fun PartnerRating.toReaction(): String? = when (this) {
    PartnerRating.GOOD -> "BOOM_UP"
    PartnerRating.BAD -> "BOOM_DOWN"
    PartnerRating.NONE -> null
}

@Composable
private fun RatingButton(
    text: String,
    iconRes: Int,
    isSelected: Boolean,
    isPositive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = BookiiBookiiTheme.shape.round20
    val bgColor = if (isSelected) {
        BookiiBookiiTheme.colors.uiMainPale
    } else {
        BookiiBookiiTheme.colors.white
    }
    val borderColor = if (isSelected) {
        BookiiBookiiTheme.colors.uiMain150
    } else {
        BookiiBookiiTheme.colors.grey200
    }
    val contentColor = when {
        isSelected -> BookiiBookiiTheme.colors.uiMain
        isPositive -> BookiiBookiiTheme.colors.grey900
        else -> BookiiBookiiTheme.colors.grey500
    }
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.regular16,
            color = contentColor,
        )
    }
}

@Composable
private fun ReviewCard(
    partnerNickname: String,
    partnerProfileImageUrl: String?,
    rating: PartnerRating,
    onRatingChange: (PartnerRating) -> Unit,
    comment: String,
    onCommentChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round20,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ProfilePlaceholder(
                modifier = Modifier.size(20.dp),
                imageUrl = partnerProfileImageUrl,
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) {
                        append(partnerNickname)
                    }
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey900)) {
                        append("님과의 교환독서는 어떠셨나요?")
                    }
                },
                style = BookiiBookiiTheme.typography.medium16,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RatingButton(
                text = "좋았어요",
                iconRes = R.drawable.ic_hand_thumbs_up,
                isSelected = rating == PartnerRating.GOOD,
                isPositive = true,
                // 이미 선택된 버튼을 다시 누르면 선택 취소
                onClick = {
                    onRatingChange(
                        if (rating == PartnerRating.GOOD) PartnerRating.NONE else PartnerRating.GOOD,
                    )
                },
                modifier = Modifier.weight(1f),
            )
            RatingButton(
                text = "별로였어요",
                iconRes = R.drawable.ic_hand_thumbs_down,
                isSelected = rating == PartnerRating.BAD,
                isPositive = false,
                // 이미 선택된 버튼을 다시 누르면 선택 취소
                onClick = {
                    onRatingChange(
                        if (rating == PartnerRating.BAD) PartnerRating.NONE else PartnerRating.BAD,
                    )
                },
                modifier = Modifier.weight(1f),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = BookiiBookiiTheme.colors.grey100,
                    shape = BookiiBookiiTheme.shape.round20,
                )
                .padding(20.dp),
            contentAlignment = Alignment.TopStart,
        ) {
            if (comment.isEmpty()) {
                Text(
                    text = "파트너에게 소중한 후기를 남겨주세요.",
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey500,
                )
            }
            BasicTextField(
                value = comment,
                onValueChange = onCommentChange,
                modifier = Modifier.fillMaxSize(),
                textStyle = BookiiBookiiTheme.typography.regular16.copy(
                    color = BookiiBookiiTheme.colors.grey900,
                ),
                cursorBrush = SolidColor(BookiiBookiiTheme.colors.uiMain),
            )
        }
    }
}

@Composable
private fun TrackerCard(
    groupName: String,
    myNickname: String,
    myBookTitle: String,
    myBookCoverUrl: String?,
    myProfileImageUrl: String?,
    partnerNickname: String,
    partnerBookTitle: String,
    partnerBookCoverUrl: String?,
    partnerProfileImageUrl: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round20,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = groupName,
                style = BookiiBookiiTheme.typography.medium16,
                color = BookiiBookiiTheme.colors.grey800,
            )
            // 디바이더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(0.8.dp)
                    .background(BookiiBookiiTheme.colors.grey100),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookColumn(
                nickname = myNickname,
                bookTitle = myBookTitle,
                bookCoverUrl = myBookCoverUrl,
                profileImageUrl = myProfileImageUrl,
                isMyBook = true,
                modifier = Modifier.weight(1f),
            )
            BookColumn(
                nickname = partnerNickname,
                bookTitle = partnerBookTitle,
                bookCoverUrl = partnerBookCoverUrl,
                profileImageUrl = partnerProfileImageUrl,
                isMyBook = false,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BookColumn(
    nickname: String,
    bookTitle: String,
    bookCoverUrl: String?,
    profileImageUrl: String?,
    isMyBook: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(width = 100.dp, height = 132.dp),
                contentAlignment = Alignment.BottomEnd,
            ) {
                BookCover(
                    imageUrl = bookCoverUrl,
                    modifier = Modifier.fillMaxSize(),
                )
                if (isMyBook) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(BookiiBookiiTheme.shape.round4)
                            .background(BookiiBookiiTheme.colors.grey200.copy(alpha = 0.75f))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "내 책",
                            style = BookiiBookiiTheme.typography.regular10,
                            color = BookiiBookiiTheme.colors.grey900,
                        )
                    }
                }
            }
            // TrackerCard 위 파트너 오버레이 — BookCover와 겹치므로 innerStroke=true
            ProfilePlaceholder(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 17.dp)
                    .size(44.dp),
                imageUrl = profileImageUrl,
                innerStroke = true,
            )
        }
        Text(
            text = nickname,
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey700,
        )
        Text(
            text = bookTitle.stripBookSubtitle(),
            style = BookiiBookiiTheme.typography.medium16,
            color = BookiiBookiiTheme.colors.grey800,
            textAlign = TextAlign.Center,
            // 항상 2줄 높이 확보 → 한쪽만 길어도 양쪽 칼럼 높이가 같아 표지 정렬 유지
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TrackerPartnerReviewHeader(
    onBackClick: () -> Unit,
) {
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
                text = "교환독서 후기",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.align(Alignment.Center),
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

@Composable
private fun TrackerPartnerReviewFooter(
    enabled: Boolean,
    onSubmit: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        FooterButton(
            text = "등록하기",
            onClick = onSubmit,
            enabled = enabled,
        )
    }
}

@Preview(widthDp = 412, heightDp = 900, showBackground = true)
@Composable
private fun TrackerPartnerReviewScreenPreview() {
    BookiiPreview {
        TrackerPartnerReviewScreen(
            groupName = "김영하 도장깨기 하실 분",
            myNickname = "나",
            myBookTitle = "살인자의 기억법",
            myBookCoverUrl = null,
            myProfileImageUrl = null,
            partnerNickname = "noshel",
            partnerBookTitle = "작별인사",
            partnerBookCoverUrl = null,
            partnerProfileImageUrl = null,
            onBackClick = {},
            onSubmit = { _, _ -> },
        )
    }
}
