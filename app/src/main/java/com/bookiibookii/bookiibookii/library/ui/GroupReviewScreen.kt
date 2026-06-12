package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private val chatBubbleBg = androidx.compose.ui.graphics.Color(0xFFF4F3F1)

data class ExchangeMessage(
    val username: String,
    val message: String,
    val reaction: String,   // "BOOM_UP" | "BOOM_DOWN" | "" (없음)
    val isMine: Boolean,
    val profileImageUrl: String? = null,
)

data class BookReviewItem(
    val bookTitle: String,
    val bookAuthor: String,
    val bookGenre: String,
    val bookCoverUrl: String? = null,
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
    val myProfileImageUrl: String? = null,
    val partnerProfileImageUrl: String? = null,
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
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BookiiBackButton(onClick = onBackClick)
            Text(text = "후기", style = BookiiBookiiTheme.typography.medium20, color = BookiiBookiiTheme.colors.grey900)
            IconButton(onClick = onEditClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = "수정",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        HorizontalDivider(thickness = 1.dp, color = BookiiBookiiTheme.colors.grey200)

        if (data == null) return@Column

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(23.dp),
        ) {
            // 독서 후기 카드 (그룹 헤더 + 멤버별 코멘트)
            MemberReviewCard(data = data)

            // 도서별 리뷰 카드
            data.bookReviews.forEach { review ->
                BookReviewCard(
                    review = review,
                    myUsername = data.myUsername,
                    partnerUsername = data.partnerUsername,
                    myProfileImageUrl = data.myProfileImageUrl,
                    partnerProfileImageUrl = data.partnerProfileImageUrl,
                )
            }
        }
    }
}

// 독서 후기 카드: 그룹명·기간 헤더 + 멤버별 코멘트(따봉 + 말풍선)
@Composable
private fun MemberReviewCard(data: GroupReviewData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 헤더 (그룹명 + 기간)
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = data.groupName, style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
            Text(text = data.dateRange, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
        }
        HorizontalDivider(thickness = 0.8.dp, color = BookiiBookiiTheme.colors.grey100)

        if (data.messages.isEmpty()) {
            Text(
                text = "작성된 후기가 없습니다.",
                style = BookiiBookiiTheme.typography.regular16,
                color = BookiiBookiiTheme.colors.grey600,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                data.messages.forEach { MemberMessageRow(it) }
            }
        }
    }
}

// 멤버 코멘트 한 줄: 프로필·이름 + (따봉 + 말풍선). 내 것은 오른쪽, 상대는 왼쪽 정렬
@Composable
private fun MemberMessageRow(msg: ExchangeMessage) {
    val hasReaction = msg.reaction == "BOOM_UP" || msg.reaction == "BOOM_DOWN"
    val isGood = msg.reaction == "BOOM_UP"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (msg.isMine) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfilePlaceholder(modifier = Modifier.size(20.dp), imageUrl = msg.profileImageUrl)
            Text(text = msg.username, style = BookiiBookiiTheme.typography.medium12, color = BookiiBookiiTheme.colors.grey800)
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (msg.isMine) {
                if (hasReaction) ReactionBadge(isGood = isGood)
                MemberBubble(text = msg.message, mine = true)
            } else {
                MemberBubble(text = msg.message, mine = false)
                if (hasReaction) ReactionBadge(isGood = isGood)
            }
        }
    }
}

// 멤버 코멘트 말풍선 (채팅형) — 내 것은 grey100, 상대는 sub_pale(파랑)
@Composable
private fun MemberBubble(text: String, mine: Boolean) {
    Box(
        modifier = Modifier
            .widthIn(max = 308.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (mine) chatBubbleBg else BookiiBookiiTheme.colors.uiMainSubPale)
            .padding(16.dp),
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey900,
            textAlign = if (mine) TextAlign.End else TextAlign.Start,
        )
    }
}

