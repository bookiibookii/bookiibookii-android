package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private val chatBubbleBg = androidx.compose.ui.graphics.Color(0xFFF4F3F1)

data class ExchangeMessage(
    val username: String,
    val message: String,
    val reaction: Boolean,
    val isMine: Boolean,
)

data class BookReviewItem(
    val bookTitle: String,
    val bookAuthor: String,
    val bookGenre: String,
    val myRating: Int,
    val myReview: String,
    val myDate: String,
    val partnerRating: Int,
    val partnerReview: String,
    val partnerDate: String,
)

data class GroupReviewData(
    val groupName: String,
    val dateRange: String,
    val myUsername: String,
    val partnerUsername: String,
    val messages: List<ExchangeMessage>,
    val bookReviews: List<BookReviewItem>,
)

@Composable
fun GroupReviewScreen(
    data: GroupReviewData? = null,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white)
                .border(width = 1.dp, color = BookiiBookiiTheme.colors.grey200)
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
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(text = "후기", style = BookiiBookiiTheme.typography.medium20, color = BookiiBookiiTheme.colors.grey900)
            IconButton(onClick = onEditClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = "수정",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        if (data == null) return@Column

        val hasReviews = data.messages.isNotEmpty() || data.bookReviews.isNotEmpty()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(top = 16.dp, bottom = 24.dp),
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
                Text(text = data.groupName, style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900)
                Text(text = data.dateRange, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
            }

            // 후기 없음 빈 상태
            if (!hasReviews) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(BookiiBookiiTheme.colors.white)
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "작성된 후기가 없습니다.",
                        style = BookiiBookiiTheme.typography.regular16,
                        color = BookiiBookiiTheme.colors.grey600,
                    )
                }
                return@Column
            }

            // 교환 후기 채팅 섹션
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BookiiBookiiTheme.colors.white)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                data.messages.forEach { msg ->
                    if (msg.isMine) {
                        // 내 메시지 (오른쪽)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            if (msg.reaction) {
                                ReactionBadge(isGood = true)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            ChatBubble(text = msg.message, isMine = true)
                            Spacer(modifier = Modifier.width(8.dp))
                            ProfilePlaceholder(modifier = Modifier.size(28.dp))
                        }
                    } else {
                        // 상대 메시지 (왼쪽)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            ProfilePlaceholder(modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            ChatBubble(text = msg.message, isMine = false)
                            if (msg.reaction) {
                                Spacer(modifier = Modifier.width(4.dp))
                                ReactionBadge(isGood = true)
                            }
                        }
                    }
                }
            }

            // 도서별 리뷰 섹션
            data.bookReviews.forEach { review ->
                BookReviewCard(review = review, myUsername = data.myUsername, partnerUsername = data.partnerUsername)
            }
        }
    }
}

@Composable
private fun ChatBubble(text: String, isMine: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(chatBubbleBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text = text, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey800)
    }
}

@Composable
private fun ReactionBadge(isGood: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (isGood) BookiiBookiiTheme.colors.uiMainPale else BookiiBookiiTheme.colors.uiBg)
            .border(0.5.dp, if (isGood) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey300, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(if (isGood) R.drawable.ic_hand_thumbs_up else R.drawable.ic_hand_thumbs_down),
            contentDescription = null,
            tint = if (isGood) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey500,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun BookReviewCard(
    review: BookReviewItem,
    myUsername: String,
    partnerUsername: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 책 정보
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(100.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BookiiBookiiTheme.colors.grey200),
            )
            Column(modifier = Modifier.weight(1f).height(100.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = review.bookTitle, style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(text = review.bookAuthor, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey600)
            }
        }

        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200)

        // 상대방 리뷰 (오른쪽 정렬)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = review.partnerDate, style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey400)
                StarRow(rating = review.partnerRating)
                ProfilePlaceholder(modifier = Modifier.size(24.dp))
                Text(text = partnerUsername, style = BookiiBookiiTheme.typography.medium12, color = BookiiBookiiTheme.colors.grey700)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(chatBubbleBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(text = review.partnerReview, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey800, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }

        // 내 리뷰 (왼쪽 정렬)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProfilePlaceholder(modifier = Modifier.size(24.dp))
                Text(text = myUsername, style = BookiiBookiiTheme.typography.medium12, color = BookiiBookiiTheme.colors.grey700)
                StarRow(rating = review.myRating)
                Text(text = review.myDate, style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey400)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(chatBubbleBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(text = review.myReview, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey800, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun StarRow(rating: Int) {
    Row {
        for (i in 1..5) {
            Icon(
                painter = painterResource(R.drawable.ic_star),
                contentDescription = null,
                tint = if (i <= rating) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey200,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupReviewScreenPreview() {
    GroupReviewScreen()
}