@Composable
private fun ReactionBadge(isGood: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (isGood) BookiiBookiiTheme.colors.uiMainPale else BookiiBookiiTheme.colors.uiBg)
            .border(0.5.dp, if (isGood) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey500, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(if (isGood) R.drawable.ic_hand_thumbs_up else R.drawable.ic_hand_thumbs_down),
            contentDescription = null,
            tint = if (isGood) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey500,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun BookReviewCard(
    review: BookReviewItem,
    myUsername: String,
    partnerUsername: String,
    myProfileImageUrl: String? = null,
    partnerProfileImageUrl: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 헤더: 책 썸네일 + 제목/저자
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BookiiBookiiTheme.colors.grey200),
            ) {
                if (!review.bookCoverUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = review.bookCoverUrl,
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = review.bookTitle, style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(text = review.bookAuthor, style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey700)
            }
        }

        HorizontalDivider(thickness = 0.8.dp, color = BookiiBookiiTheme.colors.grey100)

        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 상대방 리뷰 (오른쪽 정렬)
            ReviewBlock(username = partnerUsername, profileImageUrl = partnerProfileImageUrl, rating = review.partnerRating, date = review.partnerDate, review = review.partnerReview, alignEnd = true)
            // 내 리뷰 (왼쪽 정렬)
            ReviewBlock(username = myUsername, profileImageUrl = myProfileImageUrl, rating = review.myRating, date = review.myDate, review = review.myReview, alignEnd = false)
        }
    }
}

// 도서 리뷰 한 블록: 프로필·이름 + 말풍선(별점·날짜 + 리뷰 텍스트)
@Composable
private fun ReviewBlock(username: String, profileImageUrl: String?, rating: Int, date: String, review: String, alignEnd: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfilePlaceholder(modifier = Modifier.size(20.dp), imageUrl = profileImageUrl)
            Text(text = username, style = BookiiBookiiTheme.typography.medium12, color = BookiiBookiiTheme.colors.grey800)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(chatBubbleBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (alignEnd) {
                    Text(text = date, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
                    StarRow(rating = rating)
                } else {
                    StarRow(rating = rating)
                    Text(text = date, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
                }
            }
            Text(
                text = review,
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey900,
                textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StarRow(rating: Int) {
    Row {
        for (i in 1..5) {
            val filled = i <= rating
            Icon(
                painter = painterResource(if (filled) R.drawable.ic_star_fill else R.drawable.ic_star),
                contentDescription = null,
                tint = if (filled) BookiiBookiiTheme.colors.uiMainSub else BookiiBookiiTheme.colors.grey200,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupReviewScreenPreview() {
    BookiiBookiiTheme {
        GroupReviewScreen(
            data = GroupReviewData(
                groupName = "함께 읽는 소설 모임",
                dateRange = "2026.05.01 ~ 2026.05.31",
                myUsername = "북이",
                partnerUsername = "부키",
                messages = listOf(
                    ExchangeMessage(
                        username = "부키",
                        message = "이번 교환 정말 즐거웠어요!",
                        reaction = "BOOM_DOWN",
                        isMine = false,
                    ),
                    ExchangeMessage(
                        username = "북이",
                        message = "저도요, 다음에 또 함께해요 :)",
                        reaction = "BOOM_UP",
                        isMine = true,
                    ),
                ),
                bookReviews = listOf(
                    BookReviewItem(
                        bookTitle = "사피엔스",
                        bookAuthor = "유발 하라리",
                        bookGenre = "인문",
                        myRating = 5,
                        myReview = "인류의 역사를 큰 흐름으로 볼 수 있어 좋았습니다.",
                        myDate = "2026.05.20",
                        partnerRating = 4,
                        partnerReview = "내용이 방대해서 읽는 데 시간이 걸렸지만 유익했어요.",
                        partnerDate = "2026.05.18",
                    ),
                ),
            ),
        )
    }
}
